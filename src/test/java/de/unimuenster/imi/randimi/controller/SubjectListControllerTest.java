package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectListDTO;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.enumeration.Delimiter;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SubjectListControllerTest extends MVCControllerTestBase {

	@Test
	public void listGet() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists"))
		       .andExpect(status().isOk())
		       .andExpect(view().name("/subjects/list"))
		       .andExpect(model().attributeExists("capacities"))
		       .andExpect(model().attributeExists("study"))
		       .andExpect(result -> {
			       assertEquals(activeStudy.getId(),
			                    ((StudyDTO) result.getModelAndView().getModel().get("study")).getId(),
			                    "Wrong study!");

		       })
		       .andExpect(model().attributeExists("subjectLists"));

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ_SUBJECTS, null);
	}

	@Test
	public void listInvalidStudyId() throws Exception {
		mockMvc.perform(get("/studies/0/subject-lists"))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/studies"))
		       .andExpect(model().attributeExists("error"))
		       .andExpect(model().attribute("error", messageService.getMessage("study.error.studyNotExist")));
	}

	@Test
	@WithUserDetails(value = ACTIVE_USER_NAME,
	                 userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
	public void listInvalidStudyIdForbidden() throws Exception {
		getActiveUser();
		mockMvc.perform(get("/studies/0/subject-lists"))
		       .andExpect(status().isForbidden());
	}

	@Test
	public void viewGet() throws Exception {
		Study activeStudy = getActiveStudy();
		SubjectList subjectList = activeStudy.getSubjectLists().get(0);

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/" + subjectList.getId()))
		       .andExpect(status().isOk())
		       .andExpect(view().name("/subjects/view"))
		       .andExpect(model().attributeExists(SubjectListController.DELETE_SUBJECT_DTO_KEY))
		       .andExpect(model().attributeExists("reasonTypes"))
		       .andExpect(model().attributeExists("study"))
		       .andExpect(result -> {
			       assertEquals(activeStudy.getId(),
			                    ((StudyDTO) result.getModelAndView().getModel().get("study")).getId(),
			                    "Wrong study!");

		       })
		       .andExpect(model().attributeExists("subjectList"))
		       .andExpect(result -> {
			       assertEquals(subjectList.getId(),
			                    ((SubjectListDTO) result.getModelAndView().getModel().get("subjectList")).getId(),
			                    "Wrong subject list!");

		       });

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ_SUBJECTS, null);
	}

	@Test
	public void viewGetPregenerated() throws Exception {
		Study pregeneratedStudy = getPregeneratedStudy();
		SubjectList subjectList = pregeneratedStudy.getSubjectLists().get(0);

		mockMvc.perform(get("/studies/" + pregeneratedStudy.getApiId() + "/subject-lists/" + subjectList.getId()))
		       .andExpect(status().isOk())
		       .andExpect(view().name("/subjects/view"))
		       .andExpect(model().attributeDoesNotExist("info"));
	}

	@Test
	public void viewInvalidStudyId() throws Exception {
		mockMvc.perform(get("/studies/0/subject-lists/0"))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/studies"))
		       .andExpect(model().attributeExists("error"))
		       .andExpect(model().attribute("error", messageService.getMessage("study.error.studyNotExist")));
	}

	@Test
	@WithUserDetails(value = ACTIVE_USER_NAME,
	                 userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
	public void viewInvalidStudyIdForbidden() throws Exception {
		mockMvc.perform(get("/studies/0/subject-lists/0"))
		       .andExpect(status().isForbidden());
	}

	@Test
	public void viewInvalidSubjectListId() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/0"))
		       .andExpect(status().is3xxRedirection())
		       .andExpect(view().name("redirect:/studies/" + activeStudy.getApiId() + "/subject-lists"))
		       .andExpect(model().attributeExists("error"))
		       .andExpect(model().attribute("error", messageService.getMessage("subjectLists.view.error.notExists")));
	}

	@Test
	public void downloadEmptyCsv() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/download")
				                .param("format", "CSV")
				                .param("delimiter", "COMMA"))
		       .andExpect(status().isOk())
		       .andExpect(header().exists("Content-Disposition"))
		       .andExpect(header().string("Content-Disposition",
		                                  "attachment;filename=\"" + activeStudy.getGuiName() + ".csv\""))
		       .andExpect(content().contentType("text/csv"))
		       .andExpect(content().string(
				       "orderNumber,pseudonym,studyArmName,status,randomizationTimestamp,deletionTimestamp,releaseTimestamp,location,gender,\"age group\"\r\n"));

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);
	}

	@Test
	public void downloadEmptyJson() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/download")
				                .param("format", "JSON")
				                .param("delimiter", "COMMA"))
		       .andExpect(status().isOk())
		       .andExpect(header().exists("Content-Disposition"))
		       .andExpect(header().string("Content-Disposition",
		                                  "attachment;filename=\"" + activeStudy.getGuiName() + ".json\""))
		       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().string("[ ]"));

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);
	}

	@Test
	public void downloadCsv() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/download")
				                .param("format", "CSV")
				                .param("delimiter", "COMMA")
				                .param("status", "ACTIVE")
				                .param("sites", "First Site of active study - API ID")
				                .param("gender - API ID", "m - API ID")
				                .param("age group - API ID", "0 - 17 - API ID"))
				.andDo(print())
		       .andExpect(status().isOk())
		       .andExpect(header().exists("Content-Disposition"))
		       .andExpect(header().string("Content-Disposition",
		                                  "attachment;filename=\"" + activeStudy.getGuiName() + ".csv\""))
		       .andExpect(content().contentType("text/csv"))
		       .andExpect(content().string(
				       "orderNumber,pseudonym,studyArmName,status,randomizationTimestamp,deletionTimestamp,releaseTimestamp,location,gender,\"age group\"\r\n" +
				       "1,pseudonym1,\"First arm of active study\",ACTIVE,\"2000-12-24 18:00:00\",,,\"First Site of active study\",m,\"0 - 17\"\r\n"));

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);
	}

	@Test
	public void downloadCsvSemicolon() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/download")
				                .param("format", "CSV")
				                .param("delimiter", Delimiter.SEMICOLON.name())
				                .param("status", "ACTIVE")
				                .param("sites", "First Site of active study - API ID")
				                .param("gender - API ID", "m - API ID")
				                .param("age group - API ID", "0 - 17 - API ID"))
		       .andDo(print())
		       .andExpect(status().isOk())
		       .andExpect(header().exists("Content-Disposition"))
		       .andExpect(header().string("Content-Disposition",
		                                  "attachment;filename=\"" + activeStudy.getGuiName() + ".csv\""))
		       .andExpect(content().contentType("text/csv"))
		       .andExpect(content().string(
				       "orderNumber;pseudonym;studyArmName;status;randomizationTimestamp;deletionTimestamp;releaseTimestamp;location;gender;\"age group\"\r\n" +
				       "1;\"pseudonym1\";\"First arm of active study\";ACTIVE;\"2000-12-24 18:00:00\";;;\"First Site of active study\";m;\"0 - 17\"\r\n"));

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);
	}

	@Test
	public void downloadJson() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/download")
				                .param("format", "JSON")
				                .param("delimiter", "COMMA")
				                .param("status", "ACTIVE")
				                .param("sites", "First Site of active study - API ID")
				                .param("gender - API ID", "m - API ID")
				                .param("age group - API ID", "0 - 17 - API ID"))
		       .andExpect(status().isOk())
		       .andExpect(header().exists("Content-Disposition"))
		       .andExpect(header().string("Content-Disposition",
		                                  "attachment;filename=\"" + activeStudy.getGuiName() + ".json\""))
		       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json(
				       "[{'orderNumber':1,'pseudonym':'pseudonym1','studyArmName':'First arm of active study','status':'ACTIVE','randomizationTimestamp':'2000-12-24 18:00:00','location':'First Site of active study','gender':'m','age group':'0 - 17'}]"));

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);
	}

	@Test
	public void downloadZip() throws Exception {
		Study activeStudy = getActiveStudy();

		MvcResult result = mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/download")
				                                   .param("splitFiles", "true")
				                                   .param("format", "CSV")
				                                   .param("delimiter", "COMMA")
				                                   .param("status", "ACTIVE")
				                                   .param("sites", "First Site of active study - API ID")
				                                   .param("gender - API ID", "m - API ID")
				                                   .param("age group - API ID", "0 - 17 - API ID"))
		                          .andExpect(status().isOk())
		                          .andExpect(header().exists("Content-Disposition"))
		                          .andExpect(header().string("Content-Disposition",
		                                                     "attachment;filename=\"" + activeStudy.getGuiName() +
		                                                     ".zip\""))
		                          .andExpect(content().contentType("application/zip")).andReturn();

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);

		try (final var zipIn = new ZipInputStream(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
			final StringBuilder sb = new StringBuilder();
			final byte[] buffer = new byte[1024];
			int read;

			final ZipEntry first =  zipIn.getNextEntry();
			assertNotNull(first, "There should be one file in the zip");
			while((read = zipIn.read(buffer, 0, 1024)) >= 0) {
				sb.append(new String(buffer, 0, read));
			}

			final String content = sb.toString();
			assertEquals("orderNumber,pseudonym,studyArmName,status,randomizationTimestamp,deletionTimestamp,releaseTimestamp,location,gender,\"age group\"\r\n" +
			             "1,pseudonym1,\"First arm of active study\",ACTIVE,\"2000-12-24 18:00:00\",,,\"First Site of active study\",m,\"0 - 17\"\r\n",
			             content, "Unexpected content of first file!");

			final ZipEntry second =  zipIn.getNextEntry();
			assertNull(second, "There should be only on file in the zip");
		}
	}

	@Test
	public void downloadWithAsCsvApiIds() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/download")
				                .param("includeApiIds", "true")
				                .param("format", "CSV")
				                .param("delimiter", "COMMA")
				                .param("status", "ACTIVE")
				                .param("sites", "First Site of active study - API ID")
				                .param("gender - API ID", "m - API ID")
				                .param("age group - API ID", "0 - 17 - API ID"))
		       .andDo(print())
		       .andExpect(status().isOk())
		       .andExpect(header().exists("Content-Disposition"))
		       .andExpect(header().string("Content-Disposition",
		                                  "attachment;filename=\"" + activeStudy.getGuiName() + ".csv\""))
		       .andExpect(content().contentType("text/csv"))
		       .andExpect(content().string(
				       "orderNumber,pseudonym,studyArmName,studyArmApiId,status,randomizationTimestamp,deletionTimestamp,releaseTimestamp,location,locationApiId,gender,\"age group\"\r\n" +
				       "1,pseudonym1,\"First arm of active study\",\"First arm of active study - API ID\",ACTIVE,\"2000-12-24 18:00:00\",,,\"First Site of active study\",\"First Site of active study - API ID\",m,\"0 - 17\"\r\n"));

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);
	}

	@Test
	public void downloadSingle() throws Exception {
		Study activeStudy = getActiveStudy();

		mockMvc.perform(get("/studies/" + activeStudy.getApiId() + "/subject-lists/ " +
		                    activeStudy.getSubjectLists().get(0).getId() + "/download")
				                .param("format", "CSV")
				                .param("delimiter", "COMMA")
				                .param("status", "ACTIVE")
				                .param("sites", "First Site of active study - API ID")
				                .param("gender - API ID", "m - API ID")
				                .param("age group - API ID", "0 - 17 - API ID"))
		       .andExpect(status().isOk())
		       .andExpect(header().exists("Content-Disposition"))
		       .andExpect(header().string("Content-Disposition",
		                                  "attachment;filename=\"" + activeStudy.getGuiName() + ".csv\""))
		       .andExpect(content().contentType("text/csv"))
		       .andExpect(content().string(
				       "orderNumber,pseudonym,studyArmName,status,randomizationTimestamp,deletionTimestamp,releaseTimestamp,location,gender,\"age group\"\r\n" +
				       "1,pseudonym1,\"First arm of active study\",ACTIVE,\"2000-12-24 18:00:00\",,,\"First Site of active study\",m,\"0 - 17\"\r\n"));

		testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);
	}

}
