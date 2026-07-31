package de.unimuenster.imi.randimi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.subject.DeleteSubjectDTO;
import de.unimuenster.imi.randimi.dto.subject.EditSubjectPseudonymDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.enumeration.SubjectStatus;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;

/**
 * @author Daniel Preciado-Marquez
 */
@Transactional
public class SubjectControllerTest extends MVCControllerTestBase {

	@Autowired
	SubjectController subjectController;

	@Autowired
	StratumCodeService stratumCodeService;

	@Autowired
	SubjectRepository subjectRepository;

	@Test
	public void addGetInvalidIdTest() {
		String path = subjectController.add(0L, 0L, model, redirectAttributes, null);
		assertEquals("redirect:/studies", path, "Returned wrong path!");
	}

	@Test
	public void addGetValidIdTest() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/subjects/add").with(csrf())
		                                    .param("id", Long.toString(activeStudy.getId())))
		       .andExpect(view().name("/subjects/add"))
		       .andExpect(model().attributeExists("study"))
		       .andExpect(result -> assertEquals(activeStudy.getId(),
		                                         ((StudyDTO) result.getModelAndView().getModel().get("study")).getId()))
		       .andExpect(model().attributeExists("subject"));
	}

	@Test
	public void addPostCancelTest() {
		SubjectDTO subjectDTO = new SubjectDTO();
		String path = subjectController.addCancel(null, subjectDTO);
		assertEquals("redirect:/studies/" + subjectDTO.getStudyApiId() + "/subject-lists", path, "Returned wrong path!");
	}

	@Test
	public void addPostNoStratificationCoinTossTest() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();
		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		final int randomCalls = study.getSites().get(0).getRandomCalls();

		mockMvc.perform(post("/subjects/add").with(csrf())
		                                     .param("action", "add")
		                                     .param("id", Long.toString(study.getId()))
		                                     .flashAttr("subject", subjectDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists"))
		       .andExpect(flash().attributeExists("success"));

		assertEquals(1, study.getSubjectLists().get(0).size(), "Subject has not been created!");

		Subject subject = study.getSubjectLists().get(0).getSubjects().get(0);
		assertEquals(1, subject.getOrderNumber(), "Order number does not match!");
		assertEquals("pseudonym-0", subject.getPseudonym(), "Pseudonym does not match!");
		assertEquals("Site A", subject.getSite().getGuiName(), "Location does not match");
		assertEquals(randomCalls + 1, study.getSites().get(0).getRandomCalls(), "Random call has not been increased!");
		assertEquals(study.getStudyArms().get(1), subject.getStudyArm(), "StudyArm does not match!");
		assertEquals(SubjectStatus.ACTIVE, subject.getStatus(), "Status should be 'ACTIVE'!");
		assertNotNull(subject.getRandomizationTimestamp(), "Randomization timestamp should not be null!");
		assertNull(subject.getDeletionTimestamp(), "Deletion timestamp should be null!");
		assertNull(subject.getReleaseTimestamp(), "Deletion timestamp should be null!");

		testLastAuditEntryForSubject(study.getId(), AuditType.CREATE, null, subject.getId());
	}

	@Test
	public void addPostNoStratificationCoinTossExtendedTest() {
		Study study = createAndActivateStudyNoStratificationCointoss();

		Site siteA = study.getSites().get(0);
		Site siteB = study.getSites().get(1);

		for (int i = 0; i < siteA.getCapacity(); i++) {
			RedirectAttributes ra = new RedirectAttributesModelMap();
			SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, siteA);
			BindingResult bindingResult = new BeanPropertyBindingResult(subjectDTO, "SubjectDTO");
			subjectController.add("save", null, subjectDTO, bindingResult, ra);
			assertTrue(ra.getFlashAttributes().containsKey("success"), "Creation of subject was not successfully!");
		}

		{
			RedirectAttributes ra = new RedirectAttributesModelMap();
			SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, siteA);
			BindingResult bindingResult = new BeanPropertyBindingResult(subjectDTO, "SubjectDTO");

			subjectController.add("save", null, subjectDTO, bindingResult, ra);
			assertTrue(ra.getFlashAttributes().containsKey("error"),
					"Subject should not have been created because of the reached site capacity!");

			String expectedErrorMessage = buildErrorMessage(RandimiException.NOT_ACCEPTABLE_SITE_FULL,
					"study.error.siteFull", siteA.getGuiName());
			assertEquals(expectedErrorMessage, ra.getFlashAttributes().get("error"), "Wrong error message");
		}

		Study updatedStudy = studyRepository.findById(study.getId()).get();
		assertEquals(siteA.getCapacity(), updatedStudy.getSubjectLists().get(0).size(),
				"Number of created subjects is wrong!");

		for (int i = siteA.getCapacity(); i < study.getCapacity(); i++) {
			RedirectAttributes ra = new RedirectAttributesModelMap();
			SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, siteB);
			BindingResult bindingResult = new BeanPropertyBindingResult(subjectDTO, "SubjectDTO");
			subjectController.add("save", null, subjectDTO, bindingResult, ra);
			assertTrue(ra.getFlashAttributes().containsKey("success"), "Creation of subject was not successfully!");
		}

		{
			RedirectAttributes ra = new RedirectAttributesModelMap();
			SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, siteB);
			BindingResult bindingResult = new BeanPropertyBindingResult(subjectDTO, "SubjectDTO");

			subjectController.add("save", null, subjectDTO, bindingResult, ra);
			assertTrue(ra.getFlashAttributes().containsKey("error"),
					"Subject should not have been created because of the reached study capacity!");

			String expectedErrorMessage = buildErrorMessage(RandimiException.NOT_ACCEPTABLE_STUDY_FULL,
					"study.error.studyFull", study.getGuiName());
			assertEquals(expectedErrorMessage, ra.getFlashAttributes().get("error"), "Wrong error message");
		}

		updatedStudy = studyRepository.findById(study.getId()).get();
		assertEquals(study.getCapacity(), updatedStudy.getSubjectLists().get(0).size(),
				"Number of created subjects is wrong!");
	}

	@Test
	public void addPostStratificationNotBySiteCointossTest() {
		Study study = createAndActivateStudyStratificationNotBySiteCointoss();
		SubjectDTO subjectDTO = getValidSubjectDTOStratificationNotBySite(study, study.getSites().get(0), "A1",
				"B2");
		testSubject(subjectDTO, study, 1, 0);
	}

	@Test
	public void addPostStratificationOnlyBySiteCointossTest() {
		Study study = createAndActivateStudyStratificationOnlyBySiteCointoss();
		SubjectDTO subjectDTO = getValidSubjectDTOStratificationOnlyBySite(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 1, 0);
	}

	@Test
	public void addPostStratificationCointossTest() {
		Study study = createAndActivateStudyStratificationCointoss();
		SubjectDTO subjectDTO = getValidSubjectDTOStratification(study, study.getSites().get(0), "A1", "B2");
		testSubject(subjectDTO, study, 1, 0);
	}

	@Test
	public void addPostNoStratificationBlockedTest() {
		Study study = createAndActivateStudyNoStratificationBlocked();

		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 0, 0);
		assertArrayEquals(new Integer[] { 1, 2 }, study.getSubjectLists().get(0).getRemainingAssignments(),
				"Remaining assignments have not been updated!");

		subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 1, 1);
		assertArrayEquals(new Integer[] { 1, 1 }, study.getSubjectLists().get(0).getRemainingAssignments(),
				"Remaining assignments have not been updated!");

		subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 0, 2);
		assertArrayEquals(new Integer[] { 0, 1 }, study.getSubjectLists().get(0).getRemainingAssignments(),
				"Remaining assignments have not been updated!");

		subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 1, 3);
		assertArrayEquals(new Integer[] { 0, 0 }, study.getSubjectLists().get(0).getRemainingAssignments(),
				"Remaining assignments have not been updated!");

		subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 1, 4);
		assertArrayEquals(new Integer[] { 2, 1 }, study.getSubjectLists().get(0).getRemainingAssignments(),
				"Remaining assignments have not been updated!");
	}

	@Test
	public void addPostNoStratificationBlockedPreGeneratedTest() {
		Study study = createAndActivateStudyNoStratificationBlockedPreGenerate();

		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 0, 0);
		assertEquals(study.getCapacity(), study.getSubjectLists().get(0).getSubjects().size(),
		             "Size of the list should not be different!");
		Subject first = study.getSubjectLists().get(0).getSubjects().get(0);
		assertEquals(SubjectStatus.ACTIVE, first.getStatus(), "Status has not been set correctly!");
		assertEquals(subjectDTO.getPseudonym(), first.getPseudonym(), "Pseudonym has not been set correctly!");

		Subject second = study.getSubjectLists().get(0).getSubjects().get(1);
		assertEquals(SubjectStatus.PRE_GENERATED, second.getStatus(), "Status should not have been modified!");
		assertNull(second.getPseudonym(), "Pseudonym should not have been modified!");
	}

	@Test
	public void addPostNoStratificationBlockedRatioTest() {
		Study study = createAndActivateStudyNoStratificationBlockedRatio();

		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 0, 0);
		assertArrayEquals(new Integer[] { 2, 1 }, study.getSubjectLists().get(0).getRemainingAssignments(),
		                  "Remaining assignments have not been updated!");

		subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 1, 1);
		assertArrayEquals(new Integer[] { 2, 0 }, study.getSubjectLists().get(0).getRemainingAssignments(),
		                  "Remaining assignments have not been updated!");

		subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 0, 2);
		assertArrayEquals(new Integer[] { 1, 0 }, study.getSubjectLists().get(0).getRemainingAssignments(),
		                  "Remaining assignments have not been updated!");

		subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 0, 3);
		assertArrayEquals(new Integer[] { 0, 0 }, study.getSubjectLists().get(0).getRemainingAssignments(),
		                  "Remaining assignments have not been updated!");

		subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		testSubject(subjectDTO, study, 1, 4);
		assertArrayEquals(new Integer[] { 3, 0 }, study.getSubjectLists().get(0).getRemainingAssignments(),
		                  "Remaining assignments have not been updated!");
	}

