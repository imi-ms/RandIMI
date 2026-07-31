package de.unimuenster.imi.randimi.controller.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unimuenster.imi.randimi.dto.study.statistics.SubjectSeriesDTO;
import de.unimuenster.imi.randimi.dto.study.statistics.SubjectSeriesParameterDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectEntryDTO;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.mapping.study.stratum.StratumMapper;
import de.unimuenster.imi.randimi.mapping.subject.SubjectEntryMapper;
import de.unimuenster.imi.randimi.model.api.*;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectListRepository;
import de.unimuenster.imi.randimi.service.*;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * Handler of the API of RandIMI.
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
 * @author <a href="mailto:tobias.brix@uni-muenster.de">Tobias Brix</a>
 * @author <a href="mailto:tobiashardt@uni-muenster.de">Tobias Hardt</a>
 * @author <a href="mailto:paul.schaub@uni-muenster.de">Paul Schaub</a>
 * @author <a href="mailto:daniel.preciado-marquez@uni-muenster.de">Daniel Preciado-Marquez</a>
 */
@RestController //equals @Controller + @ResponseBody
@RequestMapping(value = "/api/v2") //each URL of the API begins with /api/...
@Tag(name = "/api/v2", description = "Newest version of RandIMIs API.")
@CrossOrigin(origins = "*") // TODO: Restrict to more sane range.
public class APIControllerV2 {

	/**
	 * Used for logging.
	 */
	private static final Logger LOGGER = LogManager.getLogger(APIControllerV2.class);

	/**
	 * Current version of the API.
	 * Is increased manually.
	 */
	public static final String API_VERSION = "2.0";

	private final ObjectMapper objectMapper;

	private final StudyRepository studyRepository;
	private final SubjectRepository subjectRepository;
	private final SubjectListRepository subjectListRepository;

	private final StratumMapper stratumMapper;
	private final StudyMapper studyMapper;
	private final SubjectEntryMapper subjectMapper;

	private final AuditService auditService;
	private final ExportService exportService;
	private final MessageService messageService;
	private final RandomizationService randomizationService;
	private final RandimiExceptionFactoryService exceptionService;
	private final StatisticsService statisticsService;

