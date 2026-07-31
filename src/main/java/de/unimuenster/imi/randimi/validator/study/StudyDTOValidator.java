package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.model.enumeration.PseudonymHandling;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import de.unimuenster.imi.randimi.service.StudyUtilityService;
import de.unimuenster.imi.randimi.validator.study.stratum.StratumDTOValidator;
import de.unimuenster.imi.randimi.service.algorithms.Randomization;

import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class StudyDTOValidator extends AbstractValidator {

	private final SiteRepository siteRepository;
	private final StudyRepository studyRepository;
	private final SubjectRepository subjectRepository;

	private final NamesDTOValidator namesDTOValidator;
	private final StudyArmDTOValidator studyArmDTOValidator;
	private final StratumDTOValidator stratumDTOValidator;
	private final SiteDTOValidator siteDTOValidator;

	private final List<Randomization> availableAlgorithms;

	private final StratumCodeService stratumCodeService;
	private final StudyUtilityService studyUtilityService;

	@Autowired
	public StudyDTOValidator(MessageService messageService, SiteRepository siteRepository,
	                         StudyRepository studyRepository, SubjectRepository subjectRepository,
	                         NamesDTOValidator namesDTOValidator,
	                         StudyArmDTOValidator studyArmDTOValidator, StratumDTOValidator stratumDTOValidator,
	                         SiteDTOValidator siteDTOValidator, List<Randomization> availableAlgorithms,
	                         StratumCodeService stratumCodeService, StudyUtilityService studyUtilityService) {
		super(messageService);
		this.siteRepository = siteRepository;
		this.studyRepository = studyRepository;
		this.subjectRepository = subjectRepository;
		this.namesDTOValidator = namesDTOValidator;
		this.studyArmDTOValidator = studyArmDTOValidator;
		this.stratumDTOValidator = stratumDTOValidator;
		this.siteDTOValidator = siteDTOValidator;
		this.availableAlgorithms = availableAlgorithms;
		this.stratumCodeService = stratumCodeService;
		this.studyUtilityService = studyUtilityService;
	}

	@Override
	public boolean supports(Class<?> type) {
		return StudyDTO.class.isAssignableFrom(type);
	}

	@Override
	public void validate(Object o, Errors errors) {
		StudyDTO studyDTO = (StudyDTO) o;
		final Optional<Study> study = getOriginalStudy(studyDTO);
		boolean activeOrTest = isActiveOrTest(study);

		namesDTOValidator.validate(studyDTO, errors);

		// Name
		if (!errors.hasFieldErrors("guiName")) {
			if (studyWithNameAlreadyExists(studyDTO.getGuiName(), study)) {
				errors.rejectValue("guiName", "errormessage", getMsg("validator.general.mustBeUnique"));
			}
		}

		// API ID
		if (!errors.hasFieldErrors("useApiId") && !errors.hasFieldErrors("apiId") && studyDTO.getUseApiId()) {
			if (studyWithNameAlreadyExists(studyDTO.getApiId(), study)) {
				errors.rejectValue("apiId", "errormessage", getMsg("validator.general.mustBeUnique"));
			}
		}

		// Pre generate subject list
		if (studyDTO.getPreGenerateSubjectList() == null) {
			errors.rejectValue("preGenerateSubjectList", "errormessage", getMsg("validator.general.mustNotBeNull"));
		}

		// Pseudonym handling
		if (studyDTO.getPseudonymHandling() == null) {
			errors.rejectValue("pseudonymHandling", "errormessage", getMsg("validator.general.mustNotBeNull"));
		}
		if (activeOrTest) {
			if (studyDTO.getPseudonymHandling() == PseudonymHandling.UNIQUE_IN_STUDY) {
				// Check if all pseudonyms are unique
				final var numberDuplicatePseudonyms = subjectRepository.countDuplicatePseudonyms(studyDTO.getId());
				if (numberDuplicatePseudonyms > 0) {
					errors.rejectValue("pseudonymHandling", "errormessage",
					                   getMsg("validator.study.pseudonymHandling.duplicatePseudonyms",
					                          numberDuplicatePseudonyms));
				}
			}
		}

		// Randomization algorithm
		if (studyDTO.getRandomizationAlgorithm() == null) {
			errors.rejectValue("randomizationAlgorithm", "errormessage", getMsg("validator.general.mustNotBeNull"));
		}

		// Calculate number of strata
		int numberStratumCombinations = stratumCodeService.getNumberOfStratumCombinationsPerSite(studyDTO);
		int numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(studyDTO);

		Integer studyCapacity = studyDTO.getCapacity();

		// capacity
		if (studyCapacity == null) {
			errors.rejectValue("capacity", "errormessage", getMsg("validator.general.mustNotBeNull"));
		} else if (studyCapacity <= 0) {
			errors.rejectValue("capacity", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 1));
		} else {
			if (activeOrTest) {
				long studySize = subjectRepository.countBlockingSubjectInStudy(studyDTO.getId());
				if (studyCapacity < studySize)
					errors.rejectValue("capacity", "errormessage", getMsg("validator.study.capacitySmallerThanSize", studySize));
			}

			if (!studyDTO.isStratifyBySite() && numberStudyArmParts > 0) {
				// If no stratification by site and valid number of study arms
				// Capacity must be dividable by the number of study arms
				// and
				// Capacity must be dividable by the number of study arms times the number of strata
				if (studyDTO.getCapacity() % numberStudyArmParts != 0) {
					errors.rejectValue("capacity", "errormessage",
					                   getMsg("validator.study.studySizeNotDivisibleByStudyArms", numberStudyArmParts));
				} else if (studyDTO.getCapacity() % (numberStudyArmParts * numberStratumCombinations) != 0) {
					errors.rejectValue("capacity", "errormessage",
					                   getMsg("validator.study.studySizeNotDivisibleByProductStratumsStudyArms",
					                          numberStudyArmParts * numberStratumCombinations));
				}
			}
		}

		if (studyDTO.getSites().isEmpty()) {
			errors.rejectValue("sites", "errormessage", getMsg("validator.study.sitesLessThanOne"));
		}

		List<SiteDTO> sites = studyDTO.getSites();
		int sumOfSiteCapacities = 0;
		for (int i = 0; i < sites.size(); i++) {
			SiteDTO siteDTO = sites.get(i);
			errors.pushNestedPath("sites[" + i + "]");
			siteDTOValidator.validate(siteDTO, errors);
			errors.popNestedPath();

			if (siteDTO.getCapacity() != null) {
				sumOfSiteCapacities += siteDTO.getCapacity();

				if (studyDTO.isStratifyBySite() && numberStudyArmParts > 0) {
					if (siteDTO.getCapacity() % numberStudyArmParts != 0)
						errors.rejectValue("sites[" + i + "].capacity", "errormessage",
						                   getMsg("validator.site.capacityNotDivisibleByStudyArms", numberStudyArmParts));
					else if (siteDTO.getCapacity() % (numberStudyArmParts * numberStratumCombinations) != 0)
						errors.rejectValue("sites[" + i + "].capacity", "errormessage",
						                   getMsg("validator.site.capacityNotDivisibleByProductStratumsStudyArms",
						                          numberStudyArmParts * numberStratumCombinations));
				} else if (studyDTO.getId() != null && studyDTO.getId() != 0
				           && siteDTO.getId() != null && siteDTO.getId() != 0) {
					long siteSize = subjectRepository.countBlockingSubjectInStudyAndSite(studyDTO.getId(),
					                                                                     siteDTO.getId());
					if (siteDTO.getCapacity() != null && siteDTO.getCapacity() < siteSize)
						errors.rejectValue("sites[" + i + "].capacity", "errormessage",
						                   getMsg("validator.site.capacitySmallerThanSize", siteSize));
				}
			}
		}

		if (studyDTO.getCapacity() != null && studyDTO.getCapacity() > sumOfSiteCapacities) {
			String message = getMsg("validator.study.studySizeGreaterThanSumOfSiteCapacities", sumOfSiteCapacities, studyDTO.getCapacity());
			errors.rejectValue("capacity", "errormessage", message);
			for (int i = 0; i < sites.size(); i++) {
				errors.rejectValue("sites[" + i + "].capacity", "errormessage", message);
			}
		}

		namesDTOValidator.validateNames(studyDTO.getSites(), errors, "sites");

		if (activeOrTest) {
			// Capacity of deleted site
			List<Site> deletedSites = siteRepository.getNewAndDeletedSites(studyDTO).getSecond();
			for (Site site : deletedSites) {
				long siteSize = subjectRepository.countBySubjectListStudyIdAndSiteIdAndStatusAndPseudonymNotNull(studyDTO.getId(),
				                                                                                                 site.getId(), SubjectStatus.ACTIVE);
				if (siteSize != 0L)
					errors.rejectValue("sites", "errormessage", getMsg("validator.site.cantDeleteNotEmptySite", site.getGuiName()));
			}

			if (!study.get().isStratifiedBySite() && studyDTO.isStratifyBySite()) {
				// Stratify by site after activation
				if (study.get().getSites().size() != 1) {
					// The study must contain only one site
					errors.rejectValue("stratifyBySite", "errormessage",
					                   getMsg("validator.study.stratifyBySite.activeAndTooManySite"));
				} else if (studyDTO.getPreGenerateSubjectList() &&
				           !Objects.equals(studyDTO.getCapacity(), studyDTO.getSites().get(0).getCapacity())) {
					// If the study has pre-generated lists, the study and the site must have the same capacity,
					// so the size stays the same
					errors.rejectValue("stratifyBySite", "errormessage",
					                   getMsg("validator.study.stratifyBySite.activeAndCapacityMismatch"));
				}
			}
		}

		// Study arms
		if (studyDTO.getStudyArms().size() < 2) {
			errors.rejectValue("studyArms", "errormessage", getMsg("validator.study.studyArmsLessThanTwo"));
		}

		validateStudyArms(errors, studyDTO);
		validateStrataNames(errors, studyDTO);

		if (studyDTO.getRandomizationAlgorithm() != null) {
			for (Randomization implementation : availableAlgorithms) {
				if (implementation.getAlgorithm() == studyDTO.getRandomizationAlgorithm()) {
					implementation.onStudyDTOValidation(errors, studyDTO);
					break;
				}
			}
		}
	}

	private void validateStudyArms(Errors errors, StudyDTO studyDTO) {
		for (int i = 0; i < studyDTO.getStudyArms().size(); i++) {
			// Collect names
			StudyArmDTO studyArm = studyDTO.getStudyArms().get(i);
			errors.pushNestedPath("studyArms[" + i + "]");
			studyArmDTOValidator.validate(studyArm, errors);
			errors.popNestedPath();
		}

		namesDTOValidator.validateNames(studyDTO.getStudyArms(), errors, "studyArms");
	}

	// Check for duplicate stratum names
	private void validateStrataNames(Errors errors, StudyDTO studyDTO) {
		for (int i = 0; i < studyDTO.getEnumeratedStratums().size(); i++) {
			StratumDTO stratum = studyDTO.getEnumeratedStratums().get(i);
			errors.pushNestedPath("enumeratedStratums[" + i + "]");
			stratumDTOValidator.validate(stratum, errors);
			errors.popNestedPath();
		}

		namesDTOValidator.validateNames(studyDTO.getEnumeratedStratums(), errors, "enumeratedStratums");
	}

	/**
	 * Returns the corresponding study entity from the database of the given study DTO.
	 * @param studyDTO The study DTO.
	 * @return Optional containing the study entity if present.
	 */
	private Optional<Study> getOriginalStudy(final StudyDTO studyDTO) {
		if (studyDTO.getId() == null || studyDTO.getId() == 0) {
			return Optional.empty();
		}

		return studyRepository.findById(studyDTO.getId());
	}

	/**
	 * Checks if the given study is activated.
	 * @param study The study to check.
	 * @return If the study is activated.
	 */
	private boolean isActiveOrTest(final Optional<Study> study) {
		return study.isPresent() && (study.get().isActive() || study.get().isInTestMode());
	}

	/**
	 * Checks if another study with the name of the given DTO already exists.
	 * @param name The name.
	 * @param study An optionale containing the corresponding study entity from the database.
	 * @return If another study with the given name exists.
	 */
	private boolean studyWithNameAlreadyExists(final String name, final Optional<Study> study) {
		return !(study.isPresent() && (Objects.equals(study.get().getApiId(), name)
		                               || Objects.equals(study.get().getGuiName(), name)))
		       && studyRepository.existsByGuiNameOrApiId(name, name);
	}
}
