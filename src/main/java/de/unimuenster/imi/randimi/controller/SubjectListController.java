package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.subject.DeleteSubjectDTO;
import de.unimuenster.imi.randimi.dto.subject.EditSubjectPseudonymDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectListDTO;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.mapping.subject.SubjectListMapper;
import de.unimuenster.imi.randimi.model.enumeration.*;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectListRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.service.AuditService;
import de.unimuenster.imi.randimi.service.ExportService;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to handle all requests related to subject lists.
 *
 * @author Daniel Preciado-Marquez
 */
@Controller
@RequestMapping(value = "/studies/{studyApiId}/subject-lists")
public class SubjectListController {

	protected static final String DELETE_SUBJECT_DTO_KEY = "deleteSubjectDTO";
	protected static final String EDIT_SUBJECT_PSEUDONYM_DTO_KEY = "editSubjectPseudonymDTO";

	private final AclEntryRepository aclEntryRepository;
	private final StudyRepository studyRepository;
	private final SubjectListRepository subjectListRepository;
	private final SubjectRepository subjectRepository;

	private final StudyMapper studyMapper;
	private final SubjectListMapper subjectListMapper;

	private final AuditService auditService;
	private final ExportService exportService;
	private final MessageService messageService;
	private final StratumCodeService stratumCodeService;

	@Autowired
	public SubjectListController(
			final AclEntryRepository aclEntryRepository,
			final StudyRepository studyRepository,
			final SubjectListRepository subjectListRepository,
			final SubjectRepository subjectRepository,
			final StudyMapper studyMapper,
			final SubjectListMapper subjectListMapper,
			final AuditService auditService,
			final ExportService exportService,
			final MessageService messageService,
			final StratumCodeService stratumCodeService
	) {
		this.aclEntryRepository = aclEntryRepository;
		this.studyRepository = studyRepository;
		this.subjectListRepository = subjectListRepository;
		this.subjectRepository = subjectRepository;

		this.studyMapper = studyMapper;
		this.subjectListMapper = subjectListMapper;

		this.auditService = auditService;
		this.exportService = exportService;
		this.messageService = messageService;
		this.stratumCodeService = stratumCodeService;
	}

	/**
	 * Returns the page for displaying all subject lists of the study.
	 * Lists where the user does not have permissions are filtered.
	 *
	 * @param studyApiId The API ID of the study.
	 * @param model Model injected by Spring.
	 * @param redirectAttributes RedirectAttributes injected by Spring.
	 * @param currentUserDetails User of the request.
	 * @return Name of the view.
	 */
	@GetMapping(value = "")
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	public String listSubjectLists(
			@PathVariable("studyApiId") final String studyApiId,
			final Model model,
			final RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal final MyUserDetails currentUserDetails
	) {
		// Get the study
		final var requestedStudyOptional = studyRepository.findByApiId(studyApiId);
		if (requestedStudyOptional.isEmpty()) {
			redirectAttributes.addAttribute("error", messageService.getMessage("study.error.studyNotExist"));
			return "redirect:/studies";
		}

		final var requestedStudy = requestedStudyOptional.get();
		if (messageService.checkStudyStatus(List.of(StudyStatus.DELETED), requestedStudy, redirectAttributes)) {
			return "redirect:/studies";
		}

		final var requestedStudyDTO = studyMapper.toStudyDTO(requestedStudy);

		// Get the current user
		final var currentUser = currentUserDetails.getUser();

		// Get subject lists
		final List<SubjectListDTO> subjectListDTOs = new ArrayList<>();
		for (final SubjectList subjectListModel : requestedStudy.getSubjectLists()) {
			subjectListDTOs.add(subjectListMapper.toSimpleSubjectListDTO(subjectListModel));
		}

		// Filter subject lists if stratified by site
		if (!currentUser.hasUserRole(UserRoles.ROLE_ADMIN)) {
			if (requestedStudy.isStratifiedBySite()) {
				final boolean hasStudyWideReadSubjectPermissions = aclEntryRepository.hasPermission(currentUser,
				                                                                                    requestedStudy,
				                                                                                    PermissionType.READ_SUBJECT);
				if (!hasStudyWideReadSubjectPermissions) {
					final Set<String> readSubjectsSiteNames = requestedStudy.getSites().stream()
					                                                        .filter(site -> aclEntryRepository.hasPermission(
							                                                        currentUser, site,
							                                                        PermissionType.READ_SUBJECT))
					                                                        .map(Site::getGuiName)
					                                                        .collect(Collectors.toSet());

					subjectListDTOs.removeIf(subjectList -> !readSubjectsSiteNames.contains(
							stratumCodeService.getSiteDTO(subjectList.getStratumParts()).get().getGuiName()));
				}
			}

			requestedStudyDTO.getSites().removeIf(
					site -> !aclEntryRepository.hasPermission(currentUser, site, PermissionType.READ_SUBJECT));
		}

		// Collect capacities
		final List<Pair<Long, Integer>> capacities = new ArrayList<>();
		for (final SubjectList subjectList : requestedStudy.getSubjectLists()) {
			if (subjectListDTOs.stream().anyMatch(list -> list.getId() == subjectList.getId())) {
				capacities.add(Pair.of(subjectRepository.countBlockingSubjectInSubjectList(subjectList.getId()),
				                       stratumCodeService.getCapacity(subjectList)));
			}
		}

		// Create AuditEntry
		auditService.createAuditEntryReadSubjects(requestedStudy.getId());

		// Attributes for displaying the data
		model.addAttribute("capacities", capacities);
		model.addAttribute("subjectLists", subjectListDTOs);
		model.addAttribute("study", requestedStudyDTO);

		return "/subjects/list";
	}

