package de.unimuenster.imi.randimi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.unimuenster.imi.randimi.controller.StudyController;
import de.unimuenster.imi.randimi.controller.api.APIControllerV1;
import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.mapping.subject.SubjectEntryMapper;
import de.unimuenster.imi.randimi.model.api.RandomizePatientRequestBodyV1;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartInterval;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartSite;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectListRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.algorithms.Randomization;
import de.unimuenster.imi.randimi.controller.api.APIControllerV2;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.api.RandomizePatientRequestBodyV2;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;

import de.unimuenster.imi.randimi.service.auth.CustomPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.transaction.annotation.Transactional;

/**
 * Randomization Service which handles randomization of subjects.
 * <p>
 * The API has two entry points:
 * {@link #assignSubjectToStudyArm(RandomizePatientRequestBodyV1)} is
 * mainly used by the api controller and
 * has - historically - a slightly different format for parameters then
 * {@link #assignSubjectToStudyArm(SubjectDTO)} which is used by
 * {@link StudyController} to randomize patients.
 *
 * @author Daniel Preciado-Marquez
 */
@Service
public class RandomizationService {

	private final SiteRepository siteRepository;
	private final StudyRepository studyRepository;
	private final SubjectRepository subjectRepository;
	private final SubjectListRepository subjectListRepository;

	private final SubjectEntryMapper subjectMapper;

	private final RandimiExceptionFactoryService exceptionService;
	private final CustomPermissionEvaluator permissionEvaluator;

	private final List<Randomization> availableAlgorithms;
	private final StratumCodeService stratumCodeService;

	private final AuditService auditService;
	private final RandimiMailService mailService;

	@Autowired
	public RandomizationService(final SiteRepository siteRepository,
	                            final StudyRepository studyRepository,
	                            final SubjectEntryMapper subjectMapper,
	                            final CustomPermissionEvaluator permissionEvaluator,
	                            final AuditService auditService,
	                            final RandimiExceptionFactoryService randimiExceptionFactoryService,
	                            final List<Randomization> availableAlgorithms,
	                            final StratumCodeService stratumCodeService,
	                            final SubjectRepository subjectRepository,
	                            final SubjectListRepository subjectListRepository,
	                            final RandimiMailService mailService) {
		this.siteRepository = siteRepository;
		this.studyRepository = studyRepository;
		this.subjectMapper = subjectMapper;
		this.permissionEvaluator = permissionEvaluator;
		this.auditService = auditService;
		this.exceptionService = randimiExceptionFactoryService;
		this.availableAlgorithms = availableAlgorithms;
		this.stratumCodeService = stratumCodeService;
		this.subjectRepository = subjectRepository;
		this.subjectListRepository = subjectListRepository;
		this.mailService = mailService;
	}

	/**
	 * Assigns a subject to a study arm. API entry point for the {@link APIControllerV1}.
	 *
	 * @param body DTO with randomization parameters.
	 * @return The randomized subject.
	 * @throws RandimiException in case of an error.
	 */
	@Deprecated
	@Transactional
	public Subject assignSubjectToStudyArm(RandomizePatientRequestBodyV1 body) throws RandimiException {
		Study study = getStudyByApiIdOrThrow(body.getStudyApiId().toString());
		SubjectDTO subjectDTO = convertRequestBodyToSubjectDTOorThrow(body, study);
		return doAssign(subjectDTO, study);
	}

	/**
	 * Assigns a subject to a study arm. API entry point for the {@link APIControllerV2}.
	 *
	 * @param studyApiId The API ID of the study.
	 * @param body       DTO with randomization parameters.
	 * @return The randomized subject.
	 * @throws RandimiException in case of an error.
	 */
	@Transactional
	public Subject assignSubjectToStudyArm(String studyApiId, RandomizePatientRequestBodyV2 body) throws RandimiException {
		Study study = getStudyByApiIdOrThrow(studyApiId);
		SubjectDTO subjectDTO = convertRequestBodyToSubjectDTOorThrow(body, study);
		return doAssign(subjectDTO, study);
	}

