package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.subject.DeleteSubjectDTO;
import de.unimuenster.imi.randimi.dto.subject.EditSubjectPseudonymDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.study.stratum.*;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectListRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandomizationService;
import de.unimuenster.imi.randimi.service.SubjectService;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.validator.subject.SubjectDTOValidator;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * Class to handle all patient-related requests.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Controller
@RequestMapping(value = "/subjects")
public class SubjectController {

	private static final Logger LOGGER = LogManager.getLogger(SubjectController.class);

	protected static final String DELETE_SUBJECT_DTO_KEY = "deleteSubjectDTO";
	protected static final String EDIT_SUBJECT_PSEUDONYM_DTO_KEY = "editSubjectPseudonymDTO";
	private static final String SUBJECT_KEY = "subject";

	private final AclEntryRepository aclEntryRepository;
	private final StudyRepository studyRepository;
	private final SubjectRepository subjectRepository;
	private final SubjectListRepository subjectListRepository;

	private final SubjectDTOValidator subjectDTOValidator;

	private final StudyMapper studyMapper;

	private final MessageService messageService;
	private final RandomizationService randomizationService;
	private final SubjectService subjectService;

	public SubjectController(
			final AclEntryRepository aclEntryRepository,
			final StudyRepository studyRepository,
			final SubjectRepository subjectRepository,
			final SubjectListRepository subjectListRepository,
			final SubjectDTOValidator subjectDTOValidator,
			final StudyMapper studyMapper,
			final MessageService messageService,
			final RandomizationService randomizationService,
			final SubjectService subjectService
	) {
		this.aclEntryRepository = aclEntryRepository;
		this.studyRepository = studyRepository;
		this.subjectRepository = subjectRepository;
		this.subjectListRepository = subjectListRepository;

		this.subjectDTOValidator = subjectDTOValidator;

		this.studyMapper = studyMapper;

		this.messageService = messageService;
		this.randomizationService = randomizationService;
		this.subjectService = subjectService;
	}

	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)" +
	              "AND (#subjectListId == null OR @customPermissionEvaluator.hasPermissionSubjectList(authentication, #subjectListId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).CREATE_SUBJECT))")
	public String add(
			@RequestParam(value = "id", required = true) final Long studyId,
			@RequestParam(value = "subjectListId", required = false) final Long subjectListId,
			final Model model,
			final RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal final MyUserDetails currentUserDetails
	) {
		// Get the study
		final var requestedStudyOptional = studyRepository.findById(studyId);
		if (requestedStudyOptional.isEmpty()) {
			redirectAttributes.addAttribute("error", messageService.getMessage("study.error.studyNotExist"));
			return "redirect:/studies";
		}
		final StudyDTO requestedStudyDto = studyMapper.toStudyDTO(requestedStudyOptional.get());

		// Get the subject list
		SubjectList subjectList = null;
		if (subjectListId != null) {
			// Get the subject list
			final var requestedSubjectList = subjectListRepository.findById(subjectListId);
			if (requestedSubjectList.isEmpty()) {
				redirectAttributes.addAttribute("error", messageService.getMessage("subjectLists.view.error.notExists"));
				return "redirect:/studies/" + requestedStudyOptional.get().getApiId() + "/subject-lists";
			}
			subjectList = requestedSubjectList.get();
		}

		// Get current user
		final RandimiUser currentUser = currentUserDetails.getUser();
		final boolean hasStudyWideCreateSubjectPermissions = aclEntryRepository.hasPermission(currentUser,
		                                                                                      requestedStudyDto,
		                                                                                      PermissionType.CREATE_SUBJECT);

		if (!currentUser.hasUserRole(UserRoles.ROLE_ADMIN) && !hasStudyWideCreateSubjectPermissions) {
			requestedStudyDto.getSites().removeIf(
					site -> !aclEntryRepository.hasPermission(currentUser, site, PermissionType.CREATE_SUBJECT));
		}


		// Get or create a new subject dto
		SubjectDTO subjectDTO = (SubjectDTO) model.getAttribute(SUBJECT_KEY);
		if (subjectDTO == null) {
			subjectDTO = new SubjectDTO();
			subjectDTO.setEnumeratedStratums(new String[requestedStudyDto.getEnumeratedStratums().size()]);
			subjectDTO.setIntervalStratums(new Float[requestedStudyDto.getIntervalStratums().size()]);

			if (subjectListId != null) {

				// Fill subject with data from the subject list
				int enumIndex = 0;
				int intervalIndex = 0;
				for (final var part : subjectList.getStratumParts()) {
					if (part instanceof StratumPartSite && requestedStudyDto.isStratifyBySite()) {
						final Site site = ((StratumPartSite) part).getSite();
						subjectDTO.setSiteId(site.getId());
						subjectDTO.setSiteApiId(site.getApiId());
					} else if (part instanceof StratumPartEnumeration) {
						subjectDTO.getEnumeratedStratums()[enumIndex] = part.getPartKey();
						enumIndex += 1;
					} else if (part instanceof StratumPartInterval) {
						// TODO what should we do here
						intervalIndex += 1;
					}
				}
			}

		}

		model.addAttribute("strataLocked", subjectListId != null);
		model.addAttribute("study", requestedStudyDto);
		model.addAttribute(SUBJECT_KEY, subjectDTO);

		return "/subjects/add";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST, params = "action=cancel")
	@Transactional
	public String addCancel(
			@RequestParam(value = "subjectListId", required = false) final Long subjectListId,
			@Valid @ModelAttribute("subject") SubjectDTO subjectDTO
	) {
		if (subjectListId != null) {
			return "redirect:/studies/" + subjectDTO.getStudyApiId() + "/subject-lists/" + subjectListId;
		} else {
			return "redirect:/studies/" + subjectDTO.getStudyApiId() + "/subject-lists";
		}
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionSiteId(authentication, #subjectDTO.studyId, #subjectDTO.siteId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).CREATE_SUBJECT)")
	@Transactional
	public String add(@RequestParam String action,
	                  @RequestParam(value = "subjectListId", required = false) final Long subjectListId,
	                  @Valid @ModelAttribute("subject") SubjectDTO subjectDTO,
	                  BindingResult result,
	                  RedirectAttributes ra) {
		// Validate the DTO and possibly return errors
		subjectDTOValidator.validate(subjectDTO, result);

		if (result.hasErrors()) {
			ra.addAttribute("id", subjectDTO.getStudyId());
			ra.addAttribute("subjectListId", subjectListId);
			ra.addFlashAttribute(SUBJECT_KEY, subjectDTO);
			ra.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + SUBJECT_KEY, result);
			ra.addFlashAttribute("error", messageService.getMessage("general.error.invalidForm"));
			return "redirect:/subjects/add";
		}

		try {
			StudyArm assignedStudyArm = randomizationService.assignSubjectToStudyArm(subjectDTO).getStudyArm();
			ra.addFlashAttribute("success",
			                     messageService.getMessage("study.success.studyArm", assignedStudyArm.getGuiName()));
		} catch (RandimiException e) {
			LOGGER.error(e);
			ra.addFlashAttribute("error", e.getMessage());
		}

		if (subjectListId != null) {
			return "redirect:/studies/" + subjectDTO.getStudyApiId() +"/subject-lists/" + subjectListId;
		} else {
			return "redirect:/studies/" + subjectDTO.getStudyApiId() +"/subject-lists";
		}
	}

	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionSubjectList(authentication, #deleteSubjectDTO.subjectListId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).DELETE_SUBJECT)")
	public String remove(@Valid @ModelAttribute(DELETE_SUBJECT_DTO_KEY) final DeleteSubjectDTO deleteSubjectDTO,
	                     final BindingResult result,
	                     final RedirectAttributes ra) {
		if (result.hasErrors()) {
			return redirectDueToValidationError(result, ra, "subjects.view.deleteSubject.validate.error",
			                                    deleteSubjectDTO.getStudyApiId(), deleteSubjectDTO.getSubjectListId());
		}

		long studyId = deleteSubjectDTO.getStudyId();
		long subjectId = deleteSubjectDTO.getSubjectId();

		Optional<Study> studyOptional = studyRepository.findById(studyId);
		Optional<Subject> subjectOptional = subjectRepository.findById(subjectId);

		if (studyOptional.isEmpty()) {
			messageService.addError(ra, "study.error.studyNotExist");
			return "redirect:/studies";
		}

		if (subjectOptional.isEmpty()) {
			messageService.addError(ra, "subjects.error.subjectNotExist");
			return "redirect:/studies/" + deleteSubjectDTO.getStudyApiId() + "/subject-lists/" +
			       deleteSubjectDTO.getSubjectListId();
		}

		try {
			subjectService.deleteSubject(subjectOptional.get(), deleteSubjectDTO.isRelease(),
			                             deleteSubjectDTO.getChangeReason().getChangeReason());

			String subjectPseudonym = subjectOptional.get().getPseudonym();
			messageService.addSuccess(ra, "subjects.view.deleteSubject.success", subjectPseudonym);
		} catch (RandimiException e) {
			messageService.addError(ra, e);
		}

		return "redirect:/studies/" + deleteSubjectDTO.getStudyApiId() + "/subject-lists/" +
		       deleteSubjectDTO.getSubjectListId();
	}

	@RequestMapping(value = "/edit-pseudonym", method = RequestMethod.POST)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionSubjectList(authentication, #editSubjectPseudonymDTO.subjectListId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_SUBJECT)")
	public String editPseudonym(@Valid @ModelAttribute(EDIT_SUBJECT_PSEUDONYM_DTO_KEY) final EditSubjectPseudonymDTO editSubjectPseudonymDTO,
	                           final BindingResult result,
	                           final RedirectAttributes ra) {
		if (result.hasErrors()) {
			return redirectDueToValidationError(result, ra, "subjects.view.editPseudonym.validate.error",
			                                    editSubjectPseudonymDTO.getStudyApiId(),
			                                    editSubjectPseudonymDTO.getSubjectListId());
		}

		long studyId = editSubjectPseudonymDTO.getStudyId();
		long subjectId = editSubjectPseudonymDTO.getSubjectId();

		Optional<Study> studyOptional = studyRepository.findById(studyId);
		Optional<Subject> subjectOptional = subjectRepository.findById(subjectId);

		if (studyOptional.isEmpty()) {
			messageService.addError(ra, "study.error.studyNotExist");
			return "redirect:/studies";
		}

		if (subjectOptional.isEmpty()) {
			messageService.addError(ra, "subjects.error.subjectNotExist");
			return "redirect:/studies/" + editSubjectPseudonymDTO.getStudyApiId() + "/subject-lists/" +
			       editSubjectPseudonymDTO.getSubjectListId();
		}

		try {
			subjectService.updatePseudonym(subjectOptional.get(), editSubjectPseudonymDTO.getPseudonym(),
			                               editSubjectPseudonymDTO.getChangeReason().getChangeReason());
			messageService.addSuccess(ra, "subjects.view.editPseudonym.success",
			                          editSubjectPseudonymDTO.getPseudonym());
		} catch (final RandimiException e) {
			messageService.addError(ra, e);
		}

		return "redirect:/studies/" + editSubjectPseudonymDTO.getStudyApiId() + "/subject-lists/" +
		       editSubjectPseudonymDTO.getSubjectListId();
	}

	private String redirectDueToValidationError(BindingResult result, RedirectAttributes ra, String messageCode,
	                                            String studyApiId, long subjectListId) {
		String errorString = messageService.getMessage(messageCode);
		errorString += " " + result.getAllErrors().get(0).getDefaultMessage();
		for (int i = 1; i < result.getAllErrors().size(); i++)
			errorString += ", " + result.getAllErrors().get(i).getDefaultMessage();
		ra.addFlashAttribute("error", errorString);
		return "redirect:/studies/" + studyApiId + "/subject-lists/" + subjectListId;
	}

}
