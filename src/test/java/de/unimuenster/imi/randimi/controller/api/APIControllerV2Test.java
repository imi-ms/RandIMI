package de.unimuenster.imi.randimi.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import de.unimuenster.imi.randimi.model.api.RandomizePatientRequestBodyV2;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.enumeration.ImbalanceFunction;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class APIControllerV2Test extends APIControllerTestBase {

    @Test
    @WithUserDetails(value = "active_test_user",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void noAuthorizationTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/version")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("detail").value("Access Denied"));
    }

    @Test
    public void getAPIVersionTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v2/version")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string("2.0"));
    }

    @Test
    public void getStudyForbidden() throws Exception {
        final Study inactiveStudy = getInactiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + inactiveStudy.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isForbidden());
    }

    @Test
    public void getStudyInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/invalid")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isForbidden());
    }

    @Test
    public void getStudyTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(content().json(
                       "{'apiId': '" + activeStudy.getApiId() + "', 'name': '" + activeStudy.getGuiName() +
                       "', 'pseudonymHandling': '" + activeStudy.getPseudonymHandling() +
                       "', 'sites': [{'name': 'First Site of active study', 'apiId': 'First Site of active study - API ID'}, {'name': 'Second Site of active study', 'apiId': 'Second Site of active study - API ID'}], 'arms': [{'name': 'First arm of active study', 'apiId': 'First arm of active study - API ID'}, {'name': 'Second arm of active study', 'apiId': 'Second arm of active study - API ID'}], 'strata': [{'name':'gender', 'apiId': 'gender - API ID', 'values': [{'name': 'm', 'apiId': 'm - API ID'}, {'name': 'w', 'apiId': 'w - API ID'}, {'name': 'd', 'apiId': 'd - API ID'}], type: 'ENUM'},{'name':'age group', 'apiId': 'age group - API ID', 'values': [{'name': '0 - 17', 'apiId': '0 - 17 - API ID'}, {'name': '18 - 100', 'apiId': '18 - 100 - API ID'}], type: 'ENUM'}]}"));

        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ, null);
    }

    @Test
    public void getStudyStrataNamesForbiddenTest() throws Exception {
        final Study inactiveStudy = getInactiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v2/study/" + inactiveStudy.getId() + "/stratum")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getStudyStrataNamesInvalidTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/0/stratum")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("status").value(403))
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("detail").value("Access Denied"));
    }

    @Test
    public void getStudyStrataNamesTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v2/study/" + activeStudy.getApiId() + "/stratum")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string("[\"" + activeStudy.getStratums().get(0).getApiId() + "\",\""
                        + activeStudy.getStratums().get(1).getApiId() + "\"]"));

        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ, null);
    }

    @Test
    public void getStudyStrataInfoTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/stratum/definition")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(content().json(
                       "{'strata': [{'name':'gender', 'apiId': 'gender - API ID', 'values': [{'name': 'm', 'apiId': 'm - API ID'}, {'name': 'w', 'apiId': 'w - API ID'}, {'name': 'd', 'apiId': 'd - API ID'}], type: 'ENUM'},{'name':'age group', 'apiId': 'age group - API ID', 'values': [{'name': '0 - 17', 'apiId': '0 - 17 - API ID'}, {'name': '18 - 100', 'apiId': '18 - 100 - API ID'}], type: 'ENUM'}]}"));

        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ, null);
    }

    @Test
    public void getSubjectListsAsJson() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "JSON")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17 - API ID\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
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
    public void getSubjectListsAsCsv() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17 - API ID\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
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
    public void getSubjectListsAsCsvSemicolon() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "CSV")
                                .param("delimiter", ";")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17 - API ID\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
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
    public void getSubjectListsAsZip() throws Exception {
        final Study activeStudy = getActiveStudy();

        final var result = mockMvc.perform(MockMvcRequestBuilders
                                                   .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                                   .param("splitFiles", "true")
                                                   .param("format", "CSV")
                                                   .param("status", "ACTIVE")
                                                   .param("sites", "First Site of active study - API ID")
                                                   .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17 - API ID\"]}")
                                                   .contentType(MediaType.APPLICATION_JSON_VALUE))
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
    public void getSubjectListAsJson() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists/" + activeStudy.getSubjectLists().get(0).getId())
                                .param("includeApiIds", "true")
                                .param("format", "JSON")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17 - API ID\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
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
    public void getSubjectListAsJsonWithApiIds() throws Exception {
        Study activeStudy = getActiveStudy();

        mockMvc.perform(get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("includeApiIds", "true")
                                .param("format", "JSON")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17 - API ID\"]}"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(header().exists("Content-Disposition"))
               .andExpect(header().string("Content-Disposition",
                                          "attachment;filename=\"" + activeStudy.getGuiName() + ".json\""))
               .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(content().json(
                       "[{'orderNumber':1,'pseudonym':'pseudonym1','studyArmName':'First arm of active study',studyArmApiId:'First arm of active study - API ID','status':'ACTIVE','randomizationTimestamp':'2000-12-24 18:00:00','location':'First Site of active study','locationApiId':'First Site of active study - API ID','gender':'m','age group':'0 - 17'}]"));

        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.EXPORT_SUBJECTS, null);
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void getSubjectListsInvalidStudyIdAsAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/0/subject-lists")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isNotAcceptable())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("errorCode").value(5010));
    }

    @Test
    public void getSubjectListsInvalidStudyId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/0/subject-lists")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isForbidden())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("detail").value("Access Denied"));
    }

    @Test
    public void getSubjectListsInvalidSubjectListId() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists/0")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0 - 17\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isForbidden())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("detail").value("Access Denied"));
    }

    @Test
    public void getSubjectListsInvalidSplitFiles() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("splitFiles", "invalid")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0-17.0\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("details.validationErrors.splitFiles[0]").value("Failed to convert value"));
    }

    @Test
    public void getSubjectListsInvalidFormat() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "txt")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0.0-17.0\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("details.validationErrors.format[0]").value("Failed to convert value"));
    }

    @Test
    public void getSubjectListsInvalidStatus() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "CSV")
                                .param("status", "INVALID")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0.0-17.0\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andDo(print())
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("details.validationErrors.status[0]").value("Failed to convert value"));
    }

    @Test
    public void getSubjectListsInvalidSite() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "INVALID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0.0-17.0\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andDo(print())
               .andExpect(status().isForbidden())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void getSubjectListsInvalidSiteAsAdmin() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "INVALID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"], \"age group - API ID\": [\"0.0-17.0\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andDo(print())
               .andExpect(status().isNotAcceptable())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void getSubjectListsAdditionalStratum() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"m - API ID\"],\"invalid\": [\"m\"],\"age group - API ID\": [\"0.0-17.0\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andDo(print())
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void getSubjectListsMissingStratum() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"age group - API ID\": [\"0.0-17.0\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andDo(print())
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void getSubjectListsInvalidStratumPart() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject-lists")
                                .param("format", "CSV")
                                .param("status", "ACTIVE")
                                .param("sites", "First Site of active study - API ID")
                                .param("strata", "{\"gender - API ID\": [\"invalid\"],\"age group - API ID\": [\"0.0-17.0\"]}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andDo(print())
               .andExpect(status().isBadRequest())
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void randomizePatientNoAuthorizationTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        final RandomizePatientRequestBodyV2 randomizePatientRequestBodyV2 = new RandomizePatientRequestBodyV2();
        randomizePatientRequestBodyV2.setPseudonym("subject1");
        randomizePatientRequestBodyV2.setSiteApiId(activeStudy.getSites().get(1).getApiId());
        randomizePatientRequestBodyV2.setStudyStrataParams(Map.of("age group", "18", "gender", "m"));

        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode root = objectMapper.createObjectNode();
        root.put("randomizationRequest", objectMapper.writeValueAsString(randomizePatientRequestBodyV2));

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(randomizePatientRequestBodyV2)))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("status").value(403))
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("detail").value("Access Denied"));
    }

    @Test
    public void randomizePatientTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        final String pseudonym = "subject1";

        final RandomizePatientRequestBodyV2 randomizePatientRequestBodyV2 = new RandomizePatientRequestBodyV2();
        randomizePatientRequestBodyV2.setPseudonym(pseudonym);
        randomizePatientRequestBodyV2.setSiteApiId(activeStudy.getSites().get(0).getApiId());
        randomizePatientRequestBodyV2.setStudyStrataParams(Map.of("age group - API ID", "18 - 100 - API ID", "gender - API ID", "m - API ID"));

        final ObjectMapper objectMapper = new ObjectMapper();

        final String response = mockMvc.perform(MockMvcRequestBuilders
                                                        .post("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(objectMapper.writeValueAsString(
                                                                randomizePatientRequestBodyV2)))
                                       .andExpect(status().isOk())
                                       .andExpect(jsonPath("subject.studyArm.name").value("First arm of active study"))
                                       .andExpect(jsonPath("subject.studyArm.apiId").value("First arm of active study - API ID"))
                                       .andReturn().getResponse().getContentAsString();
        final String studyArm = JsonPath.read(response, "$.subject.studyArm.name");

        final List<Subject> subjectList = activeStudy.getSubjectLists().get(3).getSubjects();
        final Subject subject = subjectList.get(subjectList.size() - 1);
        assertEquals(pseudonym, subject.getPseudonym(), "Unexpected pseudonym of the subject!");
        assertEquals(studyArm, subject.getStudyArm().getGuiName(), "Unexpected study arm!");

        testLastAuditEntryForSubject(activeStudy.getId(), AuditType.CREATE, null, subject.getId());
    }

    @Test
    public void randomizePatientMissingStratumTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        final String pseudonym = "subject1";

        final RandomizePatientRequestBodyV2 randomizePatientRequestBodyV2 = new RandomizePatientRequestBodyV2();
        randomizePatientRequestBodyV2.setPseudonym(pseudonym);
        randomizePatientRequestBodyV2.setSiteApiId(activeStudy.getSites().get(0).getApiId());
        randomizePatientRequestBodyV2.setStudyStrataParams(Map.of("age group - API ID", "18"));

        final ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .header("accept-language", "en-US")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(randomizePatientRequestBodyV2)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("detail").value("[R1040] No matching stratum with API ID 'gender - API ID' has been found."));
    }

    @Test
    public void randomizePatientDuplicatePseudonymTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        final String pseudonym = "subject1";
        final RandomizePatientRequestBodyV2 randomizePatientRequestBodyV2 = new RandomizePatientRequestBodyV2();
        randomizePatientRequestBodyV2.setPseudonym(pseudonym);
        randomizePatientRequestBodyV2.setSiteApiId(activeStudy.getSites().get(0).getApiId());
        randomizePatientRequestBodyV2.setStudyStrataParams(Map.of("age group - API ID", "18 - 100 - API ID", "gender - API ID", "m - API ID"));

        final ObjectMapper objectMapper = new ObjectMapper();

        // Randomize the subject the first time
        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(
                                        randomizePatientRequestBodyV2)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("details").doesNotExist())
               .andExpect(jsonPath("details.existingSubject").doesNotExist());

        // Randomize the subject the second time to check for conflict
        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(
                                        randomizePatientRequestBodyV2)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("errorCode").value("4010"))
               .andExpect(jsonPath("details.existingSubject.studyArm.name").value("First arm of active study"));
    }

    @Test
    public void randomizePatientMissingStrataTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        final String pseudonym = "subject1";

        final RandomizePatientRequestBodyV2 randomizePatientRequestBodyV2 = new RandomizePatientRequestBodyV2();
        randomizePatientRequestBodyV2.setPseudonym(pseudonym);
        randomizePatientRequestBodyV2.setSiteApiId(activeStudy.getSites().get(0).getApiId());

        final ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .header("accept-language", "en-US")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(randomizePatientRequestBodyV2)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("detail").value("[R1045] The request did not contain strata parameter 'studyStrataParams', although the study is stratified."));
    }

    @Test
    public void fetchParticipantAssignmentNoAuthorizationTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .param("siteApiId", activeStudy.getSites().get(1).getApiId())
                                .param("pseudonym", "pseudonym2"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("status").value(403))
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("detail").value("Access Denied"));
    }

    @Test
    public void fetchParticipantAssignmentDeletedStudyTest() throws Exception {
        final Study deletedStudy = getDeletedStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + deletedStudy.getApiId() + "/subject")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .param("siteApiId", deletedStudy.getSites().get(0).getApiId())
                                .param("pseudonym", "pseudonym1"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("status").value(HttpStatus.FORBIDDEN.value()))
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("detail").value("Access Denied"));
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void fetchParticipantAssignmentDeletedStudyAdminTest() throws Exception {
        final Study deletedStudy = getDeletedStudy();
        long studyId = deletedStudy.getId();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + deletedStudy.getApiId() + "/subject")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .param("siteApiId", deletedStudy.getSites().get(0).getApiId())
                                .param("pseudonym", "pseudonym1"))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("status").value(HttpStatus.NOT_ACCEPTABLE.value()))
               .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("detail").value("[R5010] Die angeforderte Studie mit der API ID '" + studyId + " - API ID' existiert nicht."));
    }

    @Test
    public void fetchParticipantAssignmentTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final StudyArm assignedStudyArm = activeStudy.getStudyArms().get(0);

        mockMvc.perform(get("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .param("siteApiId", activeStudy.getSites().get(0).getApiId())
                                .param("pseudonym", "pseudonym1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("subject.pseudonym").value("pseudonym1"))
               .andExpect(jsonPath("subject.studyArm.name").value(assignedStudyArm.getGuiName()))
               .andExpect(jsonPath("subject.studyArm.apiId").value(assignedStudyArm.getApiId()))
               .andExpect(jsonPath("subject.stratificationParameters", Matchers.aMapWithSize(2)))
               .andExpect(jsonPath("subject.stratificationParameters",
                                   Matchers.hasEntry("age group - API ID", "0 - 17 - API ID")))
               .andExpect(jsonPath("subject.stratificationParameters",
                                   Matchers.hasEntry("gender - API ID", "m - API ID")));

        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ_SUBJECTS, null);
    }

    @Test
    public void fetchParticipantAssignmentNoEntryTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final String pseudonym = "wrongPseudonym";

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .header("accept-language", "en-US")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .param("studyId", String.valueOf(activeStudy.getId()))
                                .param("siteApiId", activeStudy.getSites().get(0).getApiId())
                                .param("pseudonym", pseudonym))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("detail").value("[R5025] Study '" + activeStudy.getApiId() +
                                                   "' does not contain an entry with pseudonym '" + pseudonym +
                                                   "' in location '" + activeStudy.getSites().get(0).getApiId() + "'"));
    }

    @Test
    public void fetchParticipantAssignmentNoEntryEnUsTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final String pseudonym = "wrongPseudonym";

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .header("accept-language", "en-US")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .param("siteApiId", activeStudy.getSites().get(0).getApiId())
                                .param("pseudonym", pseudonym))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("detail").value("[R5025] Study '" + activeStudy.getApiId() +
                                                   "' does not contain an entry with pseudonym '" + pseudonym +
                                                   "' in location '" + activeStudy.getSites().get(0).getApiId() + "'"));
    }

    @Test
    public void fetchParticipantAssignmentNoEntryDeDeTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        final String pseudonym = "wrongPseudonym";
        final String expectedContent = "[R5025] Studie '" + activeStudy.getApiId() +
                                       "' enthält keinen Eintrag mit dem Pseudonym '" + pseudonym +
                                       "' im Standort '" + activeStudy.getSites().get(0).getApiId() + "'";

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v2/study/" + activeStudy.getApiId() + "/subject")
                                .header("accept-language", "de-DE")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .param("siteApiId", activeStudy.getSites().get(0).getApiId())
                                .param("pseudonym", pseudonym))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("detail").value(expectedContent));
    }

    private static final String CREATE_STUDY_CONFIGURATION = """
            {
              "name": "API Study",
              "apiId": "api-study",
              "capacity": 16,
              "stratifyBySite": true,
              "sites": [
                {"name": "API Site 1", "capacity": 8, "pseudonymRegex": ".*", "seed": 42},
                {"name": "API Site 2", "apiId": "site-2", "capacity": 8, "pseudonymRegex": ".*"}
              ],
              "arms": [
                {"name": "Control"},
                {"name": "Intervention", "apiId": "int"}
              ],
              "strata": [
                {"name": "Sex", "values": [{"name": "F"}, {"name": "M", "apiId": "male"}]}
              ],
              "randomization": {"algorithm": "BLOCKED", "minBlocksize": 2, "maxBlocksize": 4}
            }""";

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void createStudyTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(CREATE_STUDY_CONFIGURATION))
               .andExpect(status().isCreated())
               .andExpect(content().json(
                       "{'name': 'API Study', 'apiId': 'api-study', 'pseudonymHandling': 'UNIQUE_IN_STUDY', 'sites': [{'name': 'API Site 1', 'apiId': 'API Site 1'}, {'name': 'API Site 2', 'apiId': 'site-2'}], 'arms': [{'name': 'Control', 'apiId': 'Control'}, {'name': 'Intervention', 'apiId': 'int'}], 'strata': [{'name': 'Sex', 'apiId': 'Sex', 'type': 'ENUM', 'values': [{'name': 'F', 'apiId': 'F'}, {'name': 'M', 'apiId': 'male'}]}]}"));

        final List<Study> studies = studyRepository.findByGuiName("API Study");
        assertEquals(1, studies.size(), "Study was not persisted!");

        final Study study = studies.get(0);
        assertEquals(RandomizationAlgorithm.BLOCKED, study.getRandomizationAlgorithm());
        assertEquals(2, study.getRandomizationAlgorithmVersion(), "New studies should use the latest version!");
        assertEquals(2, study.getMinBlocksize());
        assertEquals(4, study.getMaxBlocksize());
        assertTrue(study.isStratifiedBySite());
        assertEquals(42L, study.getSites().get(0).getSeed());
        assertFalse(study.isActive(), "Study should not be activated!");

        testLastAuditEntryForStudy(study.getId(), AuditType.CREATE, null);
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void createStudyMinimizationTest() throws Exception {
        final String configuration = """
                {
                  "name": "API Minimization Study",
                  "capacity": 10,
                  "sites": [{"name": "Site", "capacity": 10, "pseudonymRegex": ".*"}],
                  "arms": [{"name": "A"}, {"name": "B"}],
                  "randomization": {"algorithm": "MINIMIZATION", "imbalanceBias": 0.8, "imbalanceFunction": "RANGE"}
                }""";

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(configuration))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("apiId").value("API Minimization Study"));

        final Study study = studyRepository.findByGuiName("API Minimization Study").get(0);
        assertEquals(RandomizationAlgorithm.MINIMIZATION, study.getRandomizationAlgorithm());
        assertEquals(0.8f, study.getMinimizationParameter().getImbalanceBias());
        assertEquals(ImbalanceFunction.RANGE, study.getMinimizationParameter().getImbalanceFunction());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void createStudyInvalidConfigurationTest() throws Exception {
        final String configuration = """
                {
                  "capacity": 16,
                  "sites": [],
                  "arms": [{"name": "Control"}],
                  "randomization": {"algorithm": "BLOCKED", "minBlocksize": 1}
                }""";

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(configuration))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("details.validationErrors.name").exists())
               .andExpect(jsonPath("details.validationErrors.sites").exists())
               .andExpect(jsonPath("details.validationErrors.arms").exists())
               .andExpect(jsonPath("details.validationErrors['randomization.minBlocksize']").exists())
               .andExpect(jsonPath("details.validationErrors['randomization.maxBlocksize']").exists());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void createStudyInvalidStudyTest() throws Exception {
        final String configuration = CREATE_STUDY_CONFIGURATION
                .replace("\"name\": \"API Study\"", "\"name\": \"Active Study\"")
                .replace("\"name\": \"Intervention\"", "\"name\": \"Control\"")
                .replace("\"name\": \"M\"", "\"name\": \"F\"");

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(configuration))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("errorCode").value(3020))
               .andExpect(jsonPath("details.validationErrors.name").exists())
               .andExpect(jsonPath("details.validationErrors['arms[1].name']").exists())
               .andExpect(jsonPath("details.validationErrors['strata[0].values[1].name']").exists());

        assertTrue(studyRepository.findByGuiName("API Study").isEmpty());
    }

    @Test
    public void createStudyForbiddenTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(CREATE_STUDY_CONFIGURATION))
               .andExpect(status().isForbidden());

        assertTrue(studyRepository.findByGuiName("API Study").isEmpty());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void activateStudyTest() throws Exception {
        final Study inactiveStudy = getInactiveStudy();
        assertFalse(inactiveStudy.isActive());

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + inactiveStudy.getApiId() + "/activate"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("apiId").value(inactiveStudy.getApiId()));

        final Study study = studyRepository.findById(inactiveStudy.getId()).get();
        assertTrue(study.isActive(), "Study should be activated!");
        assertNotNull(study.getActivationDate());
        assertFalse(study.getSubjectLists().isEmpty(), "Subject lists should be generated!");

        testLastAuditEntryForStudy(study.getId(), AuditType.ACTIVATE, null);
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void activateStudyTestModeTest() throws Exception {
        final Study testModeStudy = getTestModeStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + testModeStudy.getApiId() + "/activate"))
               .andExpect(status().isOk());

        assertTrue(studyRepository.findById(testModeStudy.getId()).get().isActive(), "Study should be activated!");
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void activateStudyAlreadyActiveTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/activate"))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("errorCode").value(5055));
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void activateStudyInvalidStudyTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/invalid/activate"))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("errorCode").value(5010));
    }

    @Test
    public void activateStudyForbiddenTest() throws Exception {
        final Study inactiveStudy = getInactiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + inactiveStudy.getApiId() + "/activate"))
               .andExpect(status().isForbidden());

        assertFalse(studyRepository.findById(inactiveStudy.getId()).get().isActive());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void testStudyTest() throws Exception {
        final Study inactiveStudy = getInactiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + inactiveStudy.getApiId() + "/test"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("apiId").value(inactiveStudy.getApiId()));

        final Study study = studyRepository.findById(inactiveStudy.getId()).get();
        assertTrue(study.isInTestMode(), "Study should be in test mode!");
        assertFalse(study.getSubjectLists().isEmpty(), "Subject lists should be generated!");

        testLastAuditEntryForStudy(study.getId(), AuditType.TEST, null);
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void testStudyAlreadyActiveTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/test"))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("errorCode").value(5055));

        assertTrue(studyRepository.findById(activeStudy.getId()).get().isActive());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void testStudyInvalidStudyTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/invalid/test"))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("errorCode").value(5010));
    }

    @Test
    public void testStudyForbiddenTest() throws Exception {
        final Study inactiveStudy = getInactiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + inactiveStudy.getApiId() + "/test"))
               .andExpect(status().isForbidden());

        assertFalse(studyRepository.findById(inactiveStudy.getId()).get().isInTestMode());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void archiveStudyTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/archive"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("apiId").value(activeStudy.getApiId()));

        assertTrue(studyRepository.findById(activeStudy.getId()).get().isArchived(), "Study should be archived!");
        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.ARCHIVE, null);
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void archiveStudyLockedTest() throws Exception {
        final Study lockedStudy = getLockedActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + lockedStudy.getApiId() + "/archive"))
               .andExpect(status().isOk());

        assertTrue(studyRepository.findById(lockedStudy.getId()).get().isArchived(), "Study should be archived!");
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void archiveStudyInactiveTest() throws Exception {
        final Study inactiveStudy = getInactiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + inactiveStudy.getApiId() + "/archive"))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("errorCode").value(5051));

        assertFalse(studyRepository.findById(inactiveStudy.getId()).get().isArchived());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void archiveStudyInvalidStudyTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/invalid/archive"))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("errorCode").value(5010));
    }

    @Test
    public void archiveStudyForbiddenTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v2/study/" + activeStudy.getApiId() + "/archive"))
               .andExpect(status().isForbidden());

        assertFalse(studyRepository.findById(activeStudy.getId()).get().isArchived());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void deleteStudyTestModeTest() throws Exception {
        final Study testModeStudy = getTestModeStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .delete("/api/v2/study/" + testModeStudy.getApiId()))
               .andExpect(status().isNoContent());

        assertTrue(studyRepository.findById(testModeStudy.getId()).isEmpty(), "Study should be removed!");
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void deleteStudyArchivedTest() throws Exception {
        final Study archivedStudy = getArchivedStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .delete("/api/v2/study/" + archivedStudy.getApiId())
                                .param("changeReason", "Test"))
               .andExpect(status().isNoContent());

        assertTrue(studyRepository.findById(archivedStudy.getId()).get().isDeleted(),
                   "Study should be marked as deleted!");
        testLastAuditEntryForStudy(archivedStudy.getId(), AuditType.DELETE, "Test");
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void deleteStudyArchivedMissingChangeReasonTest() throws Exception {
        final Study archivedStudy = getArchivedStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .delete("/api/v2/study/" + archivedStudy.getApiId()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("errorCode").value(1060));

        assertTrue(studyRepository.findById(archivedStudy.getId()).get().isArchived());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void deleteStudyActiveTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .delete("/api/v2/study/" + activeStudy.getApiId()))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("errorCode").value(5055));

        assertTrue(studyRepository.findById(activeStudy.getId()).isPresent());
    }

    @Test
    @WithUserDetails(value = "admin",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void deleteStudyInvalidStudyTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .delete("/api/v2/study/invalid"))
               .andExpect(status().isNotAcceptable())
               .andExpect(jsonPath("errorCode").value(5010));
    }

    @Test
    public void deleteStudyForbiddenTest() throws Exception {
        final Study inactiveStudy = getInactiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .delete("/api/v2/study/" + inactiveStudy.getApiId()))
               .andExpect(status().isForbidden());

        assertTrue(studyRepository.findById(inactiveStudy.getId()).isPresent());
    }
}
