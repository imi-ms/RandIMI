package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.dto.AuditEntryDTO;
import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDTO;
import de.unimuenster.imi.randimi.dto.study.*;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.dto.study.user.AddStudyUsersDTO;
import de.unimuenster.imi.randimi.dto.study.user.RemoveStudyUserDTO;
import de.unimuenster.imi.randimi.dto.study.user.StudyUserDTO;
import de.unimuenster.imi.randimi.dto.study.user.StudyUsersDTO;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.mapping.AuditEntryMapper;
import de.unimuenster.imi.randimi.mapping.settings.PseudonymRegexMapper;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.mapping.study.user.StudyUserMapper;
import de.unimuenster.imi.randimi.mapping.study.user.StudyUsersMapper;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.enumeration.*;
import de.unimuenster.imi.randimi.model.settings.PseudonymRegex;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartSite;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.service.*;
import de.unimuenster.imi.randimi.controller.helper.StringUtils;
import de.unimuenster.imi.randimi.exceptions.ControllerException.CSVFileParseException;
import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.AuditEntryRepository;
import de.unimuenster.imi.randimi.repository.settings.SettingsRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.repository.user.AclObjectIdentityRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.validator.study.StudyDTOValidator;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class to handle all study related requests.
 *
 * @author Tobias Brix
 * @author <a href="mailto:tobiashardt@uni-muenster.de">Tobias Hardt</a>
 * @author Daniel Preciado-Marquez
 *
 */
@Controller
@RequestMapping(value = "/studies")
public class StudyController {

	private static final Logger LOGGER = LogManager.getLogger(StudyController.class);

	public static final String STRATUM_NAME_LOCATION = "location";

	private static final String ADD_STUDY_USERS_DTO_KEY = "addStudyUsersDTO";
	protected static final String ARCHIVE_STUDY_DTO_KEY = "archiveStudyDTO";
	private static final String CHANGE_REASON_KEY = "changeReason";
	protected static final String DELETE_STUDY_DTO_KEY = "deleteStudyDTO";
	private static final String REMOVE_STUDY_USER_DTO_KEY = "removeStudyUserDTO";
	protected static final String STUDY_KEY = "study";

	public static final String STUDY_USERS_KEY = "studyUsers";

	private final AclClassRepository aclClassRepository;
	private final AclEntryRepository aclEntryRepository;
	private final AclObjectIdentityRepository aclObjectIdentityRepository;
	private final AuditEntryRepository auditEntryRepository;
	private final SettingsRepository settingsRepository;
	private final StudyRepository studyRepository;
	private final SubjectRepository subjectRepository;
	private final RandimiUserRepository userRepository;

	private final StudyDTOValidator studyDTOValidator;

	private final AuditEntryMapper auditEntryMapper;
	private final PseudonymRegexMapper pseudonymRegexMapper;
	private final StudyMapper studyMapper;
	private final StudyUserMapper studyUserMapper;
	private final StudyUsersMapper studyUsersMapper;

	private final AuditService auditService;
	private final MessageService messageService;
	private final StatisticsService statisticsService;
	private final StratumCodeService stratumCodeService;
	private final StringUtils stringUtils;
	private final StudyService studyService;
	private final StudyUtilityService studyUtilityService;

	public StudyController(final StudyRepository studyRepository, final MessageService messageService,
	                       final StudyUtilityService studyUtilityService, final SubjectRepository subjectRepository,
	                       final StratumCodeService stratumCodeService, final SettingsRepository settingsRepository,
	                       final RandimiUserRepository userRepository, final StringUtils stringUtils,
	                       final StudyService studyService, final AclEntryRepository aclEntryRepository,
	                       final AclClassRepository aclClassRepository,
	                       final AclObjectIdentityRepository aclObjectIdentityRepository,
	                       final AuditEntryRepository auditEntryRepository, final StudyDTOValidator studyDTOValidator,
	                       final StudyUserMapper studyUserMapper, final AuditService auditService,
	                       final StudyMapper studyMapper, final AuditEntryMapper auditEntryMapper,
	                       final PseudonymRegexMapper pseudonymRegexMapper, final StudyUsersMapper studyUsersMapper,
	                       final StatisticsService statisticsService) {
		this.aclClassRepository = aclClassRepository;
		this.aclEntryRepository = aclEntryRepository;
		this.aclObjectIdentityRepository = aclObjectIdentityRepository;
		this.auditEntryRepository = auditEntryRepository;
		this.settingsRepository = settingsRepository;
		this.studyRepository = studyRepository;
		this.studyUtilityService = studyUtilityService;
		this.subjectRepository = subjectRepository;
		this.userRepository = userRepository;

		this.studyDTOValidator = studyDTOValidator;

		this.auditEntryMapper = auditEntryMapper;
		this.pseudonymRegexMapper = pseudonymRegexMapper;
		this.studyMapper = studyMapper;
		this.studyUsersMapper = studyUsersMapper;
		this.studyUserMapper = studyUserMapper;

		this.auditService = auditService;
		this.messageService = messageService;
		this.statisticsService = statisticsService;
		this.stringUtils = stringUtils;
		this.studyService = studyService;
		this.stratumCodeService = stratumCodeService;
	}