	/**
	 * Assigns a subject to a study arm.
	 * API Entry Point for the {@link StudyController}.
	 *
	 * @param subjectDTO dto with information about the subject
	 * @return The randomized subject.
	 * @throws RandimiException if the assignment failed.
	 */
	@Transactional
	public Subject assignSubjectToStudyArm(SubjectDTO subjectDTO) throws RandimiException {
		Study study = getStudyByIdOrThrow(subjectDTO.getStudyId());

		// Set site api id and location
		Site associatedSite = siteRepository.findById(subjectDTO.getSiteId()).get();
		subjectDTO.setLocation(associatedSite.getGuiName());
		subjectDTO.setSiteApiId(associatedSite.getApiId());

		return doAssign(subjectDTO, study);
	}

	public void preGenerateSubjectList(Study study, SubjectList subjectList, Randomization randomization) {
		final Optional<StratumPartBase> sitePart = subjectList.getStratumParts()
		                                                      .stream()
		                                                      .filter(part -> part instanceof StratumPartSite)
		                                                      .findFirst();
		final Site site;
		if (sitePart.isPresent()) {
			site = ((StratumPartSite) sitePart.get()).getSite();
		} else {
			site = study.getSites().get(0);
		}

		final int subjectListCapacity;
		if (study.getStratums().isEmpty()) {
			subjectListCapacity = study.getCapacity();
		} else if (!study.isStratifiedBySite()) {
			subjectListCapacity = study.getCapacity() / study.getSubjectLists().size();
		} else {
			subjectListCapacity =
					site.getCapacity() / stratumCodeService.getNumberOfStratumCombinationsPerSite(study);
		}

		for (int i = 0; i < subjectListCapacity; ++i) {
			StudyArm studyArm = randomization.getRandomStudyArm(site, subjectList);
			preGenerateSubject(subjectList, studyArm);
		}
	}

	public void preGenerateSubject(SubjectList subjectList, StudyArm studyArm) {
		Subject newSubject = new Subject(subjectList.getSubjects().size() + 1, studyArm);
		newSubject.setStatus(SubjectStatus.PRE_GENERATED);
		subjectList.addSubject(newSubject);
	}

	/**
	 * Method is synchronized because the called method always opens a new transaction
	 * and some time is required to close the transaction.
	 */
	private synchronized Subject doAssign(SubjectDTO subjectDTO, Study study) throws RandimiException {
		if (study.getStatus() == StudyStatus.LOCKED) {
			throw exceptionService.notAcceptableStudyLocked(study);
		}
		if (study.getStatus() == StudyStatus.ARCHIVED) {
			throw exceptionService.notAcceptableStudyArchived(study);
		}

		Site site = getSiteByGuiName(subjectDTO, study);

		final List<StratumPartBase> parts = stratumCodeService.calculateStratumPartCombination(subjectDTO, study);
		final Optional<SubjectList> subjectListOptional = stratumCodeService.getSubjectListForParts(parts, study);

		if (subjectListOptional.isEmpty())
			throw exceptionService.notAcceptableMissingRandomizationList(study);

		final SubjectList subjectList = subjectListOptional.get();
		final long subjectListSize = subjectRepository.countBlockingSubjectInSubjectList(subjectList.getId());
		final String stratumPartCode = stratumCodeService.calculateStratumCombinationCode(subjectDTO, study);

		if (study.getStratums().isEmpty()) {
			// no stratification, only one list exists with the capacity of the study
			if (study.getCapacity() <= subjectListSize)
				throw exceptionService.notAcceptableStudyFull(study);

			if (site.getCapacity() <= subjectRepository.countBlockingSubjectInSubjectListAndSite(
					subjectList.getId(), subjectDTO.getSiteId())) {
				throw exceptionService.notAcceptableSiteFull(site);
			}

		} else if (!study.isStratifiedBySite()) {
			// stratification, but not by site, capacity per list is the capacity of the
			// study divided by the number of lists
			int subjectListCapacity = study.getCapacity() / study.getSubjectLists().size();

			if (subjectListCapacity <= subjectListSize )
				throw exceptionService.notAcceptableStratumPartFull(stratumPartCode, study);

			if (site.getCapacity() <= subjectRepository.countBlockingSubjectInStudyAndSite(
					study.getId(), subjectDTO.getSiteId())) {
				throw exceptionService.notAcceptableSiteFull(site);
			}
		} else {
			// stratification by site, capacity per list is the capacity of the
			// site divided by the number of lists per site
			int subjectListCapacity = site.getCapacity()
			                          / stratumCodeService.getNumberOfStratumCombinationsPerSite(study);

			if (study.getCapacity() <= subjectRepository.countBlockingSubjectInStudy(study.getId()))
				throw exceptionService.notAcceptableStudyFull(study);

			if (subjectListCapacity <= subjectListSize)
				throw exceptionService.notAcceptableStratumPartFull(stratumPartCode, study);
		}

		Subject subject = null;
		if (study.getPreGenerateSubjectList()) {
			for (final Subject subjectCandidate : subjectList.getSubjects()) {
				if (subjectCandidate.getPseudonym() == null) {
					subject = subjectCandidate;
					break;
				}
			}

			subject.setAssignedTo(site, subjectDTO.getPseudonym());
			subject.setStatus(SubjectStatus.ACTIVE);
			subjectRepository.save(subject);

			auditService.createAuditEntryCreateSubject(subject);
		} else {
			Randomization randomization = getAlgorithmImplementation(study.getRandomizationAlgorithm());
			final StudyArm studyArm = randomization.getRandomStudyArm(site, subjectList);

			subject = new Subject(subjectList.getSubjects().size() + 1, studyArm);
			subject.setAssignedTo(site, subjectDTO.getPseudonym());
			subject.setSubjectList(subjectList);
			subjectListRepository.save(subjectList);

			auditService.createAuditEntryCreateSubject(subjectList.getSubjects().get(subject.getSubjectList().size() - 1));
		}

		sendNotificationMail(subjectDTO, study);

		return subject;
	}

