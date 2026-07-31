package de.unimuenster.imi.randimi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import de.unimuenster.imi.randimi.dto.AuditEntryDTO;
import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.*;
import de.unimuenster.imi.randimi.dto.study.user.AddStudyUsersDTO;
import de.unimuenster.imi.randimi.dto.study.user.RemoveStudyUserDTO;
import de.unimuenster.imi.randimi.dto.study.user.StudyUserDTO;
import de.unimuenster.imi.randimi.dto.study.user.StudyUsersDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.mapping.study.user.StudyUserMapper;
import de.unimuenster.imi.randimi.mapping.study.user.StudyUsersMapper;
import de.unimuenster.imi.randimi.model.AuditEntry;
import de.unimuenster.imi.randimi.model.enumeration.*;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartSite;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.service.AuditService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.repository.user.AclClassRepository;
import de.unimuenster.imi.randimi.repository.user.AclObjectIdentityRepository;

/**
 * @author Daniel Preciado-Marquez
 */
public class StudyControllerTest extends MVCControllerTestBase {

	@Autowired
	AclClassRepository aclClassRepository;

	@Autowired
	AclEntryRepository aclEntryRepository;

	@Autowired
	AclObjectIdentityRepository aclObjectIdentityRepository;

	@Autowired
	AuditService auditService;

	@Autowired
	StratumCodeService stratumCodeService;

	@Autowired
	StudyUserMapper studyUserMapper;

	@Autowired
	StudyUsersMapper studyUsersMapper;

	@Test
	public void listTest() {
		List<Study> studies = studyRepository.findAll();
		List<Long> studyIds = studies.stream().map(study -> study.getId()).collect(Collectors.toList());

		Model model = new ExtendedModelMap();

		String path = studyController.list(model);

		// Test returned path
		assertEquals("/studies/list", path, "Returned wrong path!");

		// Test allStudies
		List<StudyDTO> allStudyDTOs = testAndGetListModelAttribute(model, "allStudies", StudyDTO.class);
		for (StudyDTO studyDTO : allStudyDTOs)
			assertTrue(studyIds.contains(studyDTO.getId()));

		// Test reasonTypes
		List<AuditReasonType> reasonTypes = testAndGetListModelAttribute(model, "reasonTypes", AuditReasonType.class);
		assertEquals(4, reasonTypes.size(), "Wrong number of AuditReasonType!");
		assertEquals(AuditReasonType.MISCONCEPTION, reasonTypes.get(0), "AuditReasonType MISCONCEPTION is missing!");
		assertEquals(AuditReasonType.CUSTOM, reasonTypes.get(1), "AuditReasonType CUSTOM is missing");

		// Test changeReason
		Object deleteStudyDTOObject = model.getAttribute("deleteStudyDTO");
		assertNotNull(deleteStudyDTOObject, "Attribut 'deleteStudyDTO' not set!");
		assertTrue(deleteStudyDTOObject instanceof DeleteStudyDTO, "Attribut 'deleteStudyDTO' is of the wrong type!");
		DeleteStudyDTO deleteStudyDTO = (DeleteStudyDTO) deleteStudyDTOObject;
		assertEquals(0, deleteStudyDTO.getStudyId());
	}

	@Test
	public void createGetTest() {
		Model model = new ExtendedModelMap();
		String path = studyController.create(model);

		// Test the returned path
		assertEquals("/studies/edit", path, "Returned wrong path!");

		// Test study
		StudyDTO study = testAndGetModelAttribute(model, "study", StudyDTO.class);
		assertFalse(studyRepository.existsById(study.getId()), "Study should be new but already exists!");
	}