	@RequestMapping(value = {"", "/", "/list", "/archived"}, method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String list(Model model) {

		List<StudyDTO> allStudyDTOs = new ArrayList<>();
		List<StudyDTO> archivedStudyDTOs = new ArrayList<>();
		for (Study study : studyRepository.findAll()) {
			if (study.getStatus() == StudyStatus.ARCHIVED || study.getStatus() == StudyStatus.DELETED) {
				archivedStudyDTOs.add(studyMapper.toStudyDTO(study));
			} else {
				allStudyDTOs.add(studyMapper.toStudyDTO(study));
			}
		}

		model.addAttribute("maxLong", Long.MAX_VALUE);
		model.addAttribute("allStudies", allStudyDTOs);
		model.addAttribute("archivedStudies", archivedStudyDTOs);
		model.addAttribute("reasonTypes",
		                   AuditReasonType.getMembersForGroup(AuditReasonType.AuditReasonTarget.DELETE_STUDY));
		model.addAttribute(ARCHIVE_STUDY_DTO_KEY, new ArchiveStudyDTO());
		model.addAttribute(DELETE_STUDY_DTO_KEY, new DeleteStudyDTO());
		return "/studies/list";
	}


	@RequestMapping(value = "/create", method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole(T(de.unimuenster.imi.randimi.model.enumeration.UserRoles).ROLE_ADMIN, T(de.unimuenster.imi.randimi.model.enumeration.UserRoles).ROLE_LOCAL_MANAGER, T(de.unimuenster.imi.randimi.model.enumeration.UserRoles).ROLE_STUDY_MANAGER)")
	public String create(final Model model) {
		// Check if the model already contains a study from a redirect
		StudyDTO requestedStudyDTO = (StudyDTO) model.getAttribute(STUDY_KEY);

		if (requestedStudyDTO == null) {
			final Study requestedStudy = new Study();
			requestedStudy.setStatus(StudyStatus.INEXISTENT);

			requestedStudyDTO = prepareStudyDTO(requestedStudy);
			model.addAttribute(STUDY_KEY, requestedStudyDTO);
		}

		return openEdit(model, requestedStudyDTO);
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY)")
	public String edit(
			@RequestParam(value = "id") final String studyApiId,
			final Model model,
			final RedirectAttributes redirectAttributes
	) {
		// Check if the model already contains a study from a redirect
		StudyDTO requestedStudyDTO = (StudyDTO) model.getAttribute(STUDY_KEY);

		if (requestedStudyDTO == null) {

			final var reqeustedStudyOptional = studyRepository.findByApiId(studyApiId);
			if (reqeustedStudyOptional.isEmpty()) {
				messageService.addError(redirectAttributes, "study.error.studyNotExist");
				return "redirect:/studies";
			}

			final Study requestedStudy = reqeustedStudyOptional.get();

			if (messageService.checkStudyStatus(List.of(StudyStatus.ARCHIVED, StudyStatus.DELETED), requestedStudy,
			                                    redirectAttributes)) {
				return "redirect:/studies";
			}

			// Create AuditEntry
			auditService.createAuditEntryReadStudy(requestedStudy.getId());

			requestedStudyDTO = prepareStudyDTO(requestedStudy);

			final ChangeReason changeReason = new ChangeReason();
			changeReason.setOldDto(auditService.getOldDto(requestedStudyDTO));

			model.addAttribute(CHANGE_REASON_KEY, changeReason);
			model.addAttribute(STUDY_KEY, requestedStudyDTO);
		}

		return openEdit(model, requestedStudyDTO);
	}

	private StudyDTO prepareStudyDTO(final Study study) {
		var studyDTO = studyMapper.toStudyDTO(study);

		for (SiteDTO siteDTO : studyDTO.getSites()) {
			boolean empty = subjectRepository.countBySubjectListStudyIdAndSiteIdAndStatusAndPseudonymNotNull(studyDTO.getId(), siteDTO.getId(), SubjectStatus.ACTIVE) == 0;
			siteDTO.setEmpty(empty);
		}

		return studyDTO;
	}

	private String openEdit(final Model model, final StudyDTO requestedStudyDTO) {
		if (requestedStudyDTO.getSites().isEmpty()) {
			requestedStudyDTO.getSites().add(new SiteDTO());
		}
		if (requestedStudyDTO.getStudyArms().isEmpty()) {
			requestedStudyDTO.getStudyArms().add(new StudyArmDTO());
			requestedStudyDTO.getStudyArms().add(new StudyArmDTO());
		}

		final List<PseudonymRegex> pseudonymRegexList = settingsRepository.getCurrentSettings().getPseudonymRegexList();
		final List<PseudonymRegexDTO> pseudonymRegexDTOS = new ArrayList<>();
		for (final PseudonymRegex pseudonymRegex : pseudonymRegexList)
			pseudonymRegexDTOS.add(pseudonymRegexMapper.toPseudonymRegexDTO(pseudonymRegex));

		model.addAttribute("imbalanceFunctionList", ImbalanceFunction.values());
		model.addAttribute("randomizationAlgorithmList", RandomizationAlgorithm.values());
		model.addAttribute("randomizationAlgorithm", requestedStudyDTO.getRandomizationAlgorithm());
		model.addAttribute("pseudonymHandlingList", PseudonymHandling.values());
		model.addAttribute("pseudonymRegexList", pseudonymRegexDTOS);
		model.addAttribute("permissionBundleList", PermissionBundle.values());
		model.addAttribute("reasonTypes", AuditReasonType.getMembersForGroup(AuditReasonType.AuditReasonTarget.UPDATE_STUDY));

		return "/studies/edit";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("hasAnyRole(T(de.unimuenster.imi.randimi.model.enumeration.UserRoles).ROLE_ADMIN, T(de.unimuenster.imi.randimi.model.enumeration.UserRoles).ROLE_LOCAL_MANAGER, T(de.unimuenster.imi.randimi.model.enumeration.UserRoles).ROLE_STUDY_MANAGER)")
	public String create(@RequestParam final String action,
	                     @Valid @ModelAttribute("study") final StudyDTO studyDTO,
	                     final BindingResult studyResult,
	                     final RedirectAttributes redirectAttributes) {
		return doEdit(action, studyDTO, studyResult, null, null, redirectAttributes);
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyDto(authentication, #studyDTO, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY)")
	public String edit(@RequestParam final String action,
	                   @Valid @ModelAttribute("study") final StudyDTO studyDTO,
	                   final BindingResult studyResult,
	                   @Valid @ModelAttribute("changeReason") final ChangeReason changeReason,
	                   final BindingResult changeReasonResult,
	                   final RedirectAttributes redirectAttributes) {
		return doEdit(action, studyDTO, studyResult, changeReason, changeReasonResult, redirectAttributes);
	}

	/**
	 * Saves the given study dto of a new or existing study.
	 * If the change reason is null, the study will be treated as a new study.
	 */
	private String doEdit(final String action,
	                      final StudyDTO studyDTO,
	                      final BindingResult studyResult,
	                      @Nullable final ChangeReason changeReason,
	                      @Nullable final BindingResult changeReasonResult,
	                      final RedirectAttributes redirectAttributes) {
		if (action.equalsIgnoreCase("cancel")) {
			return "redirect:/studies";
		}

		boolean inactive = isNewStudyOrNotActivated(studyDTO);

		// Remove empty study arms, strata and stratum parts if it could be modified
		if (inactive) {
			studyDTO.getStudyArms().removeIf(StudyArmDTO::isFormEmpty);

			for (StratumDTO stratumDTO : studyDTO.getEnumeratedStratums()) {
				stratumDTO.getStratumParts().removeIf(StratumPartBaseDTO::isEmptyEnum);
			}
			studyDTO.getEnumeratedStratums().removeIf(StratumDTO::isFormEmpty);

			for (StratumDTO stratumDTO : studyDTO.getIntervalStratums()) {
				stratumDTO.getStratumParts().removeIf(StratumPartBaseDTO::isEmptyInterval);
			}
			studyDTO.getIntervalStratums().removeIf(StratumDTO::isFormEmpty);
		}

		// Remove deleted sites
		studyDTO.getSites().removeIf(SiteDTO::isFormEmpty);

		// Set default values and order numbers
		int orderNumber = 0;
		for (SiteDTO site : studyDTO.getSites()) {
			site.setOrderNumber(orderNumber);
			orderNumber += 1;

			if (site.getSeed() == null) {
				site.setSeed(System.currentTimeMillis());
			}
			if (site.getPseudonymRegex() == null || site.getPseudonymRegex().isBlank()) {
				site.setPseudonymRegex(".*");
			}
		}

		orderNumber = 0;
		for (StudyArmDTO studyArm : studyDTO.getStudyArms()) {
			studyArm.setOrderNumber(orderNumber);
			orderNumber += 1;

			if (studyArm.getRatio() == null) {
				studyArm.setRatio(1);
			}
		}

		orderNumber = 0;
		for (final StratumDTO stratumDTO : studyDTO.getEnumeratedStratums()) {
			stratumDTO.setOrderNumber(orderNumber);
			orderNumber += 1;

			int partOrderNumber = 0;
			for (final StratumPartBaseDTO stratumPartBaseDTO : stratumDTO.getStratumParts()) {
				stratumPartBaseDTO.setOrderNumber(partOrderNumber);
				partOrderNumber += 1;
			}
		}

		// Simplify ratios
		studyUtilityService.simplifyStudyArmRatios(studyDTO);

		// Validate
		studyDTOValidator.validate(studyDTO, studyResult);
		if (studyResult.hasErrors() || (changeReasonResult != null && changeReasonResult.hasErrors())) {
			return backToEditDueToError(studyDTO, studyDTO.getOriginalApiId(), changeReason, redirectAttributes, studyResult, changeReasonResult);
		}

		if (studyDTO.isStratifyBySite()) {
			// location stratum is missing after every edit
			final StratumDTO locationStratum = createAndGetLocationStratum(studyDTO);
			addLocationStratumPart(studyDTO.getSites(), locationStratum);
		}

		// Get the right study or create a new one if the study does not exist
		final Study study;
		final List<Site> originalSites;

		if (changeReason == null) {
			study = studyMapper.toStudy(studyDTO, new Study());
			originalSites = new ArrayList<>();
		} else {
			final Study original = studyRepository.findById(studyDTO.getId()).get();
			originalSites = new ArrayList<>(original.getSites());

			if (messageService.checkStudyStatus(List.of(StudyStatus.ARCHIVED, StudyStatus.DELETED), original, redirectAttributes)) {
				return "redirect:/studies";
			}

			final Site originalSite = original.getSites().get(0);
			final boolean previouslyStratified = original.isStratifiedBySite();
			study = studyMapper.toStudy(studyDTO, original);

			if(original.isInTestMode()) {
				try {
					studyService.changeToTestMode(study);
				} catch (RandimiException e) {
					LOGGER.error("The study with ID" + study.getId() + " could not be switched into test mode!", e);
					messageService.addError(redirectAttributes, "study.error.test", study.getGuiName());
					return "redirect:/studies";
				}
			} else if (study.isActive() && studyDTO.isStratifyBySite()) {

				if (previouslyStratified) {
					cleanOrphanSubjectLists(study);
				} else {
					for (final SubjectList list : study.getSubjectLists()) {
						list.addStratumPart(stratumCodeService.getLocationStratumPart(originalSite).get());
					}
				}

				try {
					studyRepository.save(study);
					studyService.addMissingSubjectLists(study);
				} catch (RandimiException exception) {
					redirectAttributes.addAttribute("error", messageService.getMessage("exception.unknownAlgorithm"));
					return "redirect:/studies";
				}
			}
		}

		// Persist or update the study
		studyRepository.save(study);

		// Set missing fields in studyDTO for audit entry
		studyDTO.setStatus(study.getStatus());
		studyDTO.setActivationDate(study.getActivationDate());

		RandimiUser currentUser = ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
		                                                                .getPrincipal()).getUser();
		currentUser = userRepository.findById(currentUser.getId()).get();

		// Update ACL identities for the study
		if (changeReason == null) {

			// Persist the acl object identity if the study is new
			final AclObjectIdentity aclObjectIdentity = new AclObjectIdentity(study.getId(), Boolean.TRUE,
			                                                                  aclClassRepository.findFirstByClassNameOrSynonym(
					                                                                  Study.class.getName(),
					                                                                  StudyDTO.class.getName()),
			                                                                  currentUser.getAclSid(), null);
			aclObjectIdentityRepository.save(aclObjectIdentity);

			// Set initial permissions
			if (!currentUser.hasUserRole(UserRoles.ROLE_ADMIN)) {
				study.addAssignedUser(currentUser);

				// Iterate over all bundles for study permissions and cross-site permissions
				for (PermissionBundle permissionBundle : PermissionBundle.values()) {
					for (PermissionType permissionType : PermissionBundle.getPermissionTypes(permissionBundle))
						applyStudyPermission(permissionType, currentUser.getAclSid(), aclObjectIdentity);
				}
			}
		}

		// Update ACL identities for sites
		final AclClass siteAclClass = aclClassRepository.findFirstByClassNameOrSynonym(Site.class.getName(),
		                                                                               SiteDTO.class.getName());

		final List<Site> currentSites = study.getSites();

		for (final Site site : currentSites) {
			final boolean isNewSite = originalSites.stream().noneMatch(a -> Objects.equals(a.getId(), site.getId()));
			if (isNewSite) {
				AclObjectIdentity siteAclObjectIdentity = new AclObjectIdentity(site.getId(), Boolean.TRUE,
				                                                                siteAclClass, currentUser.getAclSid(),
				                                                                null);
				aclObjectIdentityRepository.save(siteAclObjectIdentity);
			}
		}

		for (final Site site : originalSites) {
			final boolean isDeletedSite = currentSites.stream().noneMatch(a -> Objects.equals(a.getId(), site.getId()));
			if (isDeletedSite) {
				final AclObjectIdentity siteAclObjectIdentity = aclObjectIdentityRepository
						.findFirstByObjectIdClassAndObjectIdIdentity(siteAclClass, site.getId());
				aclEntryRepository.deleteByAclObjectIdentity(siteAclObjectIdentity);
				aclObjectIdentityRepository.delete(siteAclObjectIdentity);
			}
		}

		// Create audit entry
		if (changeReason == null) {
			auditService.createAuditEntryCreateStudy(studyDTO, study.getId());
		} else {
			auditService.createAuditEntryUpdateStudy(studyDTO, study.getId(), changeReason);
		}

		redirectAttributes.addFlashAttribute("success", messageService.getMessage("general.success.saved"));

		if (Objects.equals(action, "saveAndEditUsers")) {
			redirectAttributes.addAttribute("id", study.getId());
			return "redirect:/studies/editUsers";
		} else {
			return "redirect:/studies";
		}
	}

	@GetMapping(value = "/generateApiId",
	            produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public String generateApiId() {
		return studyService.generateApiId();
	}

	private StratumDTO createAndGetLocationStratum(final StudyDTO studyDTO) {
		final StratumDTO locationStratum = new StratumDTO();
		locationStratum.setGuiName(STRATUM_NAME_LOCATION);
		locationStratum.setApiId(STRATUM_NAME_LOCATION);
		locationStratum.setUseApiId(false);
		locationStratum.setOrderNumber(studyDTO.getEnumeratedStratums().size());
		locationStratum.setStratumType(StratumType.SITE);
		locationStratum.setStratumParts(new ArrayList<>());

		studyDTO.setSiteStratum(locationStratum);
		return locationStratum;
	}

	private void addLocationStratumPart(final List<SiteDTO> sites, final StratumDTO locationStratum) {
		for (final SiteDTO site : sites) {
			final StratumPartBaseDTO sitePart = new StratumPartBaseDTO();
			sitePart.setOrderNumber(locationStratum.getStratumParts().size());
			sitePart.setSite(site);
			locationStratum.getStratumParts().add(sitePart);
		}
	}

	/**
	 * Removes subject lists wich site stratum part does no longer exist.
	 * @param study Study to clean.
	 */
	private void cleanOrphanSubjectLists(final Study study) {
		final List<SubjectList> orphanSubjectLists = new ArrayList<>();

		for (final SubjectList subjectList : study.getSubjectLists()) {
			final Optional<StratumPartSite> sitePart = stratumCodeService.getLocationStratumPart(
					subjectList.getStratumParts());

			if(!study.getSites().contains(sitePart.get().getSite())) {
				orphanSubjectLists.add(subjectList);
			}
		}

		study.removeAllSubjectLists(orphanSubjectLists);
	}

	private boolean isNewStudyOrNotActivated(StudyDTO studyDTO) {
		if (studyDTO.getId() == null || studyDTO.getId() == 0) {
			return true;
		}
		Optional<Study> study = studyRepository.findById(studyDTO.getId());
		return study.isPresent() && study.get().isActive();
	}

	private String backToEditDueToError(final StudyDTO studyDTO, final String originalApiId,
	                                    @Nullable final ChangeReason changeReason,
	                                    final RedirectAttributes redirectAttributes, final BindingResult studyResult,
	                                    @Nullable final BindingResult changeReasonResult) {
		return backToEditDueToError(studyDTO, originalApiId, changeReason, redirectAttributes, studyResult,
		                            changeReasonResult, messageService.getMessage("general.error.invalidForm"));
	}

	private String backToEditDueToError(final StudyDTO studyDTO, final String originalApiId,
	                                    @Nullable final ChangeReason changeReason,
	                                    final RedirectAttributes redirectAttributes, final BindingResult studyResult,
	                                    @Nullable final BindingResult changeReasonResult, final String errorMessage) {
		if (originalApiId != null && !originalApiId.isEmpty()) {
			redirectAttributes.addAttribute("id", originalApiId);
		}

		if (studyDTO.getId() != null && studyDTO.getId() != 0) {
//			redirectAttributes.addAttribute("id", studyDTO.getId());

			// Set status because it is not returned from frontend
			Optional<Study> study = studyRepository.findById(studyDTO.getId());
			study.ifPresent(value -> studyDTO.setStatus(value.getStatus()));
			study.ifPresent(value -> studyDTO.setActivationDate(value.getActivationDate()));
		}

		redirectAttributes.addFlashAttribute("error", errorMessage);
		redirectAttributes.addFlashAttribute(STUDY_KEY, studyDTO);
		redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + STUDY_KEY, studyResult);

		if (changeReason != null) {
			redirectAttributes.addFlashAttribute(CHANGE_REASON_KEY, changeReason);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + CHANGE_REASON_KEY, changeReasonResult);
			return "redirect:/studies/edit";
		} else {
			return "redirect:/studies/create";
		}
	}

//	// @RequestMapping(value = "/uploadRandomizationList", method =
//	// RequestMethod.POST)
//	@Transactional
//	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_MANAGER') OR hasPermission(#studyDTO, 'UPDATE_STUDY')")
//	public String uploadRandomizationList(@RequestParam String action,
//			@Valid @ModelAttribute("study") StudyDTO studyDTO, BindingResult result, Model model) {
//		if (action.equalsIgnoreCase("cancel")) {
//			return "redirect:/studies";
//		} else if (action.equalsIgnoreCase("back")) {
//			model.addAttribute("study", studyDTO);
//			model.addAttribute("randomizationAlgorithmList", RandomizationAlgorithm.values());
//			model.addAttribute("pseudonymHandlingList", PseudonymHandling.values());
//			model.addAttribute("pseudonymRegexList", settingsRepository.getCurrentSettings().getPseudonymRegexList());
//			model.addAttribute("permissionBundleList", PermissionBundle.values());
//			model.addAttribute("reasonTypes", AuditReasonType.getMembersForGroup(AuditReasonTarget.UPDATE_STUDY));
//			return "/studies/edit";
//		}
//
//		studyDTOValidator.validate(studyDTO, result);
//
//		if (result.hasErrors()) {
//			// Get all stratum interval codes for the given studyDTO
//			List<String> stratumIntervalCodes = StringUtils.getStratumIntervalCodes(studyDTO.getEnumeratedStratums(),
//					studyDTO.getIntervalStratums());
//			model.addAttribute("stratumIntervalCodes", stratumIntervalCodes);
//			model.addAttribute("study", studyDTO);
//			return "/studies/uploadRandomizationList";
//		}
//
//		Study study = studyMapper.toStudy(studyDTO, new Study());
//		studyRepository.save(study);
//
//		List<SubjectList> randomizationListsFromCSV;
//
//		try {
//			randomizationListsFromCSV = this.getRandomizationListsFromCSV(studyDTO.getFile(), study,
//					StringUtils.getCSV_SEPARATOR());
//		} catch (CSVFileParseException csvException) {
//			// Remove the merged study
//			studyRepository.delete(study);
//			// If the file could not be parsed, return the error
//			result.rejectValue("file", "errormessage", messageService.getMessage(csvException.getMessage()));
//			// Get all stratum interval codes for the given studyDTO
//			List<String> stratumIntervalCodes = StringUtils.getStratumIntervalCodes(studyDTO.getEnumeratedStratums(),
//					studyDTO.getIntervalStratums());
//			model.addAttribute("stratumIntervalCodes", stratumIntervalCodes);
//			model.addAttribute("study", studyDTO);
//			return "/studies/uploadRandomizationList";
//		}
//
//		// TODO: Fix
//		// study.addAllRandomizationLists(randomizationListsFromCSV);
//		study.setActivationDate(new Timestamp(System.currentTimeMillis()));
//
//		studyRepository.save(study);
//
//		AuditEntry auditEntry = new AuditEntry();
//		auditEntry.fillAuditEntry(study, AuditType.CREATE, null);
//		auditEntryRepository.save(auditEntry);
//
//		AclObjectIdentity aclObjectIdentity;
//		RandimiUser currentUser = ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
//				.getPrincipal()).getUser();
//		if (studyDTO.getId() == 0) {
//			// Persist the acl object identity if the study is new
//			aclObjectIdentity = new AclObjectIdentity(study.getId(), Boolean.TRUE,
//					aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(), Study.class.getName()),
//					currentUser.getAclSid(), null);
//			aclObjectIdentityRepository.save(aclObjectIdentity);
//		} else {
//			aclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
//					aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(), Study.class.getName()),
//					study.getId());
//		}
//
//		// Grant the given rights on the study
//		// Remove all current rights of the study except of the current user
//		aclEntryRepository.deleteByAclObjectIdentityAndUserNot(aclObjectIdentity, currentUser);
//		return "redirect:/studies";
//	}

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	public String view(
			@RequestParam(value = "id", required = true) String studyApiId,
			Model model,
			RedirectAttributes ra
	) {
		Optional<Study> requestedStudyOptional = studyRepository.findByApiId(studyApiId);

		if (requestedStudyOptional.isEmpty()) {
			ra.addAttribute("error", messageService.getMessage("study.error.studyNotExist"));
			return "redirect:/studies";
		}

		Study requestedStudy = requestedStudyOptional.get();
		StudyDTO requestedStudyDTO = studyMapper.toStudyDTO(requestedStudy);

		// Create AuditEntry
		auditService.createAuditEntryReadStudy(requestedStudyDTO.getId());

		List<AuditEntryDTO> auditEntryDTOs = new ArrayList<>();
		for (AuditEntry entry : auditEntryRepository.findByStudyId(requestedStudy.getId())) {
			auditEntryDTOs.add(auditEntryMapper.toAuditEntryDTO(entry));
		}

		model.addAttribute("auditEntries", auditEntryDTOs);
		model.addAttribute("study", requestedStudyDTO);


		// Remove DELETE for studies that are not deleted
		final var studyAuditTypes = new ArrayList<>(AuditClass.STUDY.getValidAuditTypes());

		if (!requestedStudy.isDeleted()) {
			studyAuditTypes.remove(AuditType.DELETE);
		}

		model.addAttribute("randimiUserAuditTypes", AuditClass.RANDIMI_USER.getValidAuditTypes());
		model.addAttribute("studyAuditTypes", studyAuditTypes);
		model.addAttribute("subjectAuditTypes", AuditClass.SUBJECT.getValidAuditTypes());

		return "/studies/view";
	}

	/**
	 * Endpoint for the statistics page.
	 * Only accessible for users with READ_REPORT permission.
	 *
	 * @param studyApiId API ID of the study.
	 * @param model      Model injected by Spring.
	 * @param ra         RedirectAttributes injected by Spring.
	 * @return The name of the view.
	 */
	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_REPORT)")
	public String getStatistics(
			@RequestParam(value = "id", required = true) final String studyApiId,
			final Model model,
			final RedirectAttributes ra
	) {
		// Get the requested study
		final Optional<Study> requestedStudyOptional = studyRepository.findByApiId(studyApiId);

		if (requestedStudyOptional.isEmpty()) {
			messageService.addError(ra, "study.error.studyNotExist");
			return "redirect:/studies";
		}

		final Study requestedStudy = requestedStudyOptional.get();

		// Create AuditEntry
		auditService.createAuditEntryReadStudy(requestedStudy.getId());

		// Fill model
		final StudyDTO requestedStudyDTO = studyMapper.toStudyDTO(requestedStudy);
		model.addAttribute(STUDY_KEY, requestedStudyDTO);

		final StudyStatisticsDTO statistics = statisticsService.createStudyStatistics(requestedStudy);
		model.addAttribute("statistics", statistics);

		// Return view
		return "/studies/statistics";
	}

	/**
	 * Sets the study with the given ID into the test state.
	 * @param studyId ID of the study.
	 * @param redirectAttributes RedirectAttributes injected by Spring.
	 * @return The name of the view.
	 */
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	@Transactional
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY)")
	public String test(final @RequestParam(value = "id", required = true) Long studyId,
	                   final RedirectAttributes redirectAttributes) {
		final Optional<Study> studyOptional = studyRepository.findById(studyId);

		if (studyOptional.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", messageService.getMessage("study.error.studyNotExist"));
			return "redirect:/studies";
		}

		final Study study = studyOptional.get();

		try {
			studyService.changeToTestMode(study);
			messageService.addSuccess(redirectAttributes, "study.success.test", study.getGuiName());
		} catch (RandimiException e) {
			LOGGER.error("The study with ID" + studyId + " could not be switched into test mode!", e);
			messageService.addError(redirectAttributes, "study.error.test", study.getGuiName());
		}

		return "redirect:/studies";
	}

	@RequestMapping(value = "/activate", method = RequestMethod.GET)
	@Transactional
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY)")
	public String activate(@RequestParam(value = "id", required = true) Long studyId, Model model,
	                       RedirectAttributes ra) {
		Optional<Study> studyOptional = studyRepository.findById(studyId);

		if (studyOptional.isEmpty()) {
			return "redirect:/studies";
		}

		Study study = studyOptional.get();
		try {
			studyService.activateStudy(study);
		} catch (Exception e) {
			LOGGER.warn("The study with id '" + studyId + "' could not be activated!");
			LOGGER.warn("Exception is:\n", e);
			ra.addAttribute("error", messageService.getMessage("study.error.activationError", study.getGuiName()));
		}

		ra.addFlashAttribute("success", messageService.getMessage("study.success.activated", study.getGuiName()));
		return "redirect:/studies";
	}