	/**
	 * Verifies validity of the studyId and returns the study.
	 *
	 * @param studyApiId API ID of the study.
	 * @return study
	 * @throws RandimiException if the study id is invalid (null or non-existent).
	 */
	private Study getStudyByApiIdOrThrow(final String studyApiId) throws RandimiException {
		if (studyApiId == null || studyApiId.isBlank()) {
			throw exceptionService.missingParameterStudyId();
		}

		final Optional<Study> study = studyRepository.findByApiId(studyApiId);
		if (study.isEmpty() || study.get().isDeleted()) {
			throw exceptionService.notAcceptableMissingStudy(studyApiId);
		}

		return study.get();
	}

	private Study getStudyByIdOrThrow(final Long studyId) throws RandimiException {
		if (studyId == null) {
			throw exceptionService.missingParameterStudyId();
		}

		final Optional<Study> study = studyRepository.findById(studyId);
		if (study.isEmpty()) {
			throw exceptionService.notAcceptableMissingStudy(studyId.toString());
		}

		return study.get();
	}

	@Deprecated
	private SubjectDTO convertRequestBodyToSubjectDTOorThrow(RandomizePatientRequestBodyV1 body, Study study)
			throws RandimiException {
		if (body.getLocationApiId() == null || body.getLocationApiId().trim().isEmpty())
			throw exceptionService.missingParameterLocation();
		final Site site = getLocationByApiId(body.getLocationApiId(), study);
		final Map<String, String> studyStratumParams = convertJsonToMap(body.getStudyStrataParams());
		return doConvertRequestBodyToSubjectDTOorThrow(study, site, body.getPseudonym(), studyStratumParams);
	}

	private SubjectDTO convertRequestBodyToSubjectDTOorThrow(RandomizePatientRequestBodyV2 body, Study study)
			throws RandimiException {
		if (body.getSiteApiId() == null)
			throw exceptionService.missingParameterSiteApiId();
		final Site site = getSiteByApiId(body.getSiteApiId(), study);
		return doConvertRequestBodyToSubjectDTOorThrow(study, site, body.getPseudonym(), body.getStudyStrataParams());
	}

	/**
	 * Convert the parameter of a randomization request to a {@link SubjectDTO}.
	 * Validates the randomization parameters and throw an exception if something is wrong.
	 *
	 * @param study             Study of the subject
	 * @param site              site
	 * @param studyStrataParams Map containing strata parameters
	 * @return The converted SubjectDTO
	 * @throws RandimiException in case of an error
	 */
	private SubjectDTO doConvertRequestBodyToSubjectDTOorThrow(final Study study, final Site site,
	                                                           final String pseudonym,
	                                                           @Nullable final Map<String, String> studyStrataParams)
			throws RandimiException {
		if (study.getSubjectLists() == null || study.getSubjectLists().isEmpty())
			throw exceptionService.notAcceptableMissingRandomizationList(study);

		validatePseudonym(pseudonym, study, site);

		SubjectDTO subjectDTO = new SubjectDTO();
		subjectDTO.setStudyId(study.getId());
		subjectDTO.setStudyApiId(study.getApiId());
		subjectDTO.setSiteId(site.getId());
		subjectDTO.setSiteApiId(site.getApiId());
		subjectDTO.setLocation(site.getGuiName());
		subjectDTO.setPseudonym(pseudonym);

		setStrataParametersInSubjectDto(study, subjectDTO, studyStrataParams);

		return subjectDTO;
	}

