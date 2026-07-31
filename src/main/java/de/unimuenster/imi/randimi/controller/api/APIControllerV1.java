package de.unimuenster.imi.randimi.controller.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unimuenster.imi.randimi.model.api.StrataInfoResponseV1;
import de.unimuenster.imi.randimi.model.api.RandomizePatientRequestBodyV1;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartEnumeration;
import de.unimuenster.imi.randimi.service.AuditService;
import de.unimuenster.imi.randimi.service.RandimiExceptionFactoryService;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.RandomizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Handler of the API of RandIMI.
 * <p>
 * The URL tree /api/... requires a http basic auth and a user with role ROLE_API_USER.
 * <p>
 * Supported requests are:
 * <ul>
 *     <li>/getApiVersion</li>
 *     <li>/getStudyStrataNames, /getStudyParams</li>
 *     <li>/getStudyStrataInfo, /getStudyInfo</li>
 *     <li>/randomizePatient, /assignStudyArm</li>
 *     <li>/fetchStudyArm, /fetchAssignment</li>
 * </ul>
 *
 * @deprecated As of Randimi 2.0.0, the new API is {@link APIControllerV2}.
 *
 * @author <a href="mailto:tobias.brix@uni-muenster.de">Tobias Brix</a>
 * @author <a href="mailto:tobiashardt@uni-muenster.de">Tobias Hardt</a>
 * @author <a href="mailto:paul.schaub@uni-muenster.de">Paul Schaub</a>
 * @author <a href="mailto:daniel.preciado-marquez@uni-muenster.de">Daniel Preciado-Marquez</a>
 */
@RestController //equals @Controller + @ResponseBody
@RequestMapping(value = {"/api", "/api/v1"}) //each URL of the API begins with /api/...
@Tag(name= "/api/v1", description = "First version of RandIMIs API. Deprecated in favor for v2.")
@CrossOrigin(origins = "*") // TODO: Restrict to more sane range.
@Deprecated(since = "2.0.0")
public class APIControllerV1 {

	/**
	 * Used for logging.
	 */
	private static final Logger LOGGER = LogManager.getLogger(APIControllerV1.class);

	/**
	 * Current version of the API.
	 * Is increased manually.
	 */
	public static final String API_VERSION = "1.0";

	private final SiteRepository siteRepository;
	private final StudyRepository studyRepository;
	private final SubjectRepository subjectRepository;
	private final AuditService auditService;
	private final RandomizationService randomizationService;
	private final RandimiExceptionFactoryService exceptionService;

	@Autowired
	public APIControllerV1(final SiteRepository siteRepository, final StudyRepository studyRepository,
	                       final SubjectRepository subjectRepository, final AuditService auditService,
	                       final RandomizationService randomizationService,
	                       final RandimiExceptionFactoryService randimiExceptionFactoryService) {
		this.siteRepository = siteRepository;
		this.studyRepository = studyRepository;
		this.subjectRepository = subjectRepository;
		this.auditService = auditService;
		this.randomizationService = randomizationService;
		this.exceptionService = randimiExceptionFactoryService;
	}

	//--------------------//
	//    GET-Requests    //
	//--------------------//