	@Test
	@WithUserDetails(value = ACTIVE_USER_NAME,
	                 userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
	public void createGetNoPermissionTest() throws Exception {
		mockMvc.perform((get("/studies/create")))
		       .andExpect(status().isForbidden());
	}

	@Test
	public void editGetExistingStudyTest() {
		Study activeStudy = studyRepository.findByGuiName("Active Study").get(0);
		Model model = new ExtendedModelMap();
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		String path = studyController.edit(activeStudy.getApiId(), model, redirectAttributes);

		// Test returned path
		assertEquals("/studies/edit", path, "Returned wrong path!");

		// Test study
		StudyDTO study = testAndGetModelAttribute(model, "study", StudyDTO.class);
		assertEquals(activeStudy.getId(), study.getId(), "Model contains wrong study!");
	}

	@Test
	public void editGetArchivedStudy() throws Exception {
		var archivedStudy = getArchivedStudy();

		mockMvc.perform(get("/studies/edit")
				                .param("id", archivedStudy.getApiId()))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/studies"))
		       .andExpect(flash().attributeExists("error"))
		       .andExpect(flash().attribute("error", messageService.getMessage("study.error.studyArchived", archivedStudy.getId())));
	}

	@Test
	public void editPostCancelTest() {
		String path = studyController.edit("cancel", null, null, null, null, null);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
	}

	@Test
	public void createPostInvalidTest() {
		StudyDTO studyDTO = new StudyDTO(0L);

		final BindingResult bindingResultStudy = new BeanPropertyBindingResult(studyDTO, "StudyDTO");
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		String path = studyController.create("save", studyDTO, bindingResultStudy, redirectAttributes);

		assertEquals("redirect:/studies/create", path, "Returned wrong path!");
		assertTrue(redirectAttributes.getFlashAttributes().get("study") == studyDTO,
				"Attribute 'study' contains wrong object!");
		assertTrue(
				redirectAttributes.getFlashAttributes().get(BindingResult.MODEL_KEY_PREFIX + "study") == bindingResultStudy,
				"Attrubute for BindingResult contains wrong object!");
		assertFalse(redirectAttributes.containsAttribute("id"),
				"Editing a new study should not add the attribute 'id'!");
	}

	@Test
	public void createPostInvalid() throws Exception {
		StudyDTO studyDTO = new StudyDTO(0L);

		StudyArmDTO emptyStudyArm = new StudyArmDTO();
		emptyStudyArm.setOrderNumber(0);
		studyDTO.getStudyArms().add(emptyStudyArm);

		StudyArmDTO studyArmA = new StudyArmDTO();
		studyArmA.setRatio(2);
		studyDTO.getStudyArms().add(studyArmA);

		StudyArmDTO studyArmB = new StudyArmDTO();
		studyArmB.setRatio(4);
		studyDTO.getStudyArms().add(studyArmB);

		studyDTO.getSites().add(new SiteDTO());
		studyDTO.getSites().add(new SiteDTO());

		StudyDTO expectedStudyDTO = new StudyDTO(0);
		StudyArmDTO expectedStudyArmA = new StudyArmDTO();
		expectedStudyArmA.setRatio(1);
		expectedStudyArmA.setOrderNumber(0);
		expectedStudyDTO.getStudyArms().add(expectedStudyArmA);

		StudyArmDTO expectedStudyArmB = new StudyArmDTO();
		expectedStudyArmB.setRatio(2);
		expectedStudyArmB.setOrderNumber(1);
		expectedStudyDTO.getStudyArms().add(expectedStudyArmB);

		mockMvc.perform((post("/studies/create"))
				                .with(csrf())
				                .contentType(MediaType.APPLICATION_JSON)
				                .param("action", "save")
				                .flashAttr("study", studyDTO))
		       .andDo(print())
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/studies/create"))
		       .andExpect(flash().attributeExists("study"))
		       .andExpect(flash().attribute("study", expectedStudyDTO));
	}

	@Test
	public void editPostExistingStudyInvalidTest() {
		Study activeStudy = studyRepository.findByGuiName("Active Study").get(0);

		StudyDTO studyDTO = studyMapper.toStudyDTO(activeStudy);
		String oldDto = auditService.getOldDto(studyDTO);
		final ChangeReason changeReason = new ChangeReason("An update.", oldDto);
		final BindingResult bindingResultStudy = new BeanPropertyBindingResult(studyDTO, "StudyDTO");
		final BindingResult bindingResultChangeReason = new BeanPropertyBindingResult(changeReason, "ChangeReason");
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		studyDTO.setGuiName(null);

		String path = studyController.edit("save", studyDTO, bindingResultStudy, changeReason,
		                                   bindingResultChangeReason, redirectAttributes);

		assertEquals("redirect:/studies/edit", path, "Returned wrong path!");
		assertTrue(redirectAttributes.getFlashAttributes().get("study") == studyDTO,
				"Attribute 'study' contains wrong object!");
		assertTrue(
				redirectAttributes.getFlashAttributes()
				                  .get(BindingResult.MODEL_KEY_PREFIX + "study") == bindingResultStudy,
				"Attrubute for BindingResult contains wrong object!");
		assertEquals(studyDTO.getApiId(), redirectAttributes.getAttribute("id"), "Attribute 'id' not set");

		List<AuditEntry> auditEntries = auditEntryRepository.findByStudyId(studyDTO.getId());
		Assertions.assertEquals(AuditType.ACTIVATE, auditEntries.get(auditEntries.size() - 1).getAuditType(),
		                        "AuditEntry should not have been created!");
//		AuditEntry lastAuditEntry = auditEntries.get(auditEntries.size() - 1);
//		assertTrue(
//				lastAuditEntry.getAuditType() != AuditType.UPDATE && lastAuditEntry.getStudyId() != activeStudy.getId(),
//				"AuditEntry should not have been created!");
	}

	@Test
	public void editPostExistingStudyTest() {
		Study inactiveStudy = studyRepository.findByGuiName("Inactive Study").get(0);

		StudyDTO studyDTO = studyMapper.toStudyDTO(inactiveStudy);
		String oldDto = auditService.getOldDto(studyDTO);
		final ChangeReason changeReason = new ChangeReason("An update.", oldDto);
		final BindingResult bindingResultStudy = new BeanPropertyBindingResult(studyDTO, "StudyDTO");
		final BindingResult bindingResultChangeReason = new BeanPropertyBindingResult(changeReason, "ChangeReason");
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		studyDTO.setGuiName("Updated name");

		String path = studyController.edit("save", studyDTO, bindingResultStudy, changeReason,
		                                   bindingResultChangeReason, redirectAttributes);

		assertEquals("redirect:/studies", path, "Returned wrong path!");

		Study updatedStudy = studyRepository.findById(studyDTO.getId()).get();
		assertEquals("Updated name", updatedStudy.getGuiName(), "Study has not been updated!");

		testLastAuditEntryForStudy(studyDTO.getId(), AuditType.UPDATE, "An update.");
	}

	@Test
	public void createPostNoStratificationCointossTest() {
		StudyDTO studyDTO = getValidStudyDTONoStratificationCointoss();
		createPostTest(studyDTO);
	}

	@Test
	@WithUserDetails(value = LOCAL_MANAGER_NAME,
	                 userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
	public void createPostLocalManagerTest() {
		RandimiUser activeTestUser = getLocalManager();
		StudyDTO studyDTO = getValidStudyDTONoStratificationCointoss();
		Study study = createPostTest(studyDTO);

		for (PermissionType permissionType : PermissionType.values()) {
			final AclEntry aclEntry = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study,
			                                                                                         activeTestUser.getAclSid(),
			                                                                                         permissionType);
			assertNotNull(aclEntry, "User has no " + permissionType.name() + " permission!");
		}
	}

	@Test
	public void createPostStratificationNotBySiteCointossTest() {
		StudyDTO studyDTO = getValidStudyDTOStratificationNotBySiteCointoss();
		createPostTest(studyDTO);
	}

	@Test
	public void createPostStratificationOnlyBySiteCointossTest() {
		StudyDTO studyDTO = getValidStudyDTOStratificationOnlyBySiteCointoss();
		createPostTest(studyDTO);
	}

	@Test
	public void createPostStratificationCointossTest() {
		StudyDTO studyDTO = getValidStudyDTOStratificationCointoss();
		createPostTest(studyDTO);
	}

	@Test
	public void createPostNoStratificationBlockedTest() {
		StudyDTO studyDTO = getValidStudyDTONoStratificationBlocked();
		createPostTest(studyDTO);
	}

	@Test
	public void createPostNoStratificationBlockedPreGeneratedTest() {
		StudyDTO studyDTO = getValidStudyDTONoStratificationBlockedPreGenerated();
		createPostTest(studyDTO);
	}

	@Test
	public void editPostPseudonymHandlingEmptyStudyTest() throws Exception {
		Study study = createAndActivateStudyStratificationBlocked();
		StudyDTO studyDTO = studyMapper.toStudyDTO(study);
		final ChangeReason changeReason = new ChangeReason("Stratified by site.", auditService.getOldDto(studyDTO));

		studyDTO.setPseudonymHandling(PseudonymHandling.UNIQUE_IN_STUDY);

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", studyDTO)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"));