	public void validatePseudonym(@Nullable final String pseudonym, final Study study, final Site site)
			throws RandimiException {
		if (pseudonym == null || pseudonym.isBlank())
			throw exceptionService.missingParameterPseudonym();

		// Check if the pseudonym exists
		final var conflict = studyRepository.findRegistered(study, site.getId(), pseudonym);
		if (conflict.isPresent()) {
			final var exception = exceptionService.duplicateRequestPseudonymAlreadyRegistered(pseudonym);

			// Check if the user has permission to read the subject
			var currentUser = SecurityContextHolder.getContext().getAuthentication();
			if (permissionEvaluator.hasPermissionSite(currentUser, conflict.get().getSite(), PermissionType.READ_SUBJECT.name())) {
				var subjectResource = subjectMapper.toSubjectResource(conflict.get());
				exception.getDetails().setExistingSubject(subjectResource);
				auditService.createAuditEntryReadSubjects(study.getId());
			}

			throw exception;
		}

		// Check if pseudonym matches the regular expression
		try {
			if (!Pattern.matches(site.getPseudonymRegex(), pseudonym)) {
				throw exceptionService.unsatisfyingParameterPseudonymRegexMismatch(pseudonym, site.getPseudonymRegex());
			}
		} catch (PatternSyntaxException e) {
			throw exceptionService.malformedParameterPseudonymRegex();
		}
	}

	private Site getSiteByApiId(final String siteApiId, final Study study) throws RandimiException {
		final Site site = study.getSiteByApiId(siteApiId);

		if (site == null)
			throw exceptionService.notAcceptableMissingSite(siteApiId, study);

		return site;
	}

	private Site getLocationByApiId(final String location, final Study study) throws RandimiException {
		final Site site = study.getSiteByApiId(location);

		if (site == null)
			throw exceptionService.notAcceptableMissingLocation(location, study);

		return site;
	}

	/**
	 * Looks for the site of the given subject in the given study.
	 *
	 * @param subjectDTO The subject with the site name.
	 * @param study The study in which the site name should be looked up.
	 * @return The site.
	 */
	private Site getSiteByGuiName(SubjectDTO subjectDTO, Study study) {
		Site site = null;

		for (Site s : study.getSites()) {
			if (s.getId() == subjectDTO.getSiteId()) {
				site = s;
				break;
			}
		}
		if (site == null) {
			throw new IllegalArgumentException("Subject has invalid location value. No site with location name "
			                                   + subjectDTO.getLocation() + " found in study " + study.getGuiName() + " (" + study.getId() + ")");
		}
		return site;
	}

	/**
	 * Returns the service for the given randomization algorithm.
	 * Throws an exception in the language of the given locale if no corresponding service can be found.
	 *
	 * @param algorithm The algorithm to look for.
	 * @return The service for the randomization algorithm.
	 * @throws RandimiException If no service could be found.
	 */
	public Randomization getAlgorithmImplementation(RandomizationAlgorithm algorithm)
			throws RandimiException {
		Randomization randomization = doGetAlgorithmImplementationIntern(algorithm);

		if (randomization == null)
			throw exceptionService.unknownAlgorithm(algorithm.name());

		return randomization;
	}

	private Randomization doGetAlgorithmImplementationIntern(RandomizationAlgorithm algorithm) {
		Randomization randomization = null;

		for (Randomization implementation : availableAlgorithms) {
			if (implementation.getAlgorithm() == algorithm) {
				randomization = implementation;
			}
		}

		return randomization;
	}

	private Map<String, String> convertJsonToMap(String json) throws RandimiException {
		if (json == null || json.trim().isEmpty()) {
			json = "{}";
		}
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(json);
		} catch (JsonProcessingException e) {
			throw exceptionService.malformedParameterJson();
		}

		if (jsonNode == null) {
			throw exceptionService.missingParameterJson();
		}