	@Autowired
	public APIControllerV2(final ObjectMapper objectMapper, final StudyRepository studyRepository,
	                       final SubjectRepository subjectRepository, final SubjectListRepository subjectListRepository,
	                       final StratumMapper stratumMapper, final StudyMapper studyMapper,
	                       final SubjectEntryMapper subjectMapper, final AuditService auditService,
	                       final ExportService exportService, final MessageService messageService,
	                       final RandomizationService randomizationService,
	                       final RandimiExceptionFactoryService randimiExceptionFactoryService,
	                       final StatisticsService statisticsService) {
		this.objectMapper = objectMapper;
		this.studyRepository = studyRepository;
		this.subjectRepository = subjectRepository;
		this.subjectListRepository = subjectListRepository;
		this.stratumMapper = stratumMapper;
		this.studyMapper = studyMapper;
		this.subjectMapper = subjectMapper;
		this.auditService = auditService;
		this.exportService = exportService;
		this.messageService = messageService;
		this.randomizationService = randomizationService;
		this.exceptionService = randimiExceptionFactoryService;
		this.statisticsService = statisticsService;
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
	@RequestMapping(value = "/version", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get the current version number of the API (eg. '2.0').")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains the version number.",
						 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
											schema = @Schema(implementation = String.class),
											examples = {
													@ExampleObject(value = "2.0")
											})),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. The user has no permissions to use the API.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class)))
	})
	public ResponseEntity<String> getAPIVersion() {
		return new ResponseEntity<>(API_VERSION, HttpStatus.OK);
	}

	@Operation(summary = "Get the structure of the study.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains a study resource describing the structure of the study.",
			             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                schema = @Schema(implementation = StudyResource.class))),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. The user has no permissions to use the API or to access the study.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "406",
			             description = "Not Acceptable. No study with the given API ID could be found.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class)))
	})
	@RequestMapping(value = {"/study/{studyApiId}"}, method = RequestMethod.GET,
	                produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	public StudyResource getStudy(
			@PathVariable @Parameter(description = "API ID of the study", required = true) final String studyApiId
	) throws RandimiException {
		final Study study = studyRepository.findByApiId(studyApiId).orElse(null);
		if (study == null) {
			throw exceptionService.notAcceptableMissingStudy(studyApiId);
		}

		auditService.createAuditEntryReadStudy(study.getId());

		return studyMapper.toStudyResource(study);
	}

	@RequestMapping(value = {"/study/{studyApiId}/stratum"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	@Operation(summary = "Get a list of all stratum API IDs present in the provided study.",
	           description = "This method can be used to quickly determine, which strata are present in a study.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains a list of stratum API IDs.",
			             content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)),
			                                examples = {
					                                @ExampleObject(value = "[\n  \"Age\",\n  \"Sex\"\n]")
			                                })),
			@ApiResponse(responseCode = "400",
			             description = "Bad Request. Caused by missing or malformed parameters.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "406",
			             description = "Not Acceptable. The service cannot execute the request due to internal constraints such as missing randomization lists.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class)))
	})
	public List<String> getStudyStrataNames(
			@Parameter(description = "API ID of the study", required = true)
			@PathVariable(value = "studyApiId") String studyApiId
	) throws RandimiException {
		Study study = studyRepository.findByApiId(studyApiId).orElse(null);
		if (study == null || study.isDeleted()) {
			throw exceptionService.notAcceptableMissingStudy(studyApiId);
		}

		auditService.createAuditEntryReadStudy(study.getId());
		return study.getStratums().stream().map(Stratum::getApiId).toList();
	}

	/**
	 * This method expects a study ID as long and returns either:
	 * - a 400 Bad Request if the study ID has no matching study.
	 * - a 500 Internal Server Error if the resulting object cannot be converted to JSON.
	 * - a 200 OK with the JSON representation of a {@link StrataInfoResponseV2
	 *} which contains a list of {@link StrataInfoResponseV2
	 *.Definition} objects.
	 * <p>
	 * Each {@link StrataInfoResponseV2
	 *.Definition} has a type
	 * ({@link StratumType} which is either
	 * {@link StratumType#ENUM} or
	 * {@link StratumType#INTERVAL}).
	 * <p>
	 * If the type is {@link StratumType#ENUM}, the definition is of type
	 * {@link StrataInfoResponseV2
	 *.FactorDefinition} and has an attribute
	 * {@link StrataInfoResponseV2
	 *.FactorDefinition#getValues()}
	 * which returns a list of possible options for the stratum.
	 * <p>
	 * The result is returned in the response body.
	 *
	 * @param studyApiId studyApiId as {@link String}.
	 * @return {@link ResponseEntity} with value mapping as described above.
	 */
	@RequestMapping(value = {"/study/{studyApiId}/stratum/definition"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	@Operation(summary = "Get information about the strata (names, types, possible values) present in the provided study.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains a list of stratum definitions.",
						 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
						                    schema = @Schema(implementation = StrataInfoResponseV2.class))),
			@ApiResponse(responseCode = "400",
			             description = "Bad Request. Caused by missing or malformed parameters.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "406",
			             description = "Not Acceptable. The service cannot execute the request due to internal constraints such as missing randomization lists.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500",
			             description = "Internal Server Error. Something went wrong in the server.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class)))
	})
	public StrataInfoResponseV2 getStudyStrataInfo(
			@Parameter(description = "API ID of the study", required = true)
			@PathVariable(value = "studyApiId") String studyApiId
	) throws RandimiException {
		Study study = studyRepository.findByApiId(studyApiId).orElse(null);
		if (study == null || study.isDeleted()) {
			throw exceptionService.notAcceptableMissingStudy(studyApiId);
		}

		List<StrataInfoResponseV2.Definition> stratums = new ArrayList<>();
		for (Stratum stratum : study.getStratums()) {
			if (stratum.getStratumType().equals(StratumType.ENUM)) {
				stratums.add(stratumMapper.toStratumResource(stratum));
			}
		}
		auditService.createAuditEntryReadStudy(study.getId());

		return new StrataInfoResponseV2(stratums);
	}

	/**
	 * Exports subject lists.
	 *
	 * @param studyApiId API ID of the study from which the subject lists should be exported.
	 * @param subjectListId Optional ID of the subject list from which the subjects should be exported.
	 * @param requestParams Parameter for configuring the export.
	 * @param currentUserDetails User of the request.
	 * @param response Response of the request.
	 * @return Response containing the exported lists in the configured format or an error response.
	 */
	@RequestMapping(value = { "/study/{studyApiId}/subject-lists", "/study/{studyApiId}/subject-lists/{subjectListId}"},
	                method = RequestMethod.GET,
	                produces = {MediaType.APPLICATION_JSON_VALUE, "text/csv", "application/zip"})

	@PreAuthorize(
			"(@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId,T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)" +
			"AND @customPermissionEvaluator.hasPermissionSubjectList(authentication, #studyApiId, #subjectListId,T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_SUBJECT, true)" +
			"AND @customPermissionEvaluator.hasPermissionSiteApiIds(authentication, #studyApiId, #requestParams.sites,T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_SUBJECT))")
	@Operation(summary = "Exports subjects.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains the subject lists in the requested format.",
			             content = {
					             @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
					                      array = @ArraySchema(schema = @Schema(implementation = SubjectEntryDTO.class))),
					             @Content(mediaType = "text/csv",
					                      array = @ArraySchema(schema = @Schema(implementation = SubjectEntryDTO.class)),
					                      examples = @ExampleObject(
							                      "orderNumber,pseudonym,studyArmName,status,creationTimestamp,location,gender,age\r\n" +
							                      "1,UKM_048,\"Control Group\",ACTIVE,\"2000-12-24 18:00:00\",Münster,m,18-30\r\n")),
					             @Content(mediaType = "application/zip"),
			             }),
			@ApiResponse(responseCode = "400",
			             description = "Bad Request. Caused by missing or malformed parameters.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500",
			             description = "Internal Server Error. Something went wrong in the server.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class)))
	})
	public ResponseEntity<Object> getSubjectList(
			@Parameter(description = "API ID of the study", required = true)
			@PathVariable(value = "studyApiId") String studyApiId,
			@Parameter(description = "Numerical ID of the subject-list", required = false)
			@PathVariable(value = "subjectListId", required = false) final Long subjectListId,
			@ParameterObject @Valid final SubjectExportRequest requestParams,
			@AuthenticationPrincipal final MyUserDetails currentUserDetails,
			final HttpServletResponse response
	) throws RandimiException, IOException {

		// Get the study
		final var requestedStudyOptional = studyRepository.findByApiId(studyApiId);
		if (requestedStudyOptional.isEmpty() || requestedStudyOptional.get().isDeleted()) {
			throw exceptionService.notAcceptableMissingStudy(studyApiId);
		}
		final Study requestedStudy = requestedStudyOptional.get();

		// Get the current user
		final var currentUser = currentUserDetails.getUser();

		// Get subject list
		List<SubjectList> subjectLists;
		if (subjectListId == null) {
			subjectLists = requestedStudy.getSubjectLists();
		} else {
			final var requestedSubjectList = subjectListRepository.findById(subjectListId);
			if (requestedSubjectList.isEmpty()) {
				final RandimiException e = exceptionService.notAcceptableMissingSubjectList(studyApiId, subjectListId);
				return new ResponseEntity<>(e.getMessage(), e.getHttpStatusCode());
			}
			subjectLists = List.of(requestedSubjectList.get());
		}

		// Check site api Ids
		for (final String siteApiId : requestParams.getSites()) {
			if (requestedStudy.getSiteByApiId(siteApiId) == null) {
				throw exceptionService.notAcceptableMissingSite(siteApiId, studyApiId);
			}
		}

		// Transform strata
		Map<String, Set<String>> strata;
		try {
			strata = objectMapper.readValue(requestParams.getStrata(), new TypeReference<>() { });
		} catch (JsonProcessingException e) {
			throw exceptionService.malformedParameterStratum("strata");
		}
		Set<String> invalidStrataApiIds = new HashSet<>();
		Map<String, Set<String> > invalidStrataParts = new HashMap<>();
		for (final String stratumApiId : strata.keySet()) {
			final Optional<Stratum> stratum = requestedStudy.getStratumByApiId(stratumApiId);
			if (stratum.isEmpty()) {
				invalidStrataApiIds.add(stratumApiId);
			} else {
				for (final String strataParam : strata.get(stratumApiId)) {
					if (stratum.get().getStratumPartByValue(strataParam) == null) {
						if (!invalidStrataParts.containsKey(stratumApiId)) {
							invalidStrataParts.put(stratumApiId, new HashSet<>());
						}
						invalidStrataParts.get(stratumApiId).add(strataParam);
					}
				}
			}
		}

		final Set<String> missingStrata = new HashSet<>();
		for (final Stratum stratum : requestedStudy.getStratums()) {
			if (!strata.containsKey(stratum.getApiId())) {
				missingStrata.add(stratum.getApiId());
			}
		}
		if (!invalidStrataApiIds.isEmpty() || !invalidStrataParts.isEmpty() || !missingStrata.isEmpty()) {
			final List<String> errors = new ArrayList<>();
			for (final String strataApiId : invalidStrataApiIds) {
				errors.add(messageService.getMessage("exception.stratumNotFound", strataApiId));
			}
			for (final String strataApiId : invalidStrataParts.keySet()) {
				for (final String partApiId : invalidStrataParts.get(strataApiId)) {
					errors.add(messageService.getMessage("exception.noMatchingStratumPartFound", partApiId, strataApiId));
				}
			}
			for (final String missingStrataApiId : missingStrata) {
				errors.add(messageService.getMessage("exception.missingStratum", missingStrataApiId));
			}
			throw new RandimiException.BadRequest(RandimiException.NOT_ACCEPTABLE, String.join(", ", errors));
		}

		exportService.exportSubjectLists(currentUser, requestedStudy, subjectLists, requestParams.getStatus(),
		                                 requestParams.getSites(), strata, requestParams.getSplitFiles(),
		                                 requestParams.getIncludeApiIds(), requestParams.getFormat(),
		                                 requestParams.getDelimiter(), response);

		return ResponseEntity.ok().build();
	}

	//---------------------//
	//    POST-Requests    //
	//---------------------//

	/**
	 * Randomize a patient by assigning it a study arm.
	 * <p>
	 * The request must follow the format defined by {@link RandomizePatientRequestBodyV2}, meaning it expects a JSON object in the
	 * request body which contains an attribute "studyId" of type long, which identifies the study, as well as an
	 * attribute "location" of type  string, an attribute "pseudonym" of type string and an attribute "studyStrataParams" which
	 * represents a map of further attributes for the randomization.
	 * <p>
	 * The result which is returned in the response body consists of the display name of the study arm which the
	 * patient is assigned to.
	 *
	 * @param randomizationRequest request
	 * @return response.
	 */
	@RequestMapping(value = {"/study/{studyApiId}/subject"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionSiteApiId(authentication, #studyApiId, #randomizationRequest.siteApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).CREATE_SUBJECT)")
	@Operation(summary = "Assign a subject to a study arm.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains the name of the assigned study arm.",
			             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                schema = @Schema(implementation = RandomizationResponse.class))),
			@ApiResponse(responseCode = "400",
			             description = "Bad Request. Caused by missing or malformed parameters.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "406",
			             description = "Not Acceptable. The service cannot execute the request due to internal constraints such as full study.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409",
			             description = "Conflict. Subject already assigned to a study arm of this study.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500",
			             description = "Internal Server Error. Something went wrong in the server.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class)))
	})
	public RandomizationResponse randomizePatient(
			@Parameter(description = "API ID of the study", required = true)
			@PathVariable(value = "studyApiId") String studyApiId,
			@Parameter(description = "Randomization request containing information about the subject.", required = true)
			@RequestBody RandomizePatientRequestBodyV2 randomizationRequest
	) throws RandimiException {
		final Subject subject = randomizationService.assignSubjectToStudyArm(studyApiId, randomizationRequest);
		final SubjectResource resource = subjectMapper.toSubjectResource(subject);
		return new RandomizationResponse(resource);
	}

	/**
	 * API endpoint to fetch the study arm of an already randomized subject.
	 * @param studyApiId id of the study
	 * @param siteApiId siteApiId of the subject's site
	 * @param pseudonym pseudonym of the subject
	 * @return On success: Status 200 + gui name of the study arm
	 * On failure: Error code + error message
	 */
	@RequestMapping(value = {"/study/{studyApiId}/subject"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionSiteApiId(authentication, #studyApiId, #siteApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_SUBJECT)")
	@Operation(summary = "Fetch the study arm of an already assigned subject.",
	           description = "Will return the GUI name of the study arm the subject got assigned to.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
			             description = "OK. Response contains the name of the assigned study arm.",
			             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
											schema = @Schema(implementation = RandomizationResponse.class))),
			@ApiResponse(responseCode = "400",
			             description = "Bad Request. Caused by missing or malformed parameters.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403",
			             description = "Forbidden. Possibly the user does not have access to the study, or has no permissions to use the API.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "406",
			             description = "Not Acceptable. The service cannot execute the request due to internal constraints such as full study.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500",
			             description = "Internal Server Error. Something went wrong in the server.",
			             content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			                                schema = @Schema(implementation = ErrorResponse.class)))
	})
	public RandomizationResponse fetchParticipantAssignment(
			@Parameter(description = "API ID of the study the subject is assigned to", required = true,
			           example = "6")
			@PathVariable String studyApiId,
			@Parameter(description = "API ID of the institution that did the randomization", required = true,
			           example = "Münster42") @RequestParam String siteApiId,
			@Parameter(description = "Pseudonym of the subject", required = true, example = "MXMSTRMNN123")
			@RequestParam String pseudonym
	) throws RandimiException {
		final Subject subject = doFetchParticipantAssignment(studyApiId, siteApiId, pseudonym);
		final SubjectResource resource = subjectMapper.toSubjectResource(subject);
		return new RandomizationResponse(resource);
	}

	private Subject doFetchParticipantAssignment(String studyApiId, String siteApiId, String pseudonym)
			throws RandimiException {
		if (studyApiId == null || studyApiId.isBlank()) {
			throw exceptionService.missingParameterStudyId();
		}
		Study study = studyRepository.findByApiId(studyApiId).orElse(null);
		if (study == null || study.isDeleted()) {
			throw exceptionService.notAcceptableMissingStudy(studyApiId);
		}
		final Optional<Subject> entry = subjectRepository.findFirstByPseudonymAndSiteApiIdAndSubjectListStudyId(
				pseudonym, siteApiId, study.getId());
		if (entry.isEmpty()) {
			throw exceptionService.notAcceptableMissingEntry(studyApiId, pseudonym, siteApiId);
		}

		auditService.createAuditEntryReadSubjects(study.getId());

		return entry.get();
	}

	@Operation(summary = "Get statistics about the progress over the course of the study.",
	           description = "Get statistics about the progress over the course of the study.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200",
		             description = "OK. Response contains the statistics.",
		             content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
		                                schema = @Schema(implementation = SubjectSeriesDTO.class))),
	})
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_REPORT)")
	@GetMapping(value = "/study/{studyApiId}/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
	public SubjectSeriesDTO getStatistics(
			@Parameter(description = "API ID of the study.", required = true, example = "6")
			@PathVariable final String studyApiId,
			@ParameterObject final SubjectSeriesParameterDTO parameters,
			@RequestParam final Map<String, String> requestParameter
	) throws RandimiException {
		if (studyApiId == null || studyApiId.isBlank()) {
			throw exceptionService.missingParameterStudyId();
		}
		final Study study = studyRepository.findByApiId(studyApiId).orElse(null);
		if (study == null || study.isDeleted()) {
			throw exceptionService.notAcceptableMissingStudy(studyApiId);
		}

		// Set strata parameters
		if (requestParameter != null && !requestParameter.isEmpty()) {
			final Map<String, String> strataParameters = new HashMap<>();
			for (final Map.Entry<String, String> entry : requestParameter.entrySet()) {
				if (entry.getKey().startsWith("strataParameters.")) {
					strataParameters.put(entry.getKey().substring("strataParameters.".length()), entry.getValue());
				}
			}
			if (!strataParameters.isEmpty()) {
				parameters.setStrataParameters(strataParameters);
			}
		}

		return statisticsService.createSubjectSeriesStatistics(study, parameters);
	}
}