	/**
	 * Returns the page for displaying the subject list of the given ID.
	 * Subjects which the user does not have permissions for are filtered.
	 *
	 * @param studyApiId The API ID of the study.
	 * @param subjectListId The ID of the subject list.
	 * @param model Model injected by Spring.
	 * @param redirectAttributes RedirectAttributes injected by Spring.
	 * @param currentUserDetails User of the request.
	 * @return Name of the view.
	 */
	@GetMapping(value = "/{subjectListId}")
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)" +
	              "AND @customPermissionEvaluator.hasPermissionSubjectList(authentication, #subjectListId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_SUBJECT)")
	public String viewSubjectList(@PathVariable final String studyApiId,
	                              @PathVariable final Long subjectListId,
	                              final Model model,
	                              final RedirectAttributes redirectAttributes,
	                              @AuthenticationPrincipal final MyUserDetails currentUserDetails) {
		// Get the study
		final var requestedStudyOptional = studyRepository.findByApiId(studyApiId);
		if (requestedStudyOptional.isEmpty()) {
			redirectAttributes.addAttribute("error", messageService.getMessage("study.error.studyNotExist"));
			return "redirect:/studies";
		}

		final var requestedStudy = requestedStudyOptional.get();
		if (messageService.checkStudyStatus(List.of(StudyStatus.DELETED), requestedStudy, redirectAttributes)) {
			return "redirect:/studies";
		}

		final StudyDTO requestedStudyDTO = studyMapper.toStudyDTO(requestedStudy);

		// Get the subject list
		final var requestedSubjectList = subjectListRepository.findById(subjectListId);
		if (requestedSubjectList.isEmpty()) {
			redirectAttributes.addAttribute("error", messageService.getMessage("subjectLists.view.error.notExists"));
			return "redirect:/studies/" + studyApiId + "/subject-lists";
		}
		final var subjectList = requestedSubjectList.get();
		final var subjectListDTO = subjectListMapper.toSubjectListDTO(subjectList);

		// Get the current user
		final var currentUser = currentUserDetails.getUser();

		// Filter out subjects for which the user has no permissions
		if (!currentUser.hasUserRole(UserRoles.ROLE_ADMIN)) {
			final boolean hasStudyWideReadSubjectPermissions = aclEntryRepository.hasPermission(currentUser,
			                                                                                    requestedStudy,
			                                                                                    PermissionType.READ_SUBJECT);

			if (!hasStudyWideReadSubjectPermissions) {
				final Set<String> readSubjectsSiteNames = requestedStudy.getSites().stream()
				                                                        .filter(site -> aclEntryRepository.hasPermission(currentUser, site, PermissionType.READ_SUBJECT))
				                                                        .map(Site::getGuiName)
				                                                        .collect(Collectors.toSet());

				subjectListDTO.getSubjectEntries()
				              .removeIf(subject -> !readSubjectsSiteNames.contains(subject.getLocation()));
			}
		}

		// Create AuditEntry
		auditService.createAuditEntryReadSubjects(requestedStudy.getId());

		// Attributes for subject deletion
		final DeleteSubjectDTO deleteSubjectDTO = new DeleteSubjectDTO();
		deleteSubjectDTO.setStudyId(requestedStudy.getId());
		deleteSubjectDTO.setStudyApiId(studyApiId);
		deleteSubjectDTO.setSubjectListId(subjectListId);

		model.addAttribute(DELETE_SUBJECT_DTO_KEY, deleteSubjectDTO);
		model.addAttribute("reasonTypes",
		                   AuditReasonType.getMembersForGroup(AuditReasonType.AuditReasonTarget.DELETE_SUBJECT));

		// Attributes for editing subject pseudonym
		final EditSubjectPseudonymDTO editSubjectPseudonymDTO = new EditSubjectPseudonymDTO();
		editSubjectPseudonymDTO.setStudyId(requestedStudy.getId());
		editSubjectPseudonymDTO.setStudyApiId(studyApiId);
		editSubjectPseudonymDTO.setSubjectListId(subjectListId);

		model.addAttribute(EDIT_SUBJECT_PSEUDONYM_DTO_KEY, editSubjectPseudonymDTO);
		model.addAttribute("editPseudonymReasonTypes",
		                   AuditReasonType.getMembersForGroup(AuditReasonType.AuditReasonTarget.UPDATE_SUBJECT));

		// Attributes for displaying the data
		model.addAttribute("study", requestedStudyDTO);
		model.addAttribute("subjectList", subjectListDTO);

		final long numberRows = subjectRepository.countBySubjectListIdAndStatusNot(subjectListId,
		                                                                           SubjectStatus.PRE_GENERATED);
		final long numberHiddenRows = numberRows - subjectListDTO.getSubjectEntries().size();
		if (numberHiddenRows > 0) {
			model.addAttribute("info", messageService.getMessage("subjects.view.hiddenRows", numberHiddenRows));
		}

		return "/subjects/view";
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET, produces = {"text/csv", "application/zip"})
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	public void downloadSubjectLists(
			@PathVariable final String studyApiId,
			@RequestParam(value = "splitFiles", required = false) final String splitFiles,
			@RequestParam(value = "includeApiIds", defaultValue = "false") final boolean includeApiIds,
			@RequestParam(value = "format", required = true) final ExportFileType format,
			@RequestParam(value = "delimiter", required = true) final Delimiter delimiter,
			@RequestParam(value = "status", required = false) final Set<SubjectStatus> status,
			@RequestParam(value = "sites", required = false) final Set<String> sites,
			@RequestParam final MultiValueMap<String, String> strata,
			@AuthenticationPrincipal final MyUserDetails currentUserDetails,
			final HttpServletResponse response
	) throws IOException {
		// Get the study
		final var requestedStudyOptional = studyRepository.findByApiId(studyApiId);
		if (requestedStudyOptional.isEmpty()) {
			return;
		}
		final Study requestedStudy = requestedStudyOptional.get();

		// Get the current user
		final var currentUser = currentUserDetails.getUser();

		exportService.exportSubjectLists(currentUser, requestedStudy, requestedStudy.getSubjectLists(), status, sites,
		                                 strata, splitFiles != null, includeApiIds, format, delimiter, response);
	}

	@RequestMapping(value = "/{subjectListId}/download", method = RequestMethod.GET,
	                produces = {"text/csv", "application/zip"})
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)" +
	              "AND @customPermissionEvaluator.hasPermissionSubjectList(authentication, #subjectListId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_SUBJECT)")
	public void downloadSubjectList(
			@PathVariable final String studyApiId,
			@PathVariable final Long subjectListId,
			@RequestParam(value = "format", required = true) final ExportFileType format,
			@RequestParam(value = "delimiter", required = true) final Delimiter delimiter,
			@RequestParam(value = "includeApiIds", defaultValue = "false") final boolean includeApiIds,
			@RequestParam(value = "status", required = false) final Set<SubjectStatus> status,
			@RequestParam(value = "sites", required = false) final Set<String> sites,
			@RequestParam final MultiValueMap<String, String> strata,
			@AuthenticationPrincipal final MyUserDetails currentUserDetails,
			final HttpServletResponse response
	) throws IOException {
		// Get the study
		final var requestedStudyOptional = studyRepository.findByApiId(studyApiId);
		if (requestedStudyOptional.isEmpty()) {
			return;
		}
		final Study requestedStudy = requestedStudyOptional.get();

		// Get the subject list
		final var requestedSubjectList = subjectListRepository.findById(subjectListId);
		if (requestedSubjectList.isEmpty()) {
			return;
		}
		final SubjectList subjectList = requestedSubjectList.get();

		// Get the current user
		final var currentUser = currentUserDetails.getUser();

		exportService.exportSubjectLists(currentUser, requestedStudy, List.of(subjectList), status, sites, strata,
		                                 false, includeApiIds, format, delimiter, response);
	}

}
