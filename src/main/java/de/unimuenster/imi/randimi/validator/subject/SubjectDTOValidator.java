package de.unimuenster.imi.randimi.validator.subject;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartInterval;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;

import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class SubjectDTOValidator extends AbstractValidator {

	private final StudyRepository studyRepository;
	private final SiteRepository siteRepository;

	@Autowired
	public SubjectDTOValidator(MessageService messageService, StudyRepository studyRepository, SiteRepository siteRepository) {
		super(messageService);
		this.studyRepository = studyRepository;
		this.siteRepository = siteRepository;
	}

	@Autowired
	StudyMapper studyMapper;

	@Override
	public boolean supports(Class<?> type) {
		return SubjectDTO.class.isAssignableFrom(type);
	}

	@Override
	public void validate(Object o, Errors errors) {
		SubjectDTO subjectDTO = (SubjectDTO) o;

		String studyNotExistentErrorMsg = getMsg("validator.subject.studyNotExist", subjectDTO.getStudyId());

		Study associatedStudy = studyRepository.findById(subjectDTO.getStudyId()).orElse(null);
		if (associatedStudy == null) {
			errors.rejectValue("studyId", "errormessage", studyNotExistentErrorMsg);
			return;
		} else if (!associatedStudy.isInTestMode() && !associatedStudy.isActive()) {
			errors.rejectValue("studyId", "errormessage",
			                   getMsg("validator.subject.studyNotActive", subjectDTO.getStudyId()));
			return;
		}

		String siteNotExistentErrorMsg = getMsg("validator.subject.siteNotExists", subjectDTO.getSiteId());

		Site associatedSite = siteRepository.findById(subjectDTO.getSiteId()).orElse(null);
		if (associatedSite == null) {
			errors.rejectValue("siteId", "errormessage", siteNotExistentErrorMsg);
			return;
		} else if (associatedSite.getStudy().getId() != associatedStudy.getId()) {
			errors.rejectValue("siteId", "errormessage",
			                   getMsg("validator.subject.siteNotInStudy", associatedSite.getId(),
			                          associatedStudy.getId()));
			return;
		}

		StudyDTO associatedStudyDTO = studyMapper.toStudyDTO(associatedStudy);

		// Pseudonym
		validatePseudonym(subjectDTO, associatedStudy, associatedSite, errors);

		// Parameters
		int indexEnumerated = 0;
		int indexInterval = 0;

		for (Stratum stratum : associatedStudy.getStratums()) {
			switch (stratum.getStratumType()) {
				case ENUM:
					if (subjectDTO.getEnumeratedStratums().length <= indexEnumerated) {
						errors.rejectValue("enumeratedStratums", "errormessage",
						                   getMsg("validator.subject.parameterNotContained", stratum.getName()));
						continue;
					}

					String enumChoice = subjectDTO.getEnumeratedStratums()[indexEnumerated];
					if (enumChoice == null) {
						errors.rejectValue("enumeratedStratums[" + indexEnumerated + "]", "errormessage",
						                   getMsg("validator.general.mustNotBeEmpty"));
					} else if (isEnumeratedStratumPartMissing(subjectDTO, associatedStudyDTO, indexEnumerated)) {
						errors.rejectValue("enumeratedStratums[" + indexEnumerated + "]", "errormessage",
						                   getMsg("validator.subject.parameterNotContained", stratum.getName()));
					}

					indexEnumerated++;
					break;
				case INTERVAL:
					if (subjectDTO.getIntervalStratums().length <= indexInterval) {
						errors.rejectValue("enumeratedStratums", "errormessage",
						                   getMsg("validator.subject.parameterNotContained", stratum.getName()));
						continue;
					}
					Float intervalChoice = subjectDTO.getIntervalStratums()[indexInterval];
					// Find the right interval
					if (intervalChoice == null) {
						errors.rejectValue("intervalStratums[" + indexInterval + "]", "errormessage",
								getMsg("validator.general.mustNotBeEmpty"));
					} else {
						boolean foundMatchingStratumPart = false;
						for (StratumPartBase stratumPart : stratum.getStratumParts()) {
							StratumPartInterval intervalOption = (StratumPartInterval) stratumPart;
							if (intervalOption.isValueContainedInStratumPart(intervalChoice)) {
								foundMatchingStratumPart = true;
								break;
							}
						}
						if (!foundMatchingStratumPart) {
							errors.rejectValue("intervalStratums[" + indexInterval + "]", "errormessage",
									getMsg("validator.subject.parameterNotInInterval"));
						}
					}
					indexInterval++;
					break;
				default:
					break;
			}
		}
	}

	private void validatePseudonym(SubjectDTO subjectDTO, Study associatedStudy, Site associatedSite, Errors errors) {

		// Check if pseudonym is specified
		if (subjectDTO.getPseudonym() == null || subjectDTO.getPseudonym().trim().isEmpty()) {
			errors.rejectValue("pseudonym", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
			return;
		}

		// Check if pseudonym is too long
		if (subjectDTO.getPseudonym().length() > 255) {
			errors.rejectValue("pseudonym", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));
			return;
		}

		// Check if pseudonym matches the RegEx
		String pseudonymRegex = associatedSite.getPseudonymRegex();
		try {
			if (!Pattern.matches(pseudonymRegex, subjectDTO.getPseudonym())) {
				errors.rejectValue("pseudonym", "errormessage",
				                   getMsg("validator.subject.pseudonymRegexMismatch",
				                          subjectDTO.getPseudonym(), pseudonymRegex));
				return;
			}
		} catch (PatternSyntaxException e) {
			errors.rejectValue("pseudonym", "errormessage",
			                   getMsg("validator.subject.pseudonymRegexMismatch",
			                          subjectDTO.getPseudonym(), pseudonymRegex));
			return;
		}

		// Check if pseudonym is already registered
		Optional<Subject> conflict = studyRepository.findRegistered(associatedStudy, associatedSite.getId(), subjectDTO.getPseudonym());
		if (conflict.isPresent()) {
			switch (associatedStudy.getPseudonymHandling()) {
				case UNIQUE_IN_LOCATION:
					errors.rejectValue("pseudonym", "errormessage",
					                   getMsg("validator.subject.pseudonymAlreadyInLocationRegistered",
					                          subjectDTO.getPseudonym(), conflict.get().getSite().getGuiName()));
					break;
				case UNIQUE_IN_STUDY:
				default:
					errors.rejectValue("pseudonym", "errormessage",
					                   getMsg("validator.subject.pseudonymAlreadyRegistered", subjectDTO.getPseudonym()));
			}
		}
	}

	private boolean isEnumeratedStratumPartMissing(SubjectDTO subjectDTO, StudyDTO associatedStudyDTO, int indexEnumerated) {
		StratumDTO stratum = associatedStudyDTO.getEnumeratedStratums().get(indexEnumerated);
		List<String> enumOptions = stratum.getStratumParts().stream().map(StratumPartBaseDTO::getApiId).toList();
		String enumChoice = subjectDTO.getEnumeratedStratums()[indexEnumerated];
		return !enumOptions.contains(enumChoice);
	}
}
