package de.unimuenster.imi.randimi.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unimuenster.imi.randimi.model.api.RandomizePatientRequestBodyV1;
import de.unimuenster.imi.randimi.model.enumeration.AuditType;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Deprecated
public class APIControllerV1Test extends APIControllerTestBase {

    @Test
    @WithUserDetails(value = "active_test_user",
                     userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
    public void noAuthorizationTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v1/getApiVersion")
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("detail").value("Access Denied"));
    }

    @Test
    public void getAPIVersionTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/getApiVersion")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string("1.0"));
    }

    @Test
    public void getAPIVersionAlternatePathTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/getApiVersion")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string("1.0"));
    }

    @Test
    public void getStudyStrataNamesForbiddenTest() throws Exception {
        final Study inactiveStudy = getInactiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v1/getStudyStrataNames")
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                .param("studyId", inactiveStudy.getApiId()))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("status").value(403))
               .andExpect(jsonPath("errors").value("Access Denied"));
    }

    @Test
    public void getStudyStrataNamesInvalidTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v1/getStudyStrataNames")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .param("studyId", "0"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("status").value(403))
               .andExpect(jsonPath("errors").value("Access Denied"));
    }

    @Test
    public void getStudyStrataNamesTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/getStudyStrataNames")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .param("studyId", activeStudy.getApiId()))
                .andExpect(status().isOk())
                .andExpect(content().string("[\"" + activeStudy.getStratums().get(0).getApiId() + "\",\""
                        + activeStudy.getStratums().get(1).getApiId() + "\"]"));

        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ, null);
    }

    @Test
    public void getStudyStrataInfoTest() throws Exception {
        final Study activeStudy = getActiveStudy();

        mockMvc.perform(MockMvcRequestBuilders
                                .get("/api/v1/getStudyStrataInfo")
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                .param("studyId", activeStudy.getApiId()))
               .andExpect(status().isOk())
               .andExpect(content().json(
                       "{'strata': [{'name':'gender', 'apiId': 'gender - API ID', 'values': ['m - API ID', 'w - API ID', 'd - API ID'], type: 'ENUM'},{'name':'age group', 'apiId': 'age group - API ID', 'values': ['0 - 17 - API ID', '18 - 100 - API ID'], type: 'ENUM'}]}"));

        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ, null);
    }

    @Test
    public void randomizePatientNoAuthorizationTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final Site secondtSite = activeStudy.getSites().get(1);

        final RandomizePatientRequestBodyV1 randomizePatientRequestBodyV1 = new RandomizePatientRequestBodyV1();
        randomizePatientRequestBodyV1.setLocationApiId(secondtSite.getApiId());
        randomizePatientRequestBodyV1.setPseudonym("subject1");
        randomizePatientRequestBodyV1.setStudyApiId(activeStudy.getApiId());
        randomizePatientRequestBodyV1.setStudyStrataParams("{\"age group - API ID\":18,\"gender - API ID\":\"m\"}");

        final ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v1/randomizePatient")
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                .content(objectMapper.writeValueAsString(randomizePatientRequestBodyV1)))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("status").value(403))
               .andExpect(jsonPath("errors").value("Access Denied"))
               .andDo(print());
    }

    @Test
    public void randomizePatientTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final Site firstSite = activeStudy.getSites().get(0);

        final String pseudonym = "subject1";

        final RandomizePatientRequestBodyV1 randomizePatientRequestBodyV1 = new RandomizePatientRequestBodyV1();
        randomizePatientRequestBodyV1.setLocationApiId(firstSite.getApiId());
        randomizePatientRequestBodyV1.setPseudonym(pseudonym);
        randomizePatientRequestBodyV1.setStudyApiId(activeStudy.getApiId());
        randomizePatientRequestBodyV1.setStudyStrataParams("{\"age group - API ID\":\"18 - 100 - API ID\",\"gender - API ID\":\"m - API ID\"}");

        final ObjectMapper objectMapper = new ObjectMapper();

        final String studyArm = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/randomizePatient")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(objectMapper.writeValueAsString(randomizePatientRequestBodyV1)))
                .andExpect(status().isOk())
                .andExpect(content().string("First arm of active study"))
                .andReturn().getResponse().getContentAsString();

        final List<Subject> subjectList = activeStudy.getSubjectLists().get(3).getSubjects();
        assertEquals(1, subjectList.size(), "Number of subjects does not match!");

        final Subject subject = subjectList.get(subjectList.size() - 1);
        assertEquals(pseudonym, subject.getPseudonym(), "Unexpected pseudonym of the subject!");
        assertEquals(studyArm, subject.getStudyArm().getGuiName(), "Unexpected study arm!");

        testLastAuditEntryForSubject(activeStudy.getId(), AuditType.CREATE, null, subject.getId());
    }

    @Test
    public void randomizePatientMissingStrataTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final Site firstSite = activeStudy.getSites().get(0);

        final String pseudonym = "subject1";

        final RandomizePatientRequestBodyV1 randomizePatientRequestBodyV1 = new RandomizePatientRequestBodyV1();
        randomizePatientRequestBodyV1.setLocationApiId(firstSite.getApiId());
        randomizePatientRequestBodyV1.setPseudonym(pseudonym);
        randomizePatientRequestBodyV1.setStudyApiId(activeStudy.getApiId());

        final ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/v1/randomizePatient")
                                .header("accept-language", "en-US")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(randomizePatientRequestBodyV1)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("[R1040] No matching stratum with API ID 'gender - API ID' has been found."));
    }

    @Test
    public void fetchParticipantAssignmentNoAuthorizationTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final Site secondtSite = activeStudy.getSites().get(1);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/fetchAssignment")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .param("studyId", activeStudy.getApiId())
                        .param("location", secondtSite.getApiId())
                        .param("pseudonym", "pseudonym2"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("status").value(403))
               .andExpect(jsonPath("errors").value("Access Denied"));
    }

    @Test
    public void fetchParticipantAssignmentTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final Site firstSite = activeStudy.getSites().get(0);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/fetchAssignment")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .param("studyId", activeStudy.getApiId())
                        .param("location", firstSite.getApiId())
                        .param("pseudonym", "pseudonym1"))
                .andExpect(status().isOk())
                .andExpect(content().string(activeStudy.getStudyArms().get(0).getGuiName()));

        testLastAuditEntryForStudy(activeStudy.getId(), AuditType.READ_SUBJECTS, null);
    }

    @Test
    public void fetchParticipantAssignmentNoEntryTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final Site firstSite = activeStudy.getSites().get(0);
        final String pseudonym = "wrongPseudonym";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/fetchAssignment")
                                              .header("accept-language", "en-US")
                                              .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                              .param("studyId", activeStudy.getApiId())
                                              .param("location", firstSite.getApiId())
                                              .param("pseudonym", pseudonym))
               .andExpect(status().isNotAcceptable())
               .andExpect(content().string("[R5025] Study '" + activeStudy.getApiId() +
                                           "' does not contain an entry with pseudonym '" + pseudonym +
                                           "' in location '" + firstSite.getApiId() + "'"));
    }

    @Test
    public void fetchParticipantAssignmentNoEntryEnUsTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final Site firstSite = activeStudy.getSites().get(0);
        final String pseudonym = "wrongPseudonym";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/fetchAssignment")
                                              .header("accept-language", "en-US")
                                              .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                              .param("studyId", activeStudy.getApiId())
                                              .param("location", firstSite.getApiId())
                                              .param("pseudonym", pseudonym))
               .andExpect(status().isNotAcceptable())
               .andExpect(content().string("[R5025] Study '" + activeStudy.getApiId() +
                                           "' does not contain an entry with pseudonym '" + pseudonym +
                                           "' in location '" + firstSite.getApiId() + "'"));
    }

    @Test
    public void fetchParticipantAssignmentNoEntryDeDeTest() throws Exception {
        final Study activeStudy = getActiveStudy();
        final Site firstSite = activeStudy.getSites().get(0);
        final String pseudonym = "wrongPseudonym";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/fetchAssignment")
                                              .header("accept-language", "de-DE")
                                              .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                              .param("studyId", activeStudy.getApiId())
                                              .param("location", firstSite.getApiId())
                                              .param("pseudonym", pseudonym))
               .andExpect(status().isNotAcceptable())
               .andExpect(content().string("[R5025] Studie '" + activeStudy.getApiId() +
                                           "' enthält keinen Eintrag mit dem Pseudonym '" + pseudonym +
                                           "' im Standort '" + firstSite.getApiId() + "'"));
    }
}