		assertEquals(PseudonymHandling.UNIQUE_IN_STUDY, study.getPseudonymHandling(),
		             "Pseudonym handling has not been updated!");
	}

	@Test
	public void editPostPseudonymHandlingTest() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();
		SubjectDTO subject = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		randomizeSubject(subject);

		StudyDTO studyDTO = studyMapper.toStudyDTO(study);
		final ChangeReason changeReason = new ChangeReason("Stratified by site.", auditService.getOldDto(studyDTO));

		studyDTO.setPseudonymHandling(PseudonymHandling.UNIQUE_IN_STUDY);

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", studyDTO)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"));

		assertEquals(PseudonymHandling.UNIQUE_IN_STUDY, study.getPseudonymHandling(),
		             "Pseudonym handling has not been updated!");
	}

	@Test
	public void editPostPseudonymHandlingDuplicatePseudonymTest() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();

		SubjectDTO subject = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		subject.setPseudonym("duplicate");
		randomizeSubject(subject);

		subject = getValidSubjectDTONoStratification(study, study.getSites().get(1));
		subject.setPseudonym("duplicate");
		randomizeSubject(subject);

		StudyDTO studyDTO = studyMapper.toStudyDTO(study);
		final ChangeReason changeReason = new ChangeReason("Stratified by site.", auditService.getOldDto(studyDTO));

		studyDTO.setPseudonymHandling(PseudonymHandling.UNIQUE_IN_STUDY);

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", studyDTO)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("error"))
		       .andExpect(flash().attribute("error", messageService.getMessage("general.error.invalidForm")))
		       .andExpect(validateBindingResult("pseudonymHandling"));

		assertEquals(PseudonymHandling.UNIQUE_IN_LOCATION, study.getPseudonymHandling(),
		             "Pseudonym handling should not have been updated!");
	}

	@Test
	public void editPostAddSiteTest() {
		Study study = createAndActivateStudyStratificationBlocked();
		StudyDTO studyDTO = studyMapper.toStudyDTO(study);
		String oldDto = auditService.getOldDto(studyDTO);
		final ChangeReason changeReason = new ChangeReason("Added site.", oldDto);
		final BindingResult bindingResultStudy = new BeanPropertyBindingResult(studyDTO, "StudyDTO");
		final BindingResult bindingResultChangeReason = new BeanPropertyBindingResult(changeReason, "ChangeReason");

		SiteDTO siteDTO = new SiteDTO();
		siteDTO.setApiId("Site C");
		siteDTO.setCapacity(12);
		siteDTO.setGuiName("Site C");
		siteDTO.setUseApiId(false);

		studyDTO.getSites().add(siteDTO);
		// Front end does not post location stratum
		studyDTO.setSiteStratum(null);

		String path = studyController.edit("save", studyDTO, bindingResultStudy, changeReason,
		                                   bindingResultChangeReason, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
		testLastAuditEntryForStudy(studyDTO.getId(), AuditType.UPDATE, "Added site.");

		assertEquals(3, study.getSites().size(), "Site has not been added!");
		assertEquals(3, stratumCodeService.getLocationStratum(study).get().getStratumParts().size(),
		                        "StratumPart has not been created!");
		assertEquals(18, study.getSubjectLists().size(), "SubjectLists have not been created!");
	}

	@Test
	public void editPostRenameSiteTest() {
		Study study = createAndActivateStudyStratificationBlocked();
		StudyDTO studyDTO = studyMapper.toStudyDTO(study);
		String oldDto = auditService.getOldDto(studyDTO);
		final ChangeReason changeReason = new ChangeReason("Renamed site.", oldDto);
		final BindingResult bindingResultStudy = new BeanPropertyBindingResult(studyDTO, "StudyDTO");
		final BindingResult bindingResultChangeReason = new BeanPropertyBindingResult(changeReason, "ChangeReason");

		studyDTO.getSites().get(0).setGuiName("Updated");

		// Front end does not post location stratum
		studyDTO.setSiteStratum(null);

		String path = studyController.edit("save", studyDTO, bindingResultStudy, changeReason,
		                                   bindingResultChangeReason, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
		testLastAuditEntryForStudy(studyDTO.getId(), AuditType.UPDATE, "Renamed site.");

		assertEquals("Updated", study.getSites().get(0).getGuiName(), "Site has not been renamed!");
		assertEquals(2, study.getStratums().get(2).getStratumParts().size(),
		             "Number of stratum parts should not have changed!");
		assertEquals("Updated",
		             ((StratumPartSite) study.getStratums().get(2).getStratumParts().get(0)).getSite().getGuiName(),
		             "Site of the stratum has not been updated!");
		assertEquals(12, study.getSubjectLists().size(), "Number of subject lists should not have changed!");
		assertEquals("Updated",
		             ((StratumPartSite) study.getSubjectLists().get(0).getStratumParts().get(2)).getSite().getGuiName(),
		             "Site of the subject list has not been updated!");
	}

	@Test
	public void editPostRemoveSiteTest() {
		Study study = createAndActivateStudyStratificationCointoss();
		StudyDTO studyDTO = studyMapper.toStudyDTO(study);
		String oldDto = auditService.getOldDto(studyDTO);
		final ChangeReason changeReason = new ChangeReason("Removed site.", oldDto);
		final BindingResult bindingResultStudy = new BeanPropertyBindingResult(studyDTO, "StudyDTO");
		final BindingResult bindingResultChangeReason = new BeanPropertyBindingResult(changeReason, "ChangeReason");

		studyDTO.getSites().remove(0);
		studyDTO.getSites().get(0).setCapacity(24);

		// Front end does not post location stratum
		studyDTO.setSiteStratum(null);

		String path = studyController.edit("save", studyDTO, bindingResultStudy, changeReason,
		                                   bindingResultChangeReason, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
		testLastAuditEntryForStudy(studyDTO.getId(), AuditType.UPDATE, "Removed site.");

		assertEquals(1, study.getSites().size(), "Site has not been removed!");
		assertEquals(1, stratumCodeService.getLocationStratum(study).get().getStratumParts().size(),
		             "StratumPart has not been removed!");
		assertEquals(6, study.getSubjectLists().size(), "SubjectLists have not been removed!");
	}

	@Test
	public void editPostStratifyBySite() throws Exception {
		final StudyDTO dto = getValidStudyDTOStratificationNotBySiteBlocked();
		dto.getSites().remove(1);
		dto.getSites().get(0).setCapacity(24);
		Study study = createStudy(dto);
		study = activateStudy(study);

		final ChangeReason changeReason = new ChangeReason("Stratified by site.", auditService.getOldDto(dto));
		final StudyDTO updatedDto = studyMapper.toStudyDTO(study);
		updatedDto.setStratifyBySite(true);

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", updatedDto)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"));

		assertTrue(study.isStratifiedBySite(), "Study is not stratified by site!");
		assertEquals(6, study.getSubjectLists().size(), "Number subject list should not have changed!");
		assertEquals(3, study.getSubjectLists().get(0).getStratumParts().size(),
		             "Stratum parts of subject lists have not been updated!");
	}

	@Test
	public void editPostStratifyBySitePreGenerated() throws Exception {
		final StudyDTO dto = getValidStudyDTONoStratificationBlockedPreGenerated();
		dto.getSites().get(0).setCapacity(dto.getCapacity());
		dto.getSites().remove(1);
		Study study = createStudy(dto);
		study = activateStudy(study);

		final ChangeReason changeReason = new ChangeReason("Stratified by site.", auditService.getOldDto(dto));
		final StudyDTO updatedDto = studyMapper.toStudyDTO(study);
		updatedDto.setStratifyBySite(true);

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", updatedDto)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"));

		assertTrue(study.isStratifiedBySite(), "Study is not stratified by site!");
		assertEquals(1, study.getSubjectLists().size(), "Number subject list should not have changed!");
		assertEquals(1, study.getSubjectLists().get(0).getStratumParts().size(),
		             "Stratum parts of subject lists have not been updated!");
	}

	@Test
	public void editPostStratifyBySiteTwoNewSites() throws Exception {
		final StudyDTO dto = getValidStudyDTOStratificationNotBySiteBlocked();
		final SiteDTO siteDtoB = dto.getSites().get(1);
		dto.getSites().remove(1);
		dto.getSites().get(0).setCapacity(24);
		Study study = createStudy(dto);
		study = activateStudy(study);

		final ChangeReason changeReason = new ChangeReason("Stratified by site.", auditService.getOldDto(dto));
		final StudyDTO updatedDto = studyMapper.toStudyDTO(study);
		updatedDto.getSites().add(siteDtoB);

		updatedDto.setStratifyBySite(true);

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", updatedDto)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"));

		assertTrue(study.isStratifiedBySite(), "Study is not stratified by site!");
		assertEquals(12, study.getSubjectLists().size(), "Number subject list should not have changed!");
		assertEquals(3, study.getSubjectLists().get(0).getStratumParts().size(),
		             "Stratum parts of subject lists have not been updated!");
	}

	@Test
	public void editPostStratifyBySiteTooManySites() throws Exception {
		final Study study = createAndActivateStudyStratificationNotBySiteCointoss();
		final StudyDTO updatedDto = studyMapper.toStudyDTO(study);
		final ChangeReason changeReason = new ChangeReason("Stratified by site.", auditService.getOldDto(updatedDto));
		updatedDto.setStratifyBySite(true);

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", updatedDto)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("error"))
		       .andExpect(flash().attribute("error", messageService.getMessage("general.error.invalidForm")))
		       .andExpect(validateBindingResult("stratifyBySite"));
	}

	@Test
	public void editPostStratifyBySitePreGeneratedCapacityMismatch() throws Exception {
		final StudyDTO dto = getValidStudyDTONoStratificationBlockedPreGenerated();
		dto.getSites().get(0).setCapacity(43);
		dto.getSites().remove(1);
		Study study = createStudy(dto);
		study = activateStudy(study);

		final ChangeReason changeReason = new ChangeReason("Stratified by site.", auditService.getOldDto(dto));
		final StudyDTO updatedDto = studyMapper.toStudyDTO(study);
		updatedDto.setStratifyBySite(true);

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", updatedDto)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("error"))
		       .andExpect(flash().attribute("error", messageService.getMessage("general.error.invalidForm")))
		       .andExpect(validateBindingResult("stratifyBySite"));
	}

	@Test
	public void editPostTestMode() throws Exception {
		Study study = createStudyNoStratificationCointoss();

		mockMvc.perform(get("/studies/test")
				                .with(csrf())
				                .param("id", Objects.toString(study.getId())))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"))
		       .andExpect(flash().attribute("success", messageService.getMessage("study.success.test", study.getGuiName())));

		assertEquals(1, study.getSubjectLists().size(), "Subject lists have not been created!");
		assertEquals(0, study.getSubjectLists().get(0).getSubjects().size(), "Study should be empty!");
		assertTrue(study.isInTestMode(), "Study is not in test mode!");

		SubjectDTO subject = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		randomizeSubject(subject);

		final StudyDTO updatedDto = studyMapper.toStudyDTO(study);
		final ChangeReason changeReason = new ChangeReason("Outdated description.", auditService.getOldDto(updatedDto));
		updatedDto.setDescription("New description");

		mockMvc.perform(post("/studies/edit").with(csrf())
		                                     .param("action", "save")
		                                     .flashAttr("study", updatedDto)
		                                     .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"));

		assertEquals(0, study.getSubjectLists().get(0).getSubjects().size(), "Study should be empty!");
	}

	@Test
	public void editPostArchivedStudy() throws Exception {
		var archivedStudy = getArchivedStudy();

		final StudyDTO updatedDto = studyMapper.toStudyDTO(archivedStudy);
		final ChangeReason changeReason = new ChangeReason("Try to update archived study.",
		                                                   auditService.getOldDto(updatedDto));

		mockMvc.perform(post("/studies/edit")
				                .with(csrf())
				                .param("action", "save")
				                .flashAttr("study", updatedDto)
				                .flashAttr("changeReason", changeReason))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/studies"))
		       .andExpect(flash().attributeExists("error"))
		       .andExpect(flash().attribute("error", messageService.getMessage("study.error.studyArchived", archivedStudy.getId())));
	}

	@Test
	public void viewInvalidIdTest() {
		String path = studyController.view("0", model, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
	}

	@Test
	public void viewValidIdTest() {
		Study activeStudy = studyRepository.findByGuiName("Active Study").get(0);

		String path = studyController.view(activeStudy.getApiId(), model, redirectAttributes);
		assertEquals("/studies/view", path, "Returned wrong path!");

		List<AuditEntry> auditEntries = auditEntryRepository.findByStudyId(activeStudy.getId());
		assertEquals(AuditType.READ, auditEntries.get(auditEntries.size() - 1).getAuditType(),
				"AuditEntry has not been created!");

		StudyDTO studyDTO = testAndGetModelAttribute(model, "study", StudyDTO.class);
		assertEquals(activeStudy.getId(), studyDTO.getId(), "Attribute 'study' contains the wrong study!");

		testAndGetListModelAttribute(model, "auditEntries", AuditEntryDTO.class);
	}

	@Test
	public void testInvalidId() throws Exception {
		mockMvc.perform(get("/studies/test")
				                .with(csrf())
				                .param("id", "0"))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("error"))
		       .andExpect(flash().attribute("error", messageService.getMessage("study.error.studyNotExist")));
	}

	public void testNoStratification() throws Exception {
		Study study = createStudyNoStratificationCointoss();

		mockMvc.perform(get("/studies/test")
				                .with(csrf())
				                .param("id", Objects.toString(study.getId())))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"))
		       .andExpect(flash().attribute("success", messageService.getMessage("study.success.test", study.getGuiName())));
		testLastAuditEntryForStudy(study.getId(), AuditType.TEST, null);

		assertEquals(1, study.getSubjectLists().size(), "Subject lists have not been created!");
		assertEquals(0, study.getSubjectLists().get(0).getSubjects().size(), "Study should be empty!");
		assertTrue(study.isInTestMode(), "Study is not in test mode!");

		SubjectDTO subject = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		randomizeSubject(subject);

		mockMvc.perform(get("/studies/test")
				                .with(csrf())
				                .param("id", Objects.toString(study.getId())))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"))
		       .andExpect(flash().attribute("success", messageService.getMessage("study.success.test", study.getGuiName())));
		testLastAuditEntryForStudy(study.getId(), AuditType.TEST, null);

		assertEquals(0, study.getSubjectLists().get(0).getSubjects().size(), "Study should be empty!");
	}

	@Test
	public void activateInvalidIdTest() {
		String path = studyController.activate(0L, model, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
	}

	@Test
	public void activateNoStratificationCointossTest() {
		Study study = createStudyNoStratificationCointoss();

		String path = studyController.activate(study.getId(), model, redirectAttributes);

		assertEquals("redirect:/studies", path, "Returned wrong path!");
		testLastAuditEntryForStudy(study.getId(), AuditType.ACTIVATE, null);

		assertTrue(study.isActive(), "Study has not been activated!");
		assertNotNull(study.getActivationDate(), "Study has not been activated!");
		assertEquals(1, study.getSubjectLists().size(), "Wrong amount of generated SubjectLists!");

		SubjectList subjectList = study.getSubjectLists().get(0);
		assertEquals(0, subjectList.size(), "SubjectList should be empty!");
		testStratumParts(subjectList.getStratumParts(), new ArrayList<>());
	}

	@Test
	public void activateStratificationNotBySiteCointossTest() {
		Study study = createStudyStratificationNotBySiteCointoss();

		String path = studyController.activate(study.getId(), model, redirectAttributes);

		assertEquals("redirect:/studies", path, "Returned wrong path!");
		testLastAuditEntryForStudy(study.getId(), AuditType.ACTIVATE, null);

		assertTrue(study.isActive(), "Study has not been activated!");
		assertNotNull(study.getActivationDate(), "Study has not been activated!");
		assertEquals(6, study.getSubjectLists().size(), "Wrong amount of generated SubjectLists!");
	}

	@Test
	public void activateStratificationOnlyBySiteCointossTest() {
		Study study = createStudyStratificationOnlyBySiteCointoss();

		String path = studyController.activate(study.getId(), model, redirectAttributes);

		assertEquals("redirect:/studies", path, "Returned wrong path!");
		testLastAuditEntryForStudy(study.getId(), AuditType.ACTIVATE, null);

		assertTrue(study.isActive(), "Study has not been activated!");
		assertNotNull(study.getActivationDate(), "Study has not been activated!");
		assertEquals(2, study.getSubjectLists().size(), "Wrong amount of generated SubjectLists!");

		testStratumParts(study.getSubjectLists().get(0).getStratumParts(), List.of(Pair.of("location", "Site A")));
		testStratumParts(study.getSubjectLists().get(1).getStratumParts(), List.of(Pair.of("location", "Site B")));
	}

	@Test
	public void activateStratificationCointossTest() {
		Study study = createStudyStratificationCointoss();

		String path = studyController.activate(study.getId(), model, redirectAttributes);

		assertEquals("redirect:/studies", path, "Returned wrong path!");
		testLastAuditEntryForStudy(study.getId(), AuditType.ACTIVATE, null);

		assertTrue(study.isActive(), "Study has not been activated!");
		assertNotNull(study.getActivationDate(), "Study has not been activated!");
		assertEquals(12, study.getSubjectLists().size(), "Wrong amount of generated SubjectLists!");
	}

	@Test
	public void activateNoStratificationBlockedTest() {
		Study study = createStudyNoStratificationBlocked();

		String path = studyController.activate(study.getId(), model, redirectAttributes);

		assertEquals("redirect:/studies", path, "Returned worng path!");
		testLastAuditEntryForStudy(study.getId(), AuditType.ACTIVATE, null);

		assertTrue(study.isActive(), "Study has not been activated!");
		assertNotNull(study.getActivationDate(), "Study has not been activated!");

		assertArrayEquals(new Integer[] { 0, 0 }, study.getSubjectLists().get(0).getRemainingAssignments(),
				"Remaining assignments not initialized!");
	}

	@Test
	public void activateNoStratificationBlockedPreGeneratedTest() {
		Study study = createStudyNoStratificationBlockedPreGenerate();

		String path = studyController.activate(study.getId(), model, redirectAttributes);

		assertEquals("redirect:/studies", path, "Returned worng path!");
		testLastAuditEntryForStudy(study.getId(), AuditType.ACTIVATE, null);

		assertTrue(study.isActive(), "Study has not been activated!");
		assertNotNull(study.getActivationDate(), "Study has not been activated!");
		assertEquals(study.getCapacity(), study.getSubjectLists().get(0).getSubjects().size(),
		             "Subject list has not been initialized");

		Subject first = study.getSubjectLists().get(0).getSubjects().get(0);
		assertEquals(SubjectStatus.PRE_GENERATED, first.getStatus(), "Status has been initialized incorrectly");
		assertNull(first.getPseudonym(), "Pseudonym should be null");

		assertArrayEquals(new Integer[] { 0, 0 }, study.getSubjectLists().get(0).getRemainingAssignments(),
		                  "Remaining assignments not initialized!");
	}

	@Test
	public void activateTestMode() throws Exception {
		Study study = createStudyNoStratificationCointoss();

		mockMvc.perform(get("/studies/test")
				                .with(csrf())
				                .param("id", Objects.toString(study.getId())))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"))
		       .andExpect(flash().attribute("success", messageService.getMessage("study.success.test", study.getGuiName())));
		testLastAuditEntryForStudy(study.getId(), AuditType.TEST, null);

		assertEquals(1, study.getSubjectLists().size(), "Subject lists have not been created!");
		assertEquals(0, study.getSubjectLists().get(0).getSubjects().size(), "Study should be empty!");
		assertTrue(study.isInTestMode(), "Study is not in test mode!");

		SubjectDTO subject = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		randomizeSubject(subject);

		mockMvc.perform(get("/studies/activate").with(csrf())
		                                     .param("id", Objects.toString(study.getId())))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(flash().attributeExists("success"));
		testLastAuditEntryForStudy(study.getId(), AuditType.ACTIVATE, null);

		assertEquals(0, study.getSubjectLists().get(0).getSubjects().size(), "Study should be empty!");
	}

	@Test
	public void lockStudy() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(post("/studies/lock")
				                .with(csrf())
				                .param("id", Objects.toString(activeStudy.getId()))
				                .param("lock", Objects.toString(true)))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(redirectedUrl("/studies"))
		       .andExpect(flash().attributeExists("success"));

		assertEquals(StudyStatus.LOCKED, activeStudy.getStatus(), "Study has not been locked!");
	}

	@Test
	public void lockInactiveStudy() throws Exception {
		Study inactiveStudy = getInactiveStudy();

		mockMvc.perform(post("/studies/lock")
				                .with(csrf())
				                .param("id", Objects.toString(inactiveStudy.getId()))
				                .param("lock", Objects.toString(true)))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(redirectedUrl("/studies"))
		       .andExpect(flash().attributeExists("error"));

		assertEquals(StudyStatus.CREATED, inactiveStudy.getStatus(), "Study has not been locked!");
	}

	@Test
	public void unlockStudy() throws Exception {
		Study lockedStudy = getLockedActiveStudy();

		mockMvc.perform(post("/studies/lock")
				                .with(csrf())
				                .param("id", Objects.toString(lockedStudy.getId()))
				                .param("lock", Objects.toString(false)))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(redirectedUrl("/studies"))
		       .andExpect(flash().attributeExists("success"));

		assertEquals(StudyStatus.ACTIVE, lockedStudy.getStatus(), "Study has not been locked!");
	}

	@Test
	public void archiveStudy() throws Exception {
		Study study = createAndActivateStudyNoStratificationBlocked();

		final ArchiveStudyDTO archiveStudyDTO = new ArchiveStudyDTO();
		archiveStudyDTO.setStudyId(study.getId());

		mockMvc.perform(post("/studies/archive")
				                .with(csrf())
				                .flashAttr(StudyController.ARCHIVE_STUDY_DTO_KEY, archiveStudyDTO))
		       .andExpect(redirectedUrl("/studies/archived"))
		       .andExpect(flash().attributeExists("success"));
	}

	@Test
	public void archiveStudyPastRetentionPeriod() throws Exception {
		Study study = createAndActivateStudyNoStratificationBlocked();

		final ArchiveStudyDTO archiveStudyDTO = new ArchiveStudyDTO();
		archiveStudyDTO.setStudyId(study.getId());
		archiveStudyDTO.setRetentionPeriod(LocalDate.now().minusDays(1));

		mockMvc.perform(post("/studies/archive")
				                .with(csrf())
				                .flashAttr(StudyController.ARCHIVE_STUDY_DTO_KEY, archiveStudyDTO))
		       .andExpect(redirectedUrl("/studies"))
		       .andExpect(flash().attributeExists("error"))
		       .andExpect(flash().attribute("error", messageService.getMessage("studies.archiveStudy.error") + " " +
		                                             messageService.getMessage(
				       "validator.archiveStudy.retentionPeriodMustBeInTheFuture")));
	}

	@Test
	public void removeStudyTest() {
		Study study = createStudyNoStratificationCointoss();

		final ChangeReason changeReason = new ChangeReason("Deleted study.", null);
		final DeleteStudyDTO deleteStudyDTO = new DeleteStudyDTO(changeReason, study.getId());
		final BindingResult result = new BeanPropertyBindingResult(deleteStudyDTO, "DeleteStudyDTO");

		String path = studyController.remove(deleteStudyDTO, result, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
		assertFalse(redirectAttributes.containsAttribute("error"), "Error attribute should not be present!");
//		testLastAuditEntryForStudy(study.getId(), AuditType.DELETE, "Deleted study.");

		Optional<Study> deletedStudy = studyRepository.findById(study.getId());
		assertFalse(deletedStudy.isPresent(), "Study should have been deleted!");

		AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(), null), study.getId());
		assertNull(aclObjectIdentity, "AclObjectIdentity should have been deleted!");

		Site site = study.getSites().get(0);
		assertFalse(siteRepository.findById(site.getId()).isPresent(), "Associated sites have not been deleted!");
	}

	@Test
	public void removeActiveStudyTest() {
		Study study = createAndActivateStudyNoStratificationCointoss();

		final ChangeReason changeReason = new ChangeReason("Deleted study.", null);
		final DeleteStudyDTO deleteStudyDTO = new DeleteStudyDTO(changeReason, study.getId());
		final BindingResult result = new BeanPropertyBindingResult(deleteStudyDTO, "DeleteStudyDTO");

		String path = studyController.remove(deleteStudyDTO, result, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
		assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"), "Error attribute should be present!");
		assertEquals(messageService.getMessage("study.error.deleteActivatedStudy", study.getGuiName()),
		             redirectAttributes.getFlashAttributes().get("error"), "Wrong error message!");
	}

	@Test
	public void removeStudyInvalidDeleteStudyDTOTest() throws Exception {
		final ChangeReason changeReason = new ChangeReason(null, null);
		final DeleteStudyDTO deleteStudyDTO = new DeleteStudyDTO(changeReason, 0);

		mockMvc.perform(post("/studies/remove")
				                .with(csrf())
				                .flashAttr(StudyController.DELETE_STUDY_DTO_KEY, deleteStudyDTO))
		       .andExpect(redirectedUrl("/studies"))
		       .andExpect(flash().attributeExists("error"));
	}

	@Test
	public void editUsersGetInvalidStudyIdTest() {
		String path = studyController.editUsers("0", model, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
	}

	@Test
	public void editUsersGetTest() {
		Study study = createStudyNoStratificationCointoss();
		String path = studyController.editUsers(study.getApiId(), model, redirectAttributes);

		assertEquals("/studies/editUsers", path, "Returned wrong path!");

		StudyUsersDTO studyUsersDTO = testAndGetModelAttribute(model, "studyUsers", StudyUsersDTO.class);
		assertEquals(study.getId(), studyUsersDTO.getStudyId(), "Wrong study id");
		assertEquals(0, studyUsersDTO.getStudyUserDTOs().size(), "There should not be entries!");
		assertEquals(2, studyUsersDTO.getSiteNames().size(), "There should be an entry for every site!");
		assertTrue(studyUsersDTO.getSiteNames().containsValue("Site A"), "First site is missing!");
		assertTrue(studyUsersDTO.getSiteNames().containsValue("Site B"), "Second site is missing!");
	}

	@Test
	public void editUsersGetArchivedStudy() throws Exception {
		var archivedStudy = getArchivedStudy();

		mockMvc.perform(get("/studies/editUsers")
				                .param("id", archivedStudy.getApiId()))
		       .andExpect(status().isOk())
		       .andExpect(view().name("/studies/editUsers"));
	}

	@Test
	public void editUsersPostAddTest() {
		final Study study = getActiveStudy();
		final RandimiUser user = getActiveUser();

		final StudyUsersDTO studyUsersDTO = studyUsersMapper.toStudyUsersDTO(study);
		final AddStudyUsersDTO addStudyUsersDTO = new AddStudyUsersDTO();
		addStudyUsersDTO.getNewUserIds().add(user.getId());
		final ChangeReason changeReason = new ChangeReason();

		final BindingResult bindingResult = new BeanPropertyBindingResult(studyUsersDTO, "StudyUsersDTO");
		final BindingResult addBindingResult = new BeanPropertyBindingResult(addStudyUsersDTO, "AddStudyUsersDTO");

		final String path = studyController.editUsersAdd(studyUsersDTO, bindingResult, addStudyUsersDTO,
		                                                 addBindingResult, changeReason, redirectAttributes);
		assertEquals("redirect:/studies/editUsers", path, "Returned wrong path!");

		assertTrue(redirectAttributes.containsAttribute("id"), "Redirect attributes doesn't contain the study id!");
		assertNotNull(redirectAttributes.getAttribute("id"), "Redirect attributes doesn't contain the study id!");
		assertEquals(study.getApiId(), redirectAttributes.getAttribute("id"), "Redirect attributes contains a wrong study id!");

		assertTrue(redirectAttributes.getFlashAttributes().containsKey(StudyController.STUDY_USERS_KEY), "Redirect attributes doesn't contain the StudyUsersDTO!");
		assertTrue(studyUsersDTO.getStudyUserDTOs().stream().anyMatch(studyUserDTO -> studyUserDTO.getUserId() == user.getId()), "The study users doesn't contain the added user!");
	}

	@Test
	public void editUsersPostRemoveTest() {
		final Study study = getActiveStudy();
		final RandimiUser user = getActiveUser();

		final StudyUsersDTO studyUsersDTO = studyUsersMapper.toStudyUsersDTO(study);
		final StudyUserDTO activeUserStudyUserDTO = studyUserMapper.toStudyUserDTO(study, user);
		studyUsersDTO.getStudyUserDTOs().add(activeUserStudyUserDTO);
		final RemoveStudyUserDTO removeStudyUserDTO = new RemoveStudyUserDTO();
		removeStudyUserDTO.setRemovedUserId(user.getId());
		final ChangeReason changeReason = new ChangeReason();

		final BindingResult bindingResult = new BeanPropertyBindingResult(studyUsersDTO, "StudyUsersDTO");
		final BindingResult removeBindingResult = new BeanPropertyBindingResult(removeStudyUserDTO, "RemoveStudyUserDTO");

		final String path = studyController.editUsersRemove(studyUsersDTO, bindingResult, removeStudyUserDTO,
		                                                    removeBindingResult, changeReason, redirectAttributes);
		assertEquals("redirect:/studies/editUsers", path, "Returned wrong path!");

		assertTrue(redirectAttributes.containsAttribute("id"), "Redirect attributes doesn't contain the study id!");
		assertNotNull(redirectAttributes.getAttribute("id"), "Redirect attributes doesn't contain the study id!");
		assertEquals(study.getApiId(), redirectAttributes.getAttribute("id"), "Redirect attributes contains a wrong study id!");

		assertTrue(redirectAttributes.getFlashAttributes().containsKey(StudyController.STUDY_USERS_KEY),
				"Redirect attributes doesn't contain the StudyUsersDTO!");
		assertFalse(studyUsersDTO.getStudyUserDTOs().stream().anyMatch(studyUserDTO -> studyUserDTO.getUserId() == user.getId()),
				"The study users doesn't contain the added user!");
	}

	@Test
	public void editUsersPostAddUserTest() {
		final Study study = getActiveStudy();
		RandimiUser user = getActiveUser();
		final StudyUsersDTO studyUsersDTO = studyUsersMapper.toStudyUsersDTO(study);
		final ChangeReason changeReason = new ChangeReason("Added permissions", auditService.getOldDto(studyUsersDTO));

		final BindingResult bindingResult = new BeanPropertyBindingResult(studyUsersDTO, "StudyUsersDTO");
		final BindingResult changeReasonBindingResult = new BeanPropertyBindingResult(changeReason, "ChangeReason");

		final StudyUserDTO studyUserDTO = studyUserMapper.toStudyUserDTO(study, user);
		studyUserDTO.getSitePermissionBundles().get(study.getSites().get(0).getGuiName()).add(PermissionBundle.RANDOMIZE_SUBJECTS);
		studyUsersDTO.getStudyUserDTOs().add(studyUserDTO);

		final String path = studyController.editUsers(null, studyUsersDTO, bindingResult, changeReason,
		                                              changeReasonBindingResult, redirectAttributes);

        testLastAuditEntryForStudy(study.getId(), AuditType.UPDATE, "Added permissions");

		assertEquals("redirect:/studies", path, "Returned wrong path!");
		assertTrue(study.getAssignedUsers().contains(user), "User has not been assigned to the study!");
		assertTrue(user.getAssignedStudies().contains(study), "Study has not been assigned to the user!");

		final AclEntry readStudy = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study,
		                                                                                          user.getAclSid(),
		                                                                                          PermissionType.READ_STUDY);
		assertNotNull(readStudy, "User has no READ_STUDY permission!");

		final AclEntry createSubject = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study.getSites().get(0),
				user.getAclSid(),
				PermissionType.CREATE_SUBJECT);
        assertNotNull(createSubject, "User has no CREATE_SUBJECT permission!");

        final AclEntry readSubject = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study.getSites().get(0),
                user.getAclSid(),
                PermissionType.READ_SUBJECT);
        assertNotNull(readSubject, "User has no READ_SUBJECT permission!");

        final AclEntry readAuditSimple = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study,
                user.getAclSid(),
                PermissionType.READ_AUDIT_SIMPLE);
        assertNotNull(readAuditSimple, "User has no READ_AUDIT_SIMPLE permission!");
	}

    @Test
    public void editUsersPostRemoveUserTest() {
        addUserPermissionsToActiveStudy();

        final Study study = getActiveStudy();
        final RandimiUser user = getActiveUser();
        final StudyUsersDTO studyUsersDTO = studyUsersMapper.toStudyUsersDTO(study);
	    final ChangeReason changeReason = new ChangeReason("Removed permissions", auditService.getOldDto(studyUsersDTO));

	    final BindingResult bindingResult = new BeanPropertyBindingResult(studyUsersDTO, "StudyUsersDTO");
	    final BindingResult changeReasonBindingResult = new BeanPropertyBindingResult(changeReason, "ChangeReason");

        studyUsersDTO.getStudyUserDTOs().removeIf(studyUserDTO -> studyUserDTO.getUserId() == user.getId());

	    final String path = studyController.editUsers(null, studyUsersDTO, bindingResult, changeReason,
	                                                  changeReasonBindingResult, redirectAttributes);

        testLastAuditEntryForStudy(study.getId(), AuditType.UPDATE, "Removed permissions");

        assertEquals("redirect:/studies", path, "Returned wrong path!");
        assertFalse(study.getAssignedUsers().contains(user), "User has not been removed from the study!");
        assertFalse(user.getAssignedStudies().contains(study), "Study has not been removed from the user!");

        final AclEntry readStudy = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study,
                user.getAclSid(),
                PermissionType.READ_STUDY);
        assertNull(readStudy, "User has READ_STUDY permission!");

        final AclEntry createSubject = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study.getSites().get(0),
                user.getAclSid(),
                PermissionType.CREATE_SUBJECT);
        assertNull(createSubject, "User has CREATE_SUBJECT permission!");

        final AclEntry readSubject = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study.getSites().get(0),
                user.getAclSid(),
                PermissionType.READ_SUBJECT);
        assertNull(readSubject, "User has READ_SUBJECT permission!");

        final AclEntry readAuditSimple = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(study,
                user.getAclSid(),
                PermissionType.READ_AUDIT_SIMPLE);
        assertNull(readAuditSimple, "User has READ_AUDIT_SIMPLE permission!");
    }

	private Study createPostTest(StudyDTO studyDTO) {
		final BindingResult bindingResultStudy = new BeanPropertyBindingResult(studyDTO, "StudyDTO");

		String path = studyController.create("save", studyDTO, bindingResultStudy, redirectAttributes);
		assertEquals("redirect:/studies", path, "Returned wrong path!");

		List<Study> studies = studyRepository.findByGuiName(STUDY_GUI_NAME);
		assertEquals(1, studies.size(), "Study was not persisted!");

		Study postedStudy = studies.get(0);

		assertNotNull(aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(), null), postedStudy.getId()),
				"AclObjectIdentity not created!");
		assertFalse(postedStudy.isActive(), "Study should not be activated!");
		assertNull(postedStudy.getActivationDate(), "Study should not be activated!");

		List<AuditEntry> auditEntries = auditEntryRepository.findByStudyId(postedStudy.getId());
		assertEquals(AuditType.CREATE, auditEntries.get(auditEntries.size() - 1).getAuditType(),
				"AuditEntry has not been created!");

		testLastAuditEntryForStudy(postedStudy.getId(), AuditType.CREATE, null);

		return postedStudy;
	}

	private void addUserPermissionsToActiveStudy() {
		final Study study = getActiveStudy();
		final RandimiUser user = getActiveUser();
		final StudyUsersDTO studyUsersDTO = studyUsersMapper.toStudyUsersDTO(study);
		final ChangeReason changeReason = new ChangeReason("Added permission", auditService.getOldDto(studyUsersDTO));
		final BindingResult bindingResult = new BeanPropertyBindingResult(studyUsersDTO, "StudyUsersDTO");
		final BindingResult changeReasonBindingResult = new BeanPropertyBindingResult(changeReason, "ChangeReason");
		final StudyUserDTO studyUserDTO = studyUserMapper.toStudyUserDTO(study, user);
		studyUserDTO.getSitePermissionBundles().get(study.getSites().get(0).getGuiName())
		            .add(PermissionBundle.RANDOMIZE_SUBJECTS);
		studyUsersDTO.getStudyUserDTOs().add(studyUserDTO);
		studyController.editUsers(null, studyUsersDTO, bindingResult, changeReason, changeReasonBindingResult,
		                          redirectAttributes);
		init();
	}

	private void testStratumParts(final List<StratumPartBase> parts, final List<Pair<String, String>> expectedParts) {
		assertEquals(expectedParts.size(), parts.size(), "Wrong number of stratum parts!");

		for (int i = 0; i < expectedParts.size(); i++) {
			final StratumPartBase part = parts.get(i);
			final Pair<String, String> expectedPart = expectedParts.get(i);
			assertEquals(expectedPart.getFirst(), part.getStratum().getName(), "Wrong stratum!");
			assertEquals(expectedPart.getSecond(), part.getPartKey(), "Wrong stratum part!");
		}

	}

	private ResultMatcher validateBindingResult(final String fieldName) {
		return mvcResult -> {
			assertTrue(mvcResult.getFlashMap().containsKey(BindingResult.MODEL_KEY_PREFIX + StudyController.STUDY_KEY));
			final BindingResult response = (BindingResult) mvcResult.getFlashMap().get(BindingResult.MODEL_KEY_PREFIX +
			                                                                           StudyController.STUDY_KEY);
			assertTrue(response.hasFieldErrors(fieldName));
		};
	}
}