		final Map<String, String> map = new HashMap<>();
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			final Iterator<Map.Entry<String, JsonNode>> i = objectNode.fields();
			while (i.hasNext()) {
				final var entry = i.next();

				// Check if the value of the enumerated stratum is textual
				if (entry.getValue().isTextual()) {
					map.put(entry.getKey(), entry.getValue().textValue());
				} else if (entry.getValue().isNumber()) {
					map.put(entry.getKey(), Float.toString(entry.getValue().floatValue()));
				} else {
					throw exceptionService.malformedParameterStratum(entry.getKey());
				}
			}
		}
		return map;
	}

	/**
	 * Write stratum parameters from the {@link RandomizePatientRequestBodyV2} to the
	 * {@link SubjectDTO} object.
	 *
	 * @param study             study
	 * @param subjectDTO        target dto
	 * @param studyStrataParams Map containing strata parameters
	 * @throws RandimiException in case of an error
	 */
	private void setStrataParametersInSubjectDto(final Study study, final SubjectDTO subjectDTO,
	                                             @Nullable final Map<String, String> studyStrataParams) throws RandimiException {
		// Order the strata by order number
		List<Stratum> strata = study.getStratums();

		if (!strata.isEmpty() && studyStrataParams == null) {
			throw exceptionService.missingParameterStratumParams();
		}

		List<Float> intervalStrata = new ArrayList<>();
		List<String> enumStrata = new ArrayList<>();

		// Iterate over all strata and check if all parameters are given
		for (Stratum stratum : strata) {
			switch (stratum.getStratumType()) {
				case ENUM:
					String enumStratumValue = studyStrataParams.getOrDefault(stratum.getApiId(), null);
					if (enumStratumValue == null) {
						throw exceptionService.missingParameterStratum(stratum);
					}
					// Check if the value is included in the stratum parts
					if (!stratumContainsValue(stratum, enumStratumValue)) {
						throw exceptionService.notAcceptableMissingMatchingStratumPart(enumStratumValue, stratum);
					}
					enumStrata.add(enumStratumValue);
					break;

				case INTERVAL:
					// Check if the interval stratum is given in the json object
					String intervalStratumValueString = studyStrataParams.getOrDefault(stratum.getApiId(), null);
					if (intervalStratumValueString == null) {
						throw exceptionService.missingParameterStratum(stratum);
					}
					// Check if the value is numeric
					Float intervalStratumValue;
					try {
						intervalStratumValue = Float.valueOf(intervalStratumValueString);
					} catch (Exception e) {
						throw exceptionService.malformedParameterStratum(stratum, e);
					}

					boolean partFound = false;
					// Iterate over all stratum parts and check if the value is in any range
					for (StratumPartBase stratumPart : stratum.getStratumParts()) {
						StratumPartInterval stratumPartInterval = (StratumPartInterval) stratumPart;
						if (stratumPartInterval.isValueContainedInStratumPart(intervalStratumValue)) {
							intervalStrata.add(intervalStratumValue);
							partFound = true;
							break;
						}
					}

					if (!partFound) {
						throw exceptionService.notAcceptableMissingMatchingStratumPart(intervalStratumValue.toString(),
						                                                               stratum);
					}
					break;
				case SITE:
					// Handled by siteApiId
					break;

				default:
					throw exceptionService.unknownStratumType(stratum.getStratumType().name());
			}
		}
		String[] enumStratumArray = new String[enumStrata.size()];
		enumStrata.toArray(enumStratumArray);
		subjectDTO.setEnumeratedStratums(enumStratumArray);

		Float[] intervalStratumArray = new Float[intervalStrata.size()];
		intervalStrata.toArray(intervalStratumArray);
		subjectDTO.setIntervalStratums(intervalStratumArray);
	}

	private boolean stratumContainsValue(final Stratum stratum, final String value) {
		for (final StratumPartBase part : stratum.getStratumParts()) {
			if (part.isValueContainedInStratumPart(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Send an notification mail to notify users of the study about the randomized
	 * patient.
	 *
	 * @param subjectDTO dto with parameters
	 * @param study      study
	 */
	private void sendNotificationMail(final SubjectDTO subjectDTO, final Study study) {
		final String subject = mailService.assembleMailSubject("subjects.add.mail.subject");
		final String content = mailService.assembleMailText("subjects.add.mail.content", new Object[]{study.getGuiName(), subjectDTO.getPseudonym(), subjectDTO.getLocation()});
		mailService.sendStudyNotification(study, subject, content);
	}
}