//	@Test
//	public void showInvalidSubjectIdTest() {
//		final Study study = getActiveStudy();
//
//		final String path = subjectController.show(study.getId(), 0L, null, model, redirectAttributes);
//
//		assertEquals("redirect:/subjects/view", path, "Returned unexpected path!");
//		assertEquals(study.getId(), Long.parseLong((String) redirectAttributes.getAttribute("id")),
//				"Response contains unexpected study id!");
//		assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"), "Response does not contain an error!");
//	}

//	@Test
//	public void showTest() {
//		final Study study = getActiveStudy();
//		final Subject subject = study.getSubjectLists().get(0).getSubjects().get(0);
//
//		final String path = subjectController.show(study.getId(), subject.getId(), null, model, redirectAttributes);
//
//		assertEquals("/subjects/show", path, "Returned unexpected path!");
//		assertEquals(study.getId(), model.getAttribute("studyId"), "Response contains unexpected study id!");
//		assertTrue(model.containsAttribute("auditEntries"), "Response does not contain audit entries!");
//	}

	@Test
	public void removeInvalidDeleteSubjectDTOTest() throws Exception {
		final DeleteSubjectDTO deleteSubjectDTO = new DeleteSubjectDTO();
		mockMvc.perform(post("/subjects/remove")
				                .with(csrf())
				                .header("referer", "id=1")
				                .flashAttr(SubjectController.DELETE_SUBJECT_DTO_KEY, deleteSubjectDTO))
		       .andExpect(redirectedUrl("/studies/null/subject-lists/0"))
		       .andExpect(flash().attributeExists("error"));
	}

	@RepeatedTest(value = 3, failureThreshold = 2)
	public void removeNoReleaseTest() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();
		Site site = study.getSites().get(0);
		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
		Subject subject = testSubject(subjectDTO, study, 1, 0);

		final ChangeReason changeReason = new ChangeReason("Delete", null);
		final DeleteSubjectDTO deleteSubjectDTO = new DeleteSubjectDTO(changeReason, false, study.getId(),
		                                                               study.getApiId(), site.getId(),
		                                                               subject.getSubjectList().getId(),
		                                                               subject.getId());

		mockMvc.perform(post("/subjects/remove").with(csrf())
		                                        .flashAttr(SubjectController.DELETE_SUBJECT_DTO_KEY, deleteSubjectDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists/" + subject.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("success"));

		testLastAuditEntryForSubject(study.getId(), AuditType.DELETE, "Delete", subject.getId());

		Optional<Subject> updatedSubject = subjectRepository.findById(subject.getId());
		assertTrue(updatedSubject.isPresent(), "Subject should still be present!");
		assertEquals(SubjectStatus.DELETED, updatedSubject.get().getStatus(), "Subject should be marked as deleted!");
		assertNotNull(updatedSubject.get().getRandomizationTimestamp(), "Randomization timestamp should not be null!");
		assertNotNull(updatedSubject.get().getDeletionTimestamp(), "Deletion timestamp should not be null!");
		assertNull(updatedSubject.get().getReleaseTimestamp(), "Deletion timestamp should be null!");
	}

	@Test
	public void removeReleaseCointossTest() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();
		Site site = study.getSites().get(0);
		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
		Subject subject = testSubject(subjectDTO, study, 1, 0);

		final ChangeReason changeReason = new ChangeReason("Delete", null);
		final DeleteSubjectDTO deleteSubjectDTO = new DeleteSubjectDTO(changeReason, true, study.getId(),
		                                                               study.getApiId(), site.getId(),
		                                                               subject.getSubjectList().getId(),
		                                                               subject.getId());

		mockMvc.perform(post("/subjects/remove").with(csrf())
		                                        .flashAttr(SubjectController.DELETE_SUBJECT_DTO_KEY, deleteSubjectDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists/" + subject.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("success"));

		testLastAuditEntryForSubject(study.getId(), AuditType.RELEASE_SUBJECT, "Delete", subject.getId());

		Optional<Subject> updatedSubject = subjectRepository.findById(subject.getId());
		assertTrue(updatedSubject.isPresent(), "Subject should still be present!");
		assertEquals(SubjectStatus.RELEASED, updatedSubject.get().getStatus(), "Subject should be marked as released!");
		assertNotNull(updatedSubject.get().getRandomizationTimestamp(), "Randomization timestamp should not be null!");
		assertNotNull(updatedSubject.get().getDeletionTimestamp(), "Deletion timestamp should not be null!");
		assertNotNull(updatedSubject.get().getReleaseTimestamp(), "Deletion timestamp should not be null!");
	}

	@Test
	public void removeReleaseBlockedTest() {
		Study study = createAndActivateStudyNoStratificationBlocked();
		Site site = study.getSites().get(0);
		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
		Subject subject = testSubject(subjectDTO, study, 0, 0);
		SubjectList subjectList = subject.getSubjectList();

		final ChangeReason changeReason = new ChangeReason("Delete", null);
		final DeleteSubjectDTO deleteSubjectDTO = new DeleteSubjectDTO(changeReason, true, study.getId(),
		                                                               study.getApiId(), site.getId(),
		                                                               subjectList.getId(), subject.getId());
		BindingResult bindingResult = new BeanPropertyBindingResult(deleteSubjectDTO, "DeleteSubjectDTO");

		String path = subjectController.remove(deleteSubjectDTO, bindingResult, redirectAttributes);
		assertEquals("redirect:/studies/" + study.getApiId() + "/subject-lists/" + subjectList.getId(), path,
		             "Returned unexpected path!");
		assertTrue(redirectAttributes.getFlashAttributes().containsKey("success"),
				"Response does not contain a success!");
		testLastAuditEntryForSubject(study.getId(), AuditType.RELEASE_SUBJECT, "Delete", subject.getId());

		Optional<Subject> updatedSubject = subjectRepository.findById(subject.getId());
		assertTrue(updatedSubject.isPresent(), "Subject should still be present!");
		assertEquals(SubjectStatus.RELEASED, updatedSubject.get().getStatus(), "Subject should be marked as released!");
		assertNotNull(updatedSubject.get().getRandomizationTimestamp(), "Randomization timestamp should not be null!");
		assertNotNull(updatedSubject.get().getDeletionTimestamp(), "Deletion timestamp should not be null!");
		assertNotNull(updatedSubject.get().getReleaseTimestamp(), "Deletion timestamp should not be null!");

		assertArrayEquals(new Integer[] { 2, 2 }, subjectList.getRemainingAssignments(),
				"Study arm has not been added to the remaining assignments!");
	}

	@Test
	public void removeReleaseBlockedPreGenerate() {
		Study study = createAndActivateStudyNoStratificationBlockedPreGenerate();
		Site site = study.getSites().get(0);
		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, study.getSites().get(0));
		Subject subject = testSubject(subjectDTO, study, 0, 0);
		SubjectList subjectList = subject.getSubjectList();
		int subjectListSize = subjectList.size();

		final ChangeReason changeReason = new ChangeReason("Delete", null);
		final DeleteSubjectDTO deleteSubjectDTO = new DeleteSubjectDTO(changeReason, true, study.getId(),
		                                                               study.getApiId(), site.getId(),
		                                                               subjectList.getId(), subject.getId());
		BindingResult bindingResult = new BeanPropertyBindingResult(deleteSubjectDTO, "DeleteSubjectDTO");

		String path = subjectController.remove(deleteSubjectDTO, bindingResult, redirectAttributes);
		assertEquals("redirect:/studies/" + study.getApiId() + "/subject-lists/" + subjectList.getId(), path,
		             "Returned unexpected path!");
		assertTrue(redirectAttributes.getFlashAttributes().containsKey("success"),
		           "Response does not contain a success!");
		testLastAuditEntryForSubject(study.getId(), AuditType.RELEASE_SUBJECT, "Delete", subject.getId());

		Optional<Subject> updatedSubject = subjectRepository.findById(subject.getId());
		assertTrue(updatedSubject.isPresent(), "Subject should still be present!");
		assertEquals(SubjectStatus.RELEASED, updatedSubject.get().getStatus(), "Subject should be marked as released!");
		assertNotNull(updatedSubject.get().getRandomizationTimestamp(), "Randomization timestamp should not be null!");
		assertNotNull(updatedSubject.get().getDeletionTimestamp(), "Deletion timestamp should not be null!");
		assertNotNull(updatedSubject.get().getReleaseTimestamp(), "Deletion timestamp should not be null!");

		SubjectList updatedSubjectList = updatedSubject.get().getSubjectList();
		assertEquals(subjectListSize + 1, updatedSubjectList.size(), "New entry has not been added!");

		Subject newSubject = updatedSubjectList.getSubjects().get(subjectListSize);
		assertEquals(subject.getStudyArm(), newSubject.getStudyArm(), "New entry has not been assigned to the same study arm!");
		assertEquals(SubjectStatus.PRE_GENERATED, newSubject.getStatus(), "New entry has the wrong status!");
	}

	@Test
	public void releaseRemovedSubject() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();
		Site site = study.getSites().get(0);
		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
		Subject subject = testSubject(subjectDTO, study, 1, 0);

		// Delete subject
		final ChangeReason changeReason = new ChangeReason("Delete", null);
		final DeleteSubjectDTO deleteSubjectDTO = new DeleteSubjectDTO(changeReason, false, study.getId(),
		                                                               study.getApiId(), site.getId(),
		                                                               subject.getSubjectList().getId(),
		                                                               subject.getId());

		mockMvc.perform(post("/subjects/remove").with(csrf())
		                                        .flashAttr(SubjectController.DELETE_SUBJECT_DTO_KEY, deleteSubjectDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists/" + subject.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("success"));

		// Test deleted subject
		Optional<Subject> updatedSubject = subjectRepository.findById(subject.getId());
		assertTrue(updatedSubject.isPresent(), "Subject should still be present!");
		assertEquals(SubjectStatus.DELETED, updatedSubject.get().getStatus(), "Subject should be marked as released!");
		assertNotNull(updatedSubject.get().getRandomizationTimestamp(), "Randomization timestamp should not be null!");
		assertNotNull(updatedSubject.get().getDeletionTimestamp(), "Deletion timestamp should not be null!");
		assertNull(updatedSubject.get().getReleaseTimestamp(), "Deletion timestamp should be null!");

		var randomizationTimestamp = updatedSubject.get().getRandomizationTimestamp();
		var deletionTimestamp = updatedSubject.get().getDeletionTimestamp();

		// Release subject
		deleteSubjectDTO.setRelease(true);
		mockMvc.perform(post("/subjects/remove").with(csrf())
		                                        .flashAttr(SubjectController.DELETE_SUBJECT_DTO_KEY, deleteSubjectDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists/" + subject.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("success"));

		// Test released subject
		updatedSubject = subjectRepository.findById(subject.getId());
		assertTrue(updatedSubject.isPresent(), "Subject should still be present!");
		assertEquals(SubjectStatus.RELEASED, updatedSubject.get().getStatus(), "Subject should be marked as released!");
		assertEquals(randomizationTimestamp, updatedSubject.get().getRandomizationTimestamp(), "Randomization timestamp should be the same!");
		assertEquals(deletionTimestamp, updatedSubject.get().getDeletionTimestamp(), "Deletion timestamp should be the same!");
		assertNotNull(updatedSubject.get().getReleaseTimestamp(), "Deletion timestamp should not be null!");
	}

	@Test
	public void removeFromArchivedStudy() throws Exception {
		final Study archivedStudy = getArchivedStudy();
		final Subject subject = archivedStudy.getSubjectLists().get(0).getSubjects().get(0);

		final ChangeReason changeReason = new ChangeReason("Delete", null);
		final DeleteSubjectDTO deleteSubjectDTO = new DeleteSubjectDTO(changeReason, true, archivedStudy.getId(),
		                                                               archivedStudy.getApiId(),
		                                                               subject.getSite().getId(),
		                                                               subject.getSubjectList().getId(),
		                                                               subject.getId());
		mockMvc.perform(post("/subjects/remove").with(csrf())
		                                        .flashAttr(SubjectController.DELETE_SUBJECT_DTO_KEY, deleteSubjectDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + archivedStudy.getApiId() + "/subject-lists/" + subject.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("error"));
	}

	@Test
	public void editPseudonymInvalidDTOTest() throws Exception {
		final EditSubjectPseudonymDTO editSubjectPseudonymDTO = new EditSubjectPseudonymDTO();
		mockMvc.perform(post("/subjects/edit-pseudonym")
		                            .with(csrf())
		                            .flashAttr(SubjectController.EDIT_SUBJECT_PSEUDONYM_DTO_KEY, editSubjectPseudonymDTO))
		       .andExpect(redirectedUrl("/studies/null/subject-lists/0"))
		       .andExpect(flash().attributeExists("error"));
	}

	@Test
	public void editPseudonymSuccessTest() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();
		Site site = study.getSites().get(0);
		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
		Subject subject = testSubject(subjectDTO, study, 1, 0);
		String oldPseudonym = subject.getPseudonym();

		final String newPseudonym = "new-pseudonym-edit";
		final ChangeReason changeReason = new ChangeReason("Pseudonym edit", null);
		final EditSubjectPseudonymDTO editSubjectPseudonymDTO = new EditSubjectPseudonymDTO();
		editSubjectPseudonymDTO.setChangeReason(changeReason);
		editSubjectPseudonymDTO.setPseudonym(newPseudonym);
		editSubjectPseudonymDTO.setStudyId(study.getId());
		editSubjectPseudonymDTO.setStudyApiId(study.getApiId());
		editSubjectPseudonymDTO.setSubjectListId(subject.getSubjectList().getId());
		editSubjectPseudonymDTO.setSubjectId(subject.getId());

		mockMvc.perform(post("/subjects/edit-pseudonym").with(csrf())
		                                                .flashAttr(SubjectController.EDIT_SUBJECT_PSEUDONYM_DTO_KEY, editSubjectPseudonymDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists/" + subject.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("success"));

		testLastAuditEntryForSubject(study.getId(), AuditType.UPDATE, "Pseudonym edit", subject.getId());

		Optional<Subject> updatedSubject = subjectRepository.findById(subject.getId());
		assertTrue(updatedSubject.isPresent(), "Subject should still be present!");
		assertEquals(newPseudonym, updatedSubject.get().getPseudonym(), "Pseudonym should have been updated!");
		assertNotEquals(oldPseudonym, updatedSubject.get().getPseudonym(), "Pseudonym should have changed!");
	}

	@Test
	public void editPseudonymArchivedStudy() throws Exception {
		final Study archivedStudy = getArchivedStudy();
		final Subject subject = archivedStudy.getSubjectLists().get(0).getSubjects().get(0);

		final ChangeReason changeReason = new ChangeReason("Pseudonym edit", null);
		final EditSubjectPseudonymDTO editSubjectPseudonymDTO = new EditSubjectPseudonymDTO();
		editSubjectPseudonymDTO.setChangeReason(changeReason);
		editSubjectPseudonymDTO.setPseudonym("edited-pseudonym");
		editSubjectPseudonymDTO.setStudyId(archivedStudy.getId());
		editSubjectPseudonymDTO.setStudyApiId(archivedStudy.getApiId());
		editSubjectPseudonymDTO.setSubjectListId(subject.getSubjectList().getId());
		editSubjectPseudonymDTO.setSubjectId(subject.getId());

		mockMvc.perform(post("/subjects/edit-pseudonym").with(csrf())
		                                                .flashAttr(SubjectController.EDIT_SUBJECT_PSEUDONYM_DTO_KEY, editSubjectPseudonymDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + archivedStudy.getApiId() + "/subject-lists/" + subject.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("error"));

		Optional<Subject> updatedSubject = subjectRepository.findById(subject.getId());
		assertNotEquals("edited-pseudonym", updatedSubject.get().getPseudonym(),
		                "Pseudonym should not have been updated for archived study!");
	}

	@Test
	public void editPseudonymNonExistentSubject() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();

		final ChangeReason changeReason = new ChangeReason("Pseudonym edit", null);
		final EditSubjectPseudonymDTO editSubjectPseudonymDTO = new EditSubjectPseudonymDTO();
		editSubjectPseudonymDTO.setChangeReason(changeReason);
		editSubjectPseudonymDTO.setPseudonym("edited-pseudonym");
		editSubjectPseudonymDTO.setStudyId(study.getId());
		editSubjectPseudonymDTO.setStudyApiId(study.getApiId());
		editSubjectPseudonymDTO.setSubjectListId(0L);
		editSubjectPseudonymDTO.setSubjectId(0L);

		mockMvc.perform(post("/subjects/edit-pseudonym").with(csrf())
		                                                .flashAttr(SubjectController.EDIT_SUBJECT_PSEUDONYM_DTO_KEY, editSubjectPseudonymDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists/0"))
		       .andExpect(flash().attributeExists("error"));
	}

	@Test
	public void editPseudonymDuplicateInLocationTest() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();
		Site site = study.getSites().get(0);
		SubjectDTO subjectDTO1 = getValidSubjectDTONoStratification(study, site);
		Subject subject1 = testSubject(subjectDTO1, study, 1, 0);
		SubjectDTO subjectDTO2 = getValidSubjectDTONoStratification(study, site);
		Subject subject2 = testSubject(subjectDTO2, study, 0, 1);

		String pseudonymToUse = subject1.getPseudonym();

		final ChangeReason changeReason = new ChangeReason("Pseudonym edit", null);
		final EditSubjectPseudonymDTO editSubjectPseudonymDTO = new EditSubjectPseudonymDTO();
		editSubjectPseudonymDTO.setChangeReason(changeReason);
		editSubjectPseudonymDTO.setPseudonym(pseudonymToUse);
		editSubjectPseudonymDTO.setStudyId(study.getId());
		editSubjectPseudonymDTO.setStudyApiId(study.getApiId());
		editSubjectPseudonymDTO.setSubjectListId(subject2.getSubjectList().getId());
		editSubjectPseudonymDTO.setSubjectId(subject2.getId());

		mockMvc.perform(post("/subjects/edit-pseudonym").with(csrf())
		                                                .flashAttr(SubjectController.EDIT_SUBJECT_PSEUDONYM_DTO_KEY, editSubjectPseudonymDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists/" + subject2.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("error"));

		Optional<Subject> updatedSubject = subjectRepository.findById(subject2.getId());
		assertNotEquals(pseudonymToUse, updatedSubject.get().getPseudonym(),
		                "Pseudonym should not have been updated to a duplicate value!");
	}

	@Test
	public void editPseudonymSamePseudonymAllowedTest() throws Exception {
		Study study = createAndActivateStudyNoStratificationCointoss();
		Site site = study.getSites().get(0);
		SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
		Subject subject = testSubject(subjectDTO, study, 1, 0);
		String currentPseudonym = subject.getPseudonym();

		final ChangeReason changeReason = new ChangeReason("Pseudonym edit", null);
		final EditSubjectPseudonymDTO editSubjectPseudonymDTO = new EditSubjectPseudonymDTO();
		editSubjectPseudonymDTO.setChangeReason(changeReason);
		editSubjectPseudonymDTO.setPseudonym(currentPseudonym);
		editSubjectPseudonymDTO.setStudyId(study.getId());
		editSubjectPseudonymDTO.setStudyApiId(study.getApiId());
		editSubjectPseudonymDTO.setSubjectListId(subject.getSubjectList().getId());
		editSubjectPseudonymDTO.setSubjectId(subject.getId());

		mockMvc.perform(post("/subjects/edit-pseudonym").with(csrf())
		                                                .flashAttr(SubjectController.EDIT_SUBJECT_PSEUDONYM_DTO_KEY, editSubjectPseudonymDTO))
		       .andDo(print())
		       .andExpect(redirectedUrl("/studies/" + study.getApiId() + "/subject-lists/" + subject.getSubjectList().getId()))
		       .andExpect(flash().attributeExists("success"));

		Optional<Subject> updatedSubject = subjectRepository.findById(subject.getId());
		assertEquals(currentPseudonym, updatedSubject.get().getPseudonym(),
		             "Pseudonym should remain the same!");
	}

	private Subject testSubject(SubjectDTO subjectDTO, Study study, int assignedStudyArmIndex, int subjectIndex) {
		BindingResult bindingResult = new BeanPropertyBindingResult(subjectDTO, "SubjectDTO");
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		String path = subjectController.add("save", null, subjectDTO, bindingResult, redirectAttributes);
		assertEquals("redirect:/studies/" + study.getApiId() + "/subject-lists", path, "Returned wrong path!");
		assertTrue(redirectAttributes.getFlashAttributes().containsKey("success"),
				"Redirect attributes should contain attribute 'success'!");

		SubjectList subjectList = assertDoesNotThrow(() -> stratumCodeService.getSubjectListForSubject(subjectDTO, study).get());

		if (study.getPreGenerateSubjectList()) {
			assertEquals(study.getCapacity(), subjectList.size(), "Subject has not been created!");
		} else {
			assertEquals(subjectIndex + 1, subjectList.size(), "Subject has not been created!");
		}

		Subject subject = subjectList.getSubjects().get(subjectIndex);
		assertEquals(subjectIndex + 1, subject.getOrderNumber(), "Order number does not match!");
		assertEquals(SubjectStatus.ACTIVE, subject.getStatus(), "Status does not match!");
		assertEquals("pseudonym-" + subjectIndex, subject.getPseudonym(), "Pseudonym does not match!");
		assertEquals("Site A", subject.getSite().getGuiName(), "Location does not match");
		assertEquals(study.getStudyArms().get(assignedStudyArmIndex), subject.getStudyArm(),
				"StudyArm does not match!");
		assertNotNull(subject.getRandomizationTimestamp(), "Randomization timestamp should not be null!");
		assertNull(subject.getDeletionTimestamp(), "Deletion timestamp should be null!");
		assertNull(subject.getReleaseTimestamp(), "Deletion timestamp should be null!");

		testLastAuditEntryForSubject(study.getId(), AuditType.CREATE, null, subject.getId());

		return subject;
	}

}