	@RequestMapping(value = "/lock", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY)")
	public String lock(
			final @RequestParam(value = "id", required = true) Long studyId,
			final @RequestParam(value = "lock", required = true) boolean lock,
			final RedirectAttributes redirectAttributes
	) {
		final Optional<Study> studyOptional = studyRepository.findById(studyId);

		if (studyOptional.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", messageService.getMessage("study.error.studyNotExist"));
			return "redirect:/studies";
		}

		final Study study = studyOptional.get();

		try {
			if (lock) {
				studyService.lockStudy(study);
				messageService.addSuccess(redirectAttributes, "study.success.locked", study.getGuiName());
			} else {
				studyService.unlockStudy(study);
				messageService.addSuccess(redirectAttributes, "study.success.unlocked", study.getGuiName());
			}
		} catch (final RandimiException e) {
			LOGGER.warn("Failed to lock/unlock study", e);
			messageService.addError(redirectAttributes, e);
		}

		return "redirect:/studies";
	}

	@PostMapping(value = "/archive")
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #archiveStudyDTO.studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY)")
	public String archive(
			@Valid @ModelAttribute(ARCHIVE_STUDY_DTO_KEY) final ArchiveStudyDTO archiveStudyDTO,
			final BindingResult bindingResult,
			final RedirectAttributes redirectAttributes
	) {
		final Optional<Study> studyOptional = studyRepository.findById(archiveStudyDTO.getStudyId());

		if (studyOptional.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", messageService.getMessage("study.error.studyNotExist"));
			return "redirect:/studies";
		}

		if (bindingResult.hasErrors()) {
			messageService.addErrors(redirectAttributes, "studies.archiveStudy.error", bindingResult);
			return "redirect:/studies";
		}

		try {
			studyService.archiveStudy(studyOptional.get(), archiveStudyDTO.getRetentionPeriod());
			messageService.addSuccess(redirectAttributes, "studies.archiveStudy.success");
		} catch (final RandimiException e) {
			LOGGER.warn("Failed to archive study", e);
			messageService.addError(redirectAttributes, e);
		}

		return "redirect:/studies/archived";
	}