	/**
	 * Returns the current version of the API.
	 * This version can be used to ensure compatibility.
	 * The result is returned in the response body.
	 *
	 * @return API Version
	 */
	@RequestMapping(value = "/getApiVersion", method = RequestMethod.GET)
	@Operation(summary = "Get the current version number of the API (eg. '1.0').")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains the version number.",
			             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                schema = @Schema(implementation = String.class),
			                                examples = {@ExampleObject(value = "1.0")}))
	})
	public ResponseEntity<String> getAPIVersion() {
		return new ResponseEntity<>(API_VERSION, HttpStatus.OK);
	}

	@RequestMapping(value = {"/getStudyStrataNames", "/getStudyParams"},
	                method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	@Operation(summary = "Get a list of all stratum names present in the provided study.",
	           description = "This method can be used to quickly determine, which strata are present in a study.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "OK. Response contains a list of stratum names."),
			@ApiResponse(responseCode = "403", description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API."),
			@ApiResponse(responseCode = "406", description = "Not Acceptable. The service cannot execute the request due to internal constraints such as missing randomization lists.")
	})
	public ResponseEntity<String> getStudyStrataNames(
			@Parameter(description = "API ID of the study", required = true)
			@RequestParam(value = "studyId") String studyApiId,
			@Deprecated Locale locale) {
		if (locale != null) {
			LocaleContextHolder.setLocale(locale);
		}

		Study study = studyRepository.findByApiId(studyApiId).orElse(null);
		if (study == null || study.isDeleted()) {
			RandimiException e = exceptionService.notAcceptableMissingStudy(studyApiId);
			return new ResponseEntity<>(e.getMessage(), e.getHttpStatusCode());
		}
		StringBuilder str = new StringBuilder("[");
		String prefix = "";
		for (Stratum stratum : study.getStratums()) {
			str.append(prefix);
			prefix = ",";
			str.append("\"").append(stratum.getApiId()).append("\"");
		}
		str.append("]");

		auditService.createAuditEntryReadStudy(study.getId());

		return new ResponseEntity<>(str.toString(), HttpStatus.OK);
	}

	/**
	 * This method expects a study ID as long and returns either:
	 * - a 400 Bad Request if the study ID has no matching study.
	 * - a 500 Internal Server Error if the resulting object cannot be converted to JSON.
	 * - a 200 OK with the JSON representation of a {@link StrataInfoResponseV1} which contains a list of {@link StrataInfoResponseV1.Definition} objects.
	 * <p>
	 * Each {@link StrataInfoResponseV1.Definition} has a type
	 * ({@link StratumType} which is either
	 * {@link StratumType#ENUM} or
	 * {@link StratumType#INTERVAL}).
	 * <p>
	 * If the type is {@link StratumType#ENUM}, the definition is of type
	 * {@link StrataInfoResponseV1.FactorDefinition} and has an attribute
	 * {@link StrataInfoResponseV1.FactorDefinition#getValues()}
	 * which returns a list of possible options for the stratum.
	 * <p>
	 * The result is returned in the response body.
	 *
	 * @param studyApiId studyId as {@link String}.
	 * @return {@link ResponseEntity} with value mapping as described above.
	 */
	@RequestMapping(value = {"/getStudyStrataInfo","/getStudyInfo"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	@Operation(description = "Get information about the strata (names, types, possible values) present in the provided study.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains a list of stratum definitions.",
						 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
						                    schema = @Schema(implementation = StrataInfoResponseV1.class))),
			@ApiResponse(responseCode = "400",
			             description = "Bad Request. Caused by missing or malformed parameters."),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API."),
			@ApiResponse(responseCode = "406",
			             description = "Not Acceptable. The service cannot execute the request due to internal constraints such as missing randomization lists."),
			@ApiResponse(responseCode = "500",
			             description = "Internal Server Error. Something went wrong in the server.")
	})
	public ResponseEntity<String> getStudyStrataInfo(
			@Parameter(description = "API ID of the study", required = true)
			@RequestParam(value = "studyId") final String studyApiId,
			@Deprecated Locale locale) {
		if (locale != null) {
			LocaleContextHolder.setLocale(locale);
		}

		Study study = studyRepository.findByApiId(studyApiId).orElse(null);
		if (study == null || study.isDeleted()) {
			RandimiException e = exceptionService.notAcceptableMissingStudy(studyApiId);
			return new ResponseEntity<>(e.getMessage(), e.getHttpStatusCode());
		}
		List<StrataInfoResponseV1.Definition> stratums = new ArrayList<>();
		for (Stratum stratum : study.getStratums()) {
			switch (stratum.getStratumType()) {
				case ENUM: {
					List<String> values = new ArrayList<>();
					stratum.getStratumParts().stream().map(partBase -> (StratumPartEnumeration) partBase)
							.forEach(partEnum -> values.add(partEnum.getApiId()));
					stratums.add(new StrataInfoResponseV1.FactorDefinition(stratum.getName(), stratum.getApiId(), values));
				}
				break;

//				case INTERVAL: {
//					List<RandimiResponse.IntervalPart> values = new ArrayList<>();
//					stratum.getStratumParts().stream().map(base -> (StratumPartInterval) base)
//							.forEach(part -> values.add(new RandimiResponse.IntervalPart(part.getIntervalBegin(), part.getIntervalEnd())));
//					stratums.add(new RandimiResponse.IntervalDefinition(stratum.getName(), values));
//				}
			}
		}
		StrataInfoResponseV1 response = new StrataInfoResponseV1(stratums);

		auditService.createAuditEntryReadStudy(study.getId());

		try {
			return new ResponseEntity<>(new ObjectMapper().writeValueAsString(response), HttpStatus.OK);
		} catch (JsonProcessingException e) {
			return new ResponseEntity<>("Error mapping JSON: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	//---------------------//
	//    POST-Requests    //
	//---------------------//

	/**
	 * Randomize a patient by assigning it a study arm.
	 * <p>
	 * The request must follow the format defined by {@link RandomizePatientRequestBodyV1}, meaning it expects a JSON object in the
	 * request body which contains an attribute "studyId" of type long, which identifies the study, as well as an
	 * attribute "location" of type  string, an attribute "pseudonym" of type string and an attribute "studyStrataParams" which
	 * represents a map of further attributes for the randomization.
	 * <p>
	 * The result which is returned in the responses body consists of the display name of the study arm which the
	 * patient is assigned to.
	 *
	 * @param randomizationRequest request
	 * @return response.
	 */
	@RequestMapping(value = {"/randomizePatient", "/assignStudyArm"},
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionSiteApiId(authentication, #randomizationRequest.studyApiId, #randomizationRequest.locationApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).CREATE_SUBJECT)")
	@Operation(description = "Assign a subject to a study arm.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains the name of the assigned study arm.",
			             content = @Content(mediaType = MediaType.APPLICATION_JSON_UTF8_VALUE,
			                                examples = {@ExampleObject(value = "Intervention")})
			),
			@ApiResponse(responseCode = "400",
			             description = "Bad Request. Caused by missing or malformed parameters."),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API."),
			@ApiResponse(responseCode = "406",
			             description = "Not Acceptable. The service cannot execute the request due to internal constraints such as full study."),
			@ApiResponse(responseCode = "409",
			             description = "Conflict. Subject already assigned to a study arm of this study."),
			@ApiResponse(responseCode = "500",
			             description = "Internal Server Error. Something went wrong in the server.")
	})
	@Deprecated
	public ResponseEntity<String> randomizePatient(
			@Parameter(description = "Randomization request containing information about the subject.", required = true)
			@RequestBody RandomizePatientRequestBodyV1 randomizationRequest) {
		try {
			StudyArm assignedArm = randomizationService.assignSubjectToStudyArm(randomizationRequest).getStudyArm();
			return new ResponseEntity<>(assignedArm.getGuiName(), HttpStatus.OK);
		} catch (RandimiException e) {
			LOGGER.log(Level.INFO, "Error while randomizing subject.", e);
			return new ResponseEntity<>(e.getMessage(), e.getHttpStatusCode());
		}
	}

	/**
	 * API endpoint to fetch the study arm of an already randomized subject.
	 *
	 * @deprecated As of API-Version 2.0, this method has been replaced by {@link APIControllerV2#fetchParticipantAssignment(String, String, String)}.
	 *
	 * @param studyApiId id of the study
	 * @param location location
	 * @param pseudonym pseudonym of the subject
	 * @return On success: Status 200 + gui name of the study arm
	 * On failure: Error code + error message
	 */
	@RequestMapping(value = {"/fetchStudyArm", "/fetchAssignment"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionSiteApiId(authentication, #studyApiId, #location, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_SUBJECT)")
	@Operation(summary = "Fetch the study arm of an already assigned subject.",
	           description = "Will return the GUI name of the study arm the subject got assigned to.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains the name of the assigned study arm.",
			             content = @Content(mediaType = MediaType.APPLICATION_JSON_UTF8_VALUE,
			                                examples = {
					                                @ExampleObject(value = "Intervention")
			                                })),
			@ApiResponse(responseCode = "400",
			             description = "Bad Request. Caused by missing or malformed parameters."),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API."),
			@ApiResponse(responseCode = "406",
			             description = "Not Acceptable. The service cannot execute the request due to internal constraints such as full study."),
			@ApiResponse(responseCode = "500",
			             description = "Internal Server Error. Something went wrong in the server.")
	})
	@Deprecated
	public ResponseEntity<String> fetchParticipantAssignment(
			@Parameter(description = "API ID of the study the subject is assigned to", required = true,
			           example = "6") @RequestParam(name = "studyId") String studyApiId,
			@Parameter(description = "Identifier of the institution that did the randomization", required = true,
			           example = "Münster") @RequestParam String location,
			@Parameter(description = "Pseudonym of the subject", required = true, example = "MXMSTRMNN123")
			@RequestParam String pseudonym
	) {
		try {
			String studyArm = doFetchParticipantAssignment(studyApiId, location, pseudonym);
			return new ResponseEntity<>(studyArm, HttpStatus.OK);
		} catch (RandimiException e) {
			return new ResponseEntity<>(e.getMessage(), e.getHttpStatusCode());
		}
	}

	private String doFetchParticipantAssignment(String studyApiId, String location, String pseudonym)
			throws RandimiException {
		if (studyApiId == null)
			throw exceptionService.missingParameterStudyId();
		if (location == null)
			throw exceptionService.missingParameterLocation();

		final Optional<Study> study = studyRepository.findByApiId(studyApiId);
		if (study.isEmpty() || study.get().isDeleted())
			throw exceptionService.notAcceptableMissingStudy(studyApiId);
		final long studyId = study.get().getId();

		final Optional<Site> site = siteRepository.findByStudyApiIdAndApiId(studyApiId, location);
		if (site.isEmpty())
			throw exceptionService.notAcceptableMissingLocation(location, studyApiId);
		final long siteId = site.get().getId();

		final Optional<Subject> entry = subjectRepository.findFirstByPseudonymAndSiteIdAndSubjectListStudyId(pseudonym, siteId, studyId);
		if (entry.isEmpty())
			throw exceptionService.notAcceptableMissingEntry(studyApiId, pseudonym, location);

		auditService.createAuditEntryReadSubjects(studyId);

		return entry.get().getStudyArm().getGuiName();
	}

}
