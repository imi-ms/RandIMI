package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.NamedEntity;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class RandimiExceptionFactoryService {

	private final MessageService messageService;

	public RandimiExceptionFactoryService(MessageService messageService) {
		this.messageService = messageService;
	}

	public RandimiException missingParameterLocation() {
		return new RandimiException.BadRequest(RandimiException.MISSING_PARAMETER_LOCATION,
		                                       messageService.getMessage("exception.noLocation"));
	}

	public RandimiException missingParameterPseudonym() {
		return new RandimiException.BadRequest(RandimiException.MISSING_PARAMETER_PSEUDONYM,
		                                       messageService.getMessage("exception.noPseudonym"));
	}

	public RandimiException missingParameterSiteApiId() {
		return new RandimiException.BadRequest(RandimiException.MISSING_PARAMETER_SITE_API_ID,
		                                       messageService.getMessage("exception.noSiteApiId"));
	}

	public RandimiException missingParameterStudyId() {
		return new RandimiException.BadRequest(RandimiException.MISSING_PARAMETER_STUDY_ID,
		                                       messageService.getMessage("exception.noStudyId"));
	}

	public RandimiException missingParameterJson() {
		return new RandimiException.BadRequest(RandimiException.MISSING_PARAMETER,
		                                       messageService.getMessage("exception.noJson"));
	}

	public RandimiException missingParameterStratumParams() {
		return new RandimiException.BadRequest(RandimiException.MISSING_PARAMETER_STRATUM_PARAMS,
		                                       messageService.getMessage("exception.noStratumParams"));
	}

	public RandimiException missingParameterStratum(String stratumName) {
		return new RandimiException.BadRequest(RandimiException.MISSING_PARAMETER_STRATUM,
		                                       messageService.getMessage("exception.stratumNotFound", stratumName));
	}

	public RandimiException missingParameterStratum(final Stratum stratum) {
		return missingParameterStratum(getIdentifyingParam(stratum));
	}

	public RandimiException malformedParameterJson() {
		return new RandimiException.BadRequest(RandimiException.MALFORMED_PARAMETER,
		                                       messageService.getMessage("exception.noJson"));
	}

	public RandimiException malformedParameterStratum(String stratumName) {
		return new RandimiException.BadRequest(RandimiException.MALFORMED_PARAMETER_STRATUM,
		                                       messageService.getMessage("exception.unexpectedFormat", stratumName));
	}

	public RandimiException malformedParameterStratum(String stratumName, Throwable cause) {
		return new RandimiException.BadRequest(RandimiException.MALFORMED_PARAMETER_STRATUM,
		                                       messageService.getMessage("exception.unexpectedFormat", stratumName),
		                                       cause);
	}

	public RandimiException malformedParameterStratum(final Stratum stratum, final Throwable cause) {
		return malformedParameterStratum(getIdentifyingParam(stratum), cause);
	}

	public RandimiException duplicateRequestPseudonymAlreadyRegistered(String pseudonym) {
		return new RandimiException.Conflict(RandimiException.DUPLICATE_REQUEST_PSEUDONYM_ALREADY_REGISTERED,
		                                     messageService.getMessage("exception.pseudonymAlreadyRegistered",
		                                                               pseudonym));
	}

	public RandimiException unsatisfyingParameterPseudonymRegexMismatch(String pseudonym, String regex) {
		return new RandimiException.BadRequest(RandimiException.UNSATISFYING_PARAMETER_PSEUDONYM_REGEX_MISMATCH,
		                                       messageService.getMessage("exception.pseudonymRegexError", pseudonym,
		                                                                 regex));
	}

	public RandimiException malformedParameterPseudonymRegex() {
		return new RandimiException.BadRequest(RandimiException.MALFORMED_PARAMETER_PSEUDONYM_REGEX,
		                                       messageService.getMessage("exception.pseudonymRegexError"));
	}

	public RandimiException malformedParameterStratumType(String stratumType) {
		return new RandimiException.BadRequest(RandimiException.MALFORMED_PARAMETER_STRATUM_TYPE,
		                                       messageService.getMessage("exception.unexpectedStratumType",
		                                                                 stratumType));
	}

	public RandimiException notAcceptableMissingStudy(final String studyApiId) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_MISSING_STUDY,
		                                          messageService.getMessage("exception.studyNotFound", studyApiId));
	}

	public RandimiException notAcceptableMissingSubjectList(String studyApiId, long subjectListId) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_MISSING_SUBJECT_LIST,
		                                          messageService.getMessage("exception.subjectListNotFound", studyApiId,
		                                                                    subjectListId));
	}

	public RandimiException notAcceptableMissingRandomizationList(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_MISSING_RANDOMIZATION_LIST,
		                                          messageService.getMessage("study.error.noRandomizationList", study));
	}

	public RandimiException notAcceptableMissingRandomizationList(final Study study) {
		return notAcceptableMissingRandomizationList(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableMissingLocation(String location, String studyApiId) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_MISSING_LOCATION,
		                                          messageService.getMessage("exception.locationNotFound", location,
		                                                                    studyApiId));
	}

	public RandimiException notAcceptableMissingLocation(final String location, final Study study) {
		return notAcceptableMissingLocation(location, getIdentifyingParam(study));
	}

	public RandimiException notAcceptableMissingSite(String siteApiId, String studyApiId) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_MISSING_SITE,
		                                          messageService.getMessage("exception.siteNotFound", siteApiId,
		                                                                    studyApiId));
	}

	public RandimiException notAcceptableMissingSite(String siteApiId, final Study study) {
		return notAcceptableMissingSite(siteApiId, getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyFull(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_FULL,
		                                          messageService.getMessage("study.error.studyFull", study));
	}

	public RandimiException notAcceptableStudyFull(final Study study) {
		return notAcceptableStudyFull(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStratumPartFull(final String stratumPart, final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STRATUM_PART_FULL,
		                                          messageService.getMessage("study.error.stratumPartFull", stratumPart,
		                                                                    study));
	}

	public RandimiException notAcceptableStratumPartFull(final String stratumPart, final Study study) {
		return notAcceptableStratumPartFull(stratumPart, getIdentifyingParam(study));
	}

	public RandimiException notAcceptableSiteFull(final String site) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_SITE_FULL,
		                                          messageService.getMessage("study.error.siteFull", site));
	}

	public RandimiException notAcceptableSiteFull(final Site site) {
		return notAcceptableSiteFull(getIdentifyingParam(site));
	}

	public RandimiException notAcceptableStudyActive(String studyName) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_ACTIVE,
		                                          messageService.getMessage("study.error.deleteActivatedStudy",
		                                                                    studyName));
	}

	public RandimiException notAcceptableStudyActive(final Study study) {
		return notAcceptableStudyActive(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyAlreadyActivated(final String studyName) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_ACTIVE,
		                                          messageService.getMessage("study.error.studyAlreadyActivated",
		                                                                    studyName));
	}

	public RandimiException notAcceptableStudyAlreadyActivated(final Study study) {
		return notAcceptableStudyAlreadyActivated(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyArchived(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_ARCHIVED,
		                                          messageService.getMessage("study.error.studyArchived", study));
	}

	public RandimiException notAcceptableStudyArchived(final Study study) {
		return notAcceptableStudyArchived(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyLocked(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_LOCKED,
		                                          messageService.getMessage("study.error.studyLocked", study));
	}

	public RandimiException notAcceptableStudyLocked(final Study study) {
		return notAcceptableStudyLocked(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyDeleted(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_DELETED,
		                                          messageService.getMessage("study.error.studyDeleted", study));
	}

	public RandimiException notAcceptableStudyDeleted(final Study study) {
		return notAcceptableStudyLocked(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyNotActive(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_NOT_ACTIVE,
		                                          messageService.getMessage("study.error.studyNotActive", study));
	}

	public RandimiException notAcceptableStudyNotActive(final Study study) {
		return notAcceptableStudyNotActive(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyNotArchived(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_NOT_ARCHIVED,
		                                          messageService.getMessage("study.error.studyNotArchived", study));
	}

	public RandimiException notAcceptableStudyNotArchived(final Study study) {
		return notAcceptableStudyNotArchived(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyNotLocked(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_NOT_LOCKED,
		                                          messageService.getMessage("study.error.studyNotLocked", study));
	}

	public RandimiException notAcceptableStudyNotLocked(final Study study) {
		return notAcceptableStudyNotLocked(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableStudyAlreadyLocked(final String study) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_STUDY_ALREADY_LOCKED,
		                                          messageService.getMessage("study.error.studyAlreadyLocked", study));
	}

	public RandimiException notAcceptableStudyAlreadyLocked(final Study study) {
		return notAcceptableStudyAlreadyLocked(getIdentifyingParam(study));
	}

	public RandimiException notAcceptableMissingMatchingStratumPart(String stratumValue, String stratumName) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_MISSING_MATCHING_STRATUM_PART,
		                                          messageService.getMessage("exception.noMatchingStratumPartFound",
		                                                                    stratumValue, stratumName));
	}

	public RandimiException notAcceptableMissingMatchingStratumPart(final String stratumValue, final Stratum stratum) {
		return notAcceptableMissingMatchingStratumPart(stratumValue, getIdentifyingParam(stratum));
	}

	public RandimiException unknownAlgorithm(String algorithmName) {
		return new RandimiException.InternalServerError(RandimiException.INTERNAL_SERVER_ERROR_UNKNOWN_ALGORITHM,
		                                                messageService.getMessage("exception.unknownAlgorithm",
		                                                                          algorithmName));
	}

	public RandimiException unknownStratumType(String stratumType) {
		return new RandimiException.InternalServerError(RandimiException.INTERNAL_SERVER_ERROR_UNKNOWN_STRATUM_TYPE,
		                                                messageService.getMessage("exception.unknownStratumType",
		                                                                          stratumType));
	}

	public RandimiException unknownStudy() {
		return new RandimiException.InternalServerError(RandimiException.NOT_ACCEPTABLE_MISSING_STUDY,
		                                                messageService.getMessage("study.error.studyNotExist"));
	}

	public RandimiException notAcceptableMissingEntry(String studyApiId, String pseudonym, Long siteId) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_MISSING_RANDOMIZATION_ENTRY,
		                                          messageService.getMessage(
				                                          "exception.noMatchingRandomizationEntryFound", studyApiId,
				                                          pseudonym, siteId));
	}

	public RandimiException notAcceptableMissingEntry(String studyApiId, String pseudonym, String location) {
		return new RandimiException.NotAcceptable(RandimiException.NOT_ACCEPTABLE_MISSING_RANDOMIZATION_ENTRY,
		                                          messageService.getMessage(
				                                          "exception.noMatchingRandomizationEntryFound", studyApiId,
				                                          pseudonym, location));
	}

	private String getIdentifyingParam(final NamedEntity study) {
		return isApiRequest() ? study.getApiId() : study.getGuiName();
	}

	private boolean isApiRequest() {
		final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (requestAttributes == null) {
			return false;
		}

		final HttpServletRequest request = requestAttributes.getRequest();
		String requestUri = request.getRequestURI();
		return requestUri.contains("/api/");
	}
}