	@GetMapping(value = "/reactivate")
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY)")
	public String reactivate(
			@RequestParam(value = "id") final Long studyId,
			final RedirectAttributes redirectAttributes
	) {
		final Optional<Study> studyOptional = studyRepository.findById(studyId);

		if (studyOptional.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", messageService.getMessage("study.error.studyNotExist"));
			return "redirect:/studies";
		}

		try {
			studyService.reactivateStudy(studyOptional.get());
			messageService.addSuccess(redirectAttributes, "studies.reactivateStudy.success");
		} catch (final RandimiException e) {
			LOGGER.warn("Failed to reactivate study", e);
			messageService.addError(redirectAttributes, e);
		}

		return "redirect:/studies";
	}

	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #deleteStudyDTO.studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).DELETE_STUDY)")
	public String remove(@Valid @ModelAttribute(DELETE_STUDY_DTO_KEY) final DeleteStudyDTO deleteStudyDTO,
	                     final BindingResult result, final RedirectAttributes ra) {
		if (result.hasErrors()) {
			messageService.addErrors(ra, "studies.deleteStudy.validate.error", result);
			return "redirect:/studies";
		}

		try {
			studyService.deleteStudy(deleteStudyDTO);
			messageService.addSuccess(ra, "studies.deleteStudy.success");
		} catch (final RandimiException e) {
			LOGGER.warn("Failed to delete study", e);
			messageService.addError(ra, e);
		}

		return "redirect:/studies";
	}

	@RequestMapping(value = "/downloadConfiguration", method = RequestMethod.GET)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).READ_STUDY)")
	public ResponseEntity<ByteArrayResource> downloadConfiguration(
			@RequestParam(value = "id", required = true) Long studyId) {
		// If the given study does not exists
		if (studyId == null || studyRepository.findById(studyId).isEmpty()) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Location", "../studies");
			return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
		}

		// Get the study
		Study study = studyRepository.findById(studyId).get();
		byte[] data = stringUtils.getConfigurationAsCSV(study).getBytes(StandardCharsets.UTF_8);

		// The path ist the name of the downloaded file containing the study's name
		Path path = Paths
				.get(study.getGuiName().replaceAll("[\\\\/:;*?\"<>|]", "").replaceAll(" ", "_") + "_config.csv");
		ByteArrayResource resource = new ByteArrayResource(data);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + path.getFileName().toString())
				.contentLength(data.length).body(resource);
	}

	// @RequestMapping(value = "/downloadRandomizationListTemplate", method =
	// RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> downloadRandomizationListTemplate(
			@RequestParam(value = "intervalCodes", required = true) List<String> intervalCodes,
			@RequestParam(value = "studyArms", required = true) List<String> studyArms,
			@RequestParam(value = "studySize", required = true) int studySize) {
		byte[] data = stringUtils.getRandomizationListUploadTemplate(intervalCodes, studyArms, studySize)
				.getBytes(StandardCharsets.UTF_8);

		// The path ist the name of the downloaded file containing the study's name
		Path path = Paths.get("template.csv");
		ByteArrayResource resource = new ByteArrayResource(data);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + path.getFileName().toString())
				.contentLength(data.length).body(resource);
	}

	@RequestMapping(value = "/editUsers", method = RequestMethod.GET)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyApiId(authentication, #studyApiId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY_USERS)")
	public String editUsers(
			@RequestParam(value = "id", required = false) String studyApiId,
			final Model model,
			final RedirectAttributes redirectAttributes
	) {
		// Check whether there is a study to edit or not
		Optional<Study> requestedStudyOptional = Optional.empty();
		if (studyApiId != null) {
			requestedStudyOptional = studyRepository.findByApiId(studyApiId);
		}

		if (requestedStudyOptional.isEmpty()) {
			messageService.addError(redirectAttributes, "study.error.studyNotExist");
			return "redirect:/studies";
		}

		final Study requestedStudy = requestedStudyOptional.get();

		if (messageService.checkStudyStatus(List.of(StudyStatus.DELETED), requestedStudy, redirectAttributes)) {
			return "redirect:/studies";
		}

		final StudyUsersDTO studyUsersDTO;
		if (!model.containsAttribute(STUDY_USERS_KEY)) {
			studyUsersDTO = studyUsersMapper.toStudyUsersDTO(requestedStudy);
			model.addAttribute(STUDY_USERS_KEY, studyUsersDTO);
		} else {
			studyUsersDTO = (StudyUsersDTO) model.getAttribute(STUDY_USERS_KEY);
		}

		if (!model.containsAttribute(CHANGE_REASON_KEY)) {
			final ChangeReason changeReason = new ChangeReason();
			changeReason.setOldDto(auditService.getOldDto(studyUsersDTO));
			model.addAttribute(CHANGE_REASON_KEY, changeReason);
		}

		final Iterable<RandimiUser> allUsers = userRepository.findAll();
		final List<StudyUserDTO> availableUsers = new ArrayList<>();
		for (final RandimiUser randimiUser : allUsers) {
			if (randimiUser.isEnabled() && !randimiUser.hasUserRole(UserRoles.ROLE_ADMIN)
			    && studyUsersDTO.getStudyUserDTOs()
			                 .stream()
			                 .noneMatch(studyUserDTO -> studyUserDTO.getUserId().equals(randimiUser.getId()))) {
				availableUsers.add(studyUserMapper.toStudyUserDTO(requestedStudy, randimiUser));
			}
		}

		// Create AuditEntry
		auditService.createAuditEntryReadStudy(requestedStudy.getId());

		model.addAttribute(STUDY_KEY, studyMapper.toStudyDTO(requestedStudy));
		model.addAttribute(ADD_STUDY_USERS_DTO_KEY, new AddStudyUsersDTO());
		model.addAttribute(REMOVE_STUDY_USER_DTO_KEY, new RemoveStudyUserDTO());
		model.addAttribute("studyName", requestedStudy.getGuiName());
		model.addAttribute("availableUsers", availableUsers);
		model.addAttribute("studyPermissionBundleList", PermissionBundle.getStudyPermissionBundles());
		model.addAttribute("sitePermissionBundleList", PermissionBundle.getSitePermissionBundles());
		model.addAttribute("reasonTypes", AuditReasonType.getMembersForGroup(AuditReasonType.AuditReasonTarget.UPDATE_STUDY));

		return "/studies/editUsers";
	}

	@RequestMapping(value = "/editUsers", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyUsersDTO.studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY_USERS)")
	public String editUsers(
			@RequestParam final String action,
			@Valid @ModelAttribute(STUDY_USERS_KEY) final StudyUsersDTO studyUsersDTO,
			final BindingResult studyUsersDTOResult,
			@Valid @ModelAttribute(CHANGE_REASON_KEY) final ChangeReason changeReason,
			final BindingResult changeReasonResult,
			final RedirectAttributes redirectAttributes
	) {
		if (Objects.equals(action, "cancel")) {
			return "redirect:/studies";
		}

		return editUsersSave(studyUsersDTO, studyUsersDTOResult, changeReason, changeReasonResult, redirectAttributes);
	}

	@RequestMapping(value = "/editUsers", method = RequestMethod.POST, params = "editUsersAdd")
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyUsersDTO.studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY_USERS)")
	public String editUsersAdd(@Valid @ModelAttribute(STUDY_USERS_KEY) final StudyUsersDTO studyUsersDTO,
	                           final BindingResult studyUsersDTOResult,
	                           @Valid @ModelAttribute(ADD_STUDY_USERS_DTO_KEY) final AddStudyUsersDTO addStudyUsersDTO,
	                           final BindingResult addStudyUsersDTOResult,
	                           @ModelAttribute(CHANGE_REASON_KEY) final ChangeReason changeReason,
	                           final RedirectAttributes redirectAttributes) {
		if (studyUsersDTOResult.hasErrors() || addStudyUsersDTOResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(ADD_STUDY_USERS_DTO_KEY, addStudyUsersDTO);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + ADD_STUDY_USERS_DTO_KEY, addStudyUsersDTOResult);
			redirectAttributes.addFlashAttribute("error", messageService.getMessage("general.error.invalidForm"));
			return editUsersRedirectErrors(studyUsersDTO, changeReason, redirectAttributes);
		}

		final Study study = studyRepository.findById(studyUsersDTO.getStudyId()).orElse(null);
		studyUsersDTO.getStudyUserDTOs().addAll(studyUserMapper.toStudyUserDTO(study, addStudyUsersDTO.getNewUserIds()));

		return editUsersRedirect(studyUsersDTO, changeReason, redirectAttributes);
	}

	@RequestMapping(value = "/editUsers", method = RequestMethod.POST, params = "editUsersRemove")
	@PreAuthorize("@customPermissionEvaluator.hasPermissionStudyId(authentication, #studyUsersDTO.studyId, T(de.unimuenster.imi.randimi.model.enumeration.PermissionType).UPDATE_STUDY_USERS)")
	public String editUsersRemove(@Valid @ModelAttribute(STUDY_USERS_KEY) final StudyUsersDTO studyUsersDTO,
	                              final BindingResult studyUsersDTOResult,
	                              @Valid @ModelAttribute(REMOVE_STUDY_USER_DTO_KEY) final RemoveStudyUserDTO removeStudyUserDTO,
	                              final BindingResult removeStudyUserDTOResult,
	                              @ModelAttribute(CHANGE_REASON_KEY) final ChangeReason changeReason,
	                              final RedirectAttributes redirectAttributes) {
		if (studyUsersDTOResult.hasErrors() || removeStudyUserDTOResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(REMOVE_STUDY_USER_DTO_KEY, removeStudyUserDTOResult);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + REMOVE_STUDY_USER_DTO_KEY, removeStudyUserDTOResult);
			return editUsersRedirectErrors(studyUsersDTO, changeReason, redirectAttributes);
		}

		studyUsersDTO.getStudyUserDTOs()
		             .removeIf(studyUserDTO -> studyUserDTO.getUserId().equals(removeStudyUserDTO.getRemovedUserId()));
		return editUsersRedirect(studyUsersDTO, changeReason, redirectAttributes);
	}

	private String editUsersRedirect(final StudyUsersDTO studyUsersDTO, final ChangeReason changeReason,
	                                 final RedirectAttributes redirectAttributes) {
		studyUsersDTO.setModified(true);
		redirectAttributes.addAttribute("id", studyUsersDTO.getStudyApiId());
		redirectAttributes.addFlashAttribute(STUDY_USERS_KEY, studyUsersDTO);
		redirectAttributes.addFlashAttribute(CHANGE_REASON_KEY, changeReason);
		return "redirect:/studies/editUsers";
	}

	private String editUsersRedirectErrors(final StudyUsersDTO studyUsersDTO,
										   final ChangeReason changeReason,
	                                       final RedirectAttributes redirectAttributes) {
		final String errorString = messageService.getMessage("validator.changeReason.reasonEmpty");
		redirectAttributes.addFlashAttribute("error", errorString);
		return editUsersRedirect(studyUsersDTO, changeReason, redirectAttributes);
	}

	private String editUsersSave(final StudyUsersDTO studyUsersDTO, final BindingResult bindingResult,
	                             final ChangeReason changeReason, final BindingResult changeReasonResult,
	                             final RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors() || changeReasonResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(CHANGE_REASON_KEY, changeReason);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + CHANGE_REASON_KEY, changeReasonResult);
			return editUsersRedirectErrors(studyUsersDTO, changeReason, redirectAttributes);
		}

		final Study study = studyRepository.findById(studyUsersDTO.getStudyId()).orElse(null);
		// "Should never happen" because of the validation, just to mute warnings
		if (study == null) {
			messageService.addError(redirectAttributes, "study.error.studyNotExist");
			return "redirect:/studies";
		}

		if (messageService.checkStudyStatus(List.of(StudyStatus.DELETED), study, redirectAttributes)) {
			return "redirect:/studies";
		}

		for (final var studyUserDto : studyUsersDTO.getStudyUserDTOs()) {
			for (final var sitePermissionBundle : studyUserDto.getSitePermissionBundles().entrySet()) {
				if (sitePermissionBundle.getValue() == null) {
					sitePermissionBundle.setValue(new HashSet<>());
				}
			}
		}

		// Set the audit entries data and persist it
		auditService.createAuditEntryUpdateStudyUsers(studyUsersDTO, changeReason);

		RandimiUser currentUser = ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal()).getUser();

		final AclClass studyAclClass = aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(),
		                                                                                StudyDTO.class.getName());
		final AclObjectIdentity studyAclObjectIdentity = aclObjectIdentityRepository
				.findFirstByObjectIdClassAndObjectIdIdentity(studyAclClass, study.getId());

		final Map<Long, AclObjectIdentity> siteAclObjectIdentityCache = new HashMap<>();
		// Grant the given rights on the study
		// Remove all current rights of the study except of the current user
		// And add the new ones
		// TODO: replace below when switching back to the
		// "we-cannot-modify-our-own-permissions-policy".
		// aclEntryDao.removeAllEntriesForAclObjectIdentityExceptCurrentUser(aclObjectIdentity,
		// currentUser);
		aclEntryRepository.deleteByAclObjectIdentity(studyAclObjectIdentity);
		for (final RandimiUser user : study.getAssignedUsers())
			user.getAssignedStudies().remove(study);
		study.getAssignedUsers().clear();
		for (final Site site : study.getSites()) {
			final AclClass siteAclClass = aclClassRepository.findFirstByClassNameOrSynonym(Site.class.getName(),
					SiteDTO.class.getName());
			AclObjectIdentity siteAclObjectIdentity = aclObjectIdentityRepository
					.findFirstByObjectIdClassAndObjectIdIdentity(siteAclClass, site.getId());

			if (siteAclObjectIdentity == null) {
				siteAclObjectIdentity = new AclObjectIdentity(site.getId(), Boolean.TRUE, siteAclClass,
						currentUser.getAclSid(), null);
				aclObjectIdentityRepository.save(siteAclObjectIdentity);
			}
			siteAclObjectIdentityCache.put(site.getId(), siteAclObjectIdentity);
			aclEntryRepository.deleteByAclObjectIdentity(siteAclObjectIdentity);
		}

		for (final StudyUserDTO studyUserDTO : studyUsersDTO.getStudyUserDTOs()) {
			RandimiUser user = userRepository.findById(studyUserDTO.getUserId()).orElse(null);
			if (user == null)
				continue;

			study.getAssignedUsers().add(user);
			user.getAssignedStudies().add(study);
			// Reset current permissions
			final Set<PermissionType> studyPermissions = new HashSet<>();
			studyPermissions.add(PermissionType.READ_STUDY);

			for (PermissionBundle permissionBundle : studyUserDTO.getStudyPermissionBundles())
				studyPermissions.addAll(PermissionBundle.getPermissionTypes(permissionBundle));

			for (final PermissionBundle permissionBundle : studyUserDTO.getAllSitePermissionBundles()) {
				studyPermissions.addAll(PermissionBundle.getPermissionTypes(permissionBundle));
			}

			for (final Site site : study.getSites())
			{
				final Set<PermissionBundle> sitePermissionBundles = studyUserDTO.getSitePermissionBundles().get(site.getGuiName());
				if (sitePermissionBundles == null || sitePermissionBundles.isEmpty())
					continue;

				final Set<PermissionType> sitePermissions = new HashSet<>();
				final AclObjectIdentity siteAclObjectIdentity = siteAclObjectIdentityCache.get(site.getId());

				for (PermissionBundle permissionBundle : sitePermissionBundles)
					sitePermissions.addAll(PermissionBundle.getPermissionTypes(permissionBundle));

				for (PermissionType permissionType : sitePermissions)
					applySitePermission(permissionType, user.getAclSid(), siteAclObjectIdentity, studyPermissions);
			}

			for (PermissionType permissionType : studyPermissions)
				applyStudyPermission(permissionType, user.getAclSid(), studyAclObjectIdentity);
		}
		return "redirect:/studies";
	}

	private void applySitePermission(final PermissionType permissionType, final AclSid aclSid,
			final AclObjectIdentity siteAclObjectIdentity, final Set<PermissionType> studyPermissions) {
		switch (permissionType) {
			// Permissions for site
			case CREATE_SUBJECT:
			case DELETE_SUBJECT:
			case READ_REPORT:
			case READ_SUBJECT:
			case UPDATE_SUBJECT:
				aclEntryRepository.save(new AclEntry(aclSid, siteAclObjectIdentity, 1, permissionType, true, false, false));
				break;
			// Permissions for study
			case DELETE_STUDY:
			case GET_NOTIFICATION:
			case READ_AUDIT_COMPLEX:
			case READ_AUDIT_SIMPLE:
			case READ_STUDY:
			case UPDATE_STUDY:
			case UPDATE_STUDY_USERS:
				studyPermissions.add(permissionType);
				break;
			default:
				LOGGER.warn("Unhandled permission type {}!", permissionType.getValue());
				break;
		}
	}

	private void applyStudyPermission(final PermissionType permissionType, final AclSid aclSid,
			final AclObjectIdentity studyAclObjectIdentity) {
		switch (permissionType) {
			// Permissions for study
			case CREATE_SUBJECT:
			case DELETE_STUDY:
			case DELETE_SUBJECT:
			case GET_NOTIFICATION:
			case READ_AUDIT_COMPLEX:
			case READ_AUDIT_SIMPLE:
			case READ_REPORT:
			case READ_STUDY:
			case READ_SUBJECT:
			case UPDATE_STUDY:
			case UPDATE_STUDY_USERS:
			case UPDATE_SUBJECT:
				aclEntryRepository
						.save(new AclEntry(aclSid, studyAclObjectIdentity, 1, permissionType, true, false, false));
				break;
			default:
				LOGGER.warn("Unhandled permission type {}!", permissionType.getValue());
				break;
		}
	}

//	public List<SubjectList> getRandomizationListsFromCSV(MultipartFile csvFile, Study study, String csvDelimiter)
//			throws CSVFileParseException {
//		List<SubjectList> randomizationLists = new ArrayList<>();
//
//		// Try to read the csv file into a two dimensional list
//		List<List<String>> records = new ArrayList<>();
//		try (BufferedReader br = new BufferedReader(
//				new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
//			String line;
//			while ((line = br.readLine()) != null) {
//				if (line.startsWith(StringUtils.getEXCEL_UTF8_BOM())) {
//					line = line.substring(StringUtils.getEXCEL_UTF8_BOM().length());
//				}
//				String[] values = line.split(csvDelimiter);
//				records.add(Arrays.asList(values));
//			}
//		} catch (IOException e) {
//			throw new CSVFileParseException("exception.csv.readError");
//		}
//
//		int orderNumber = 1;
//
//		// If there are no strata there is only one randomization list and no stratum
//		// interval code
//		if (study.getStratums().isEmpty()) {
//			if (records.size() == 1) {
//				SubjectList randomizationList = new SubjectList();
//				for (String studyArmName : records.get(0)) {
//					randomizationList
//							.addSubject(new Subject(orderNumber++, this.getStudyArmByName(study, studyArmName)));
//				}
//				randomizationLists.add(randomizationList);
//			} else {
//				throw new CSVFileParseException("exception.csv.formatException");
//			}
//		} else {
//			// If there are strata there must be stratum interval codes
//			StudyDTO simpleStudyDto = studyMapper.toStudyDTO(study);
//			List<String> possibleStratumIntervalCodes = StringUtils.getStratumIntervalCodes(
//					simpleStudyDto.getEnumeratedStratums(), simpleStudyDto.getIntervalStratums());
//			if (possibleStratumIntervalCodes.size() != records.size()) {
//				throw new CSVFileParseException("exception.csv.stratumIntervalCodeSizeException");
//			}
//			for (List<String> record : records) {
//				SubjectList randomizationList = new SubjectList();
//				while (orderNumber % 100 != 1) {
//					orderNumber++;
//				}
//				// Check stratum interval code
//				if (possibleStratumIntervalCodes.contains(record.get(0))) {
//					// Set the stratum interval code for this randomization list and remove it for
//					// the next iterations
//					randomizationList.setStratumIntervalCode(record.get(0));
//					possibleStratumIntervalCodes.remove(record.get(0));
//				} else {
//					throw new CSVFileParseException("exception.csv.stratumIntervalCodeSizeException");
//				}
//				for (int i = 1; i < record.size(); i++) {
//					randomizationList
//							.addSubject(new Subject(orderNumber++, this.getStudyArmByName(study, record.get(i))));
//				}
//				randomizationLists.add(randomizationList);
//			}
//		}
//
//		// Check if number of entries equal the study size
//		// int numberEntries = 0;
//		// for (SubjectList randomizationList : randomizationLists) {
//		// 	numberEntries += randomizationList.getSubjects().size();
//		// }
//		// TODO: Fix this method
//		// if (numberEntries != study.getStudySize()) {
//		// throw new CSVFileParseException("exception.csv.entriesDoNotMatchStudySize");
//		// }
//
//		return randomizationLists;
//	}

	private StudyArm getStudyArmByName(Study study, String studyArmName) throws CSVFileParseException {
		for (StudyArm studyArm : study.getStudyArms()) {
			if (studyArm.getGuiName().equals(studyArmName)) {
				return studyArm;
			}
		}
		throw new CSVFileParseException("exception.csv.studyArmNotFoundException");
	}

}
