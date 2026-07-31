package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.dto.ChangeReason;
import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.dto.subject.SubjectDTO;
import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import de.unimuenster.imi.randimi.dto.user.UserDTO;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.mapping.user.AccountDetailsMapper;
import de.unimuenster.imi.randimi.model.enumeration.*;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@WithUserDetails(value = "admin", userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
public abstract class MVCControllerTestBase extends ControllerTestBase {

    protected static final String STUDY_GUI_NAME = "Study";

    @Autowired
    protected RandimiUserRepository randimiUserRepository;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    protected StudyController studyController;

    @Autowired
    protected StudyMapper studyMapper;
    @Autowired
    protected AccountDetailsMapper accountDetailsMapper;

    @Autowired
    protected MessageService messageService;

    protected Model model;

    protected RedirectAttributes redirectAttributes;

    private static int pseudonymIndex;

    @Autowired
    private WebApplicationContext wac;

    protected MockMvc mockMvc;

    @BeforeEach
    public void init() {
        model = new ExtendedModelMap();
        redirectAttributes = new RedirectAttributesModelMap();
        pseudonymIndex = 0;
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    protected <T> T testAndGetModelAttribute(final Model model, final String attributeName, final Class<T> clazz) {
        assertTrue(model.containsAttribute(attributeName), "Attribute '" + attributeName + "' not set!");
        T t = null;

        try {
            t = clazz.cast(model.getAttribute(attributeName));
        } catch (ClassCastException exception) {
            fail("Attribute '" + attributeName + "' is of the wrong type!", exception);
        }

        assertNotNull(t, "Attribute '" + attributeName + "' set but null!");
        return t;
    }

    protected <T> List<T> testAndGetListModelAttribute(final Model model, final String attributeName,
                                                       final Class<T> clazz) {
        assertTrue(model.containsAttribute(attributeName), "Attribute '" + attributeName + "' not set!");
        List<T> tList = null;

        try {
            @SuppressWarnings("unchecked")
            List<Object> objectList = (List<Object>) model.getAttribute(attributeName);
            tList = objectList.stream().map(object -> clazz.cast(object)).collect(Collectors.toList());
        } catch (ClassCastException exception) {
            fail("Attribute '" + attributeName + "' is of the wrong type!", exception);
        }

        assertNotNull(tList, "Attribute '" + attributeName + "' set but null!");
        return tList;
    }

    protected String buildErrorMessage(int errorCode, String messagePlaceholder, String nameOrId) {
        return "[" + RandimiException.ERROR_CODE_PREFIX + errorCode + "] "
               + messageService.getMessage(messagePlaceholder, nameOrId);
    }

    protected StudyDTO getValidStudyDTONoStratification(RandomizationAlgorithm randimizaAlgorithm) {
        StudyDTO studyDTO = new StudyDTO(0L);

        studyDTO.setCapacity(24);
        studyDTO.setDescription("A study.");
        studyDTO.setGuiName(STUDY_GUI_NAME);
        studyDTO.setApiId("24");
        studyDTO.setUseApiId(true);
        studyDTO.setPreGenerateSubjectList(false);
        studyDTO.setPseudonymHandling(PseudonymHandling.UNIQUE_IN_LOCATION);
        studyDTO.setRandomizationAlgorithm(randimizaAlgorithm);

        studyDTO.setMinBlocksize(4);
        studyDTO.setMaxBlocksize(4);

        StudyArmDTO arm1 = new StudyArmDTO();
        arm1.setGuiName("A");
        arm1.setApiId("A");
        arm1.setUseApiId(false);
        arm1.setRatio(1);
        StudyArmDTO arm2 = new StudyArmDTO();
        arm2.setGuiName("B");
        arm2.setApiId("B");
        arm2.setUseApiId(false);
        arm2.setRatio(1);
        studyDTO.setStudyArms(Arrays.asList(arm1, arm2));

        final List<SiteDTO> sites = new ArrayList<>(2);
        SiteDTO siteDTOA = new SiteDTO();
        siteDTOA.setApiId("Site A");
        siteDTOA.setCapacity(12);
        siteDTOA.setGuiName("Site A");
        siteDTOA.setUseApiId(true);
        siteDTOA.setOrderNumber(0);
        siteDTOA.setSeed(123L);
        sites.add(siteDTOA);

        SiteDTO siteDTOB = new SiteDTO();
        siteDTOB.setApiId("Site B");
        siteDTOB.setCapacity(12);
        siteDTOB.setGuiName("Site B");
        siteDTOB.setUseApiId(true);
        siteDTOB.setOrderNumber(1);
        siteDTOB.setSeed(321L);
        sites.add(siteDTOB);
        studyDTO.setSites(sites);

        studyDTO.setStratifyBySite(false);

        return studyDTO;
    }

    protected StudyDTO getValidStudyDTONoStratificationCointoss() {
        return getValidStudyDTONoStratification(RandomizationAlgorithm.COINTOSS);
    }

    protected StudyDTO getValidStudyDTONoStratificationBlocked() {
        return getValidStudyDTONoStratification(RandomizationAlgorithm.BLOCKED);
    }

    protected StudyDTO getValidStudyDTONoStratificationBlockedPreGenerated() {
        final StudyDTO studyDTO = getValidStudyDTONoStratificationBlocked();
        studyDTO.setPreGenerateSubjectList(true);
        return studyDTO;
    }

    protected StudyDTO getValidStudyDTONoStratificationBlockedRatio() {
        final StudyDTO studyDTO = getValidStudyDTONoStratificationBlocked();
        studyDTO.getStudyArms().get(0).setRatio(3);
        return studyDTO;
    }

    protected StratumDTO getValidStratumDTOA() {
        return getValidStratumDTO("A", 2);
    }

    protected StratumDTO getValidStratumDTOB() {
        return getValidStratumDTO("B", 3);
    }

    protected StratumDTO getValidStratumDTO(final String letter, final int numberParts) {
        StratumDTO enumStratum = new StratumDTO();
        enumStratum.setStratumType(StratumType.ENUM);
        enumStratum.setGuiName("Enumeration" + letter);
        enumStratum.setApiId("EnumerationApi" + letter);
        enumStratum.setUseApiId(true);

        final List<StratumPartBaseDTO> parts = new ArrayList<>();
        for (int i = 0; i < numberParts; i++) {
            StratumPartBaseDTO part = new StratumPartBaseDTO();
            part.setEnumValue(letter + i);
            part.setApiId(letter + i);
            part.setUseApiId(true);
            parts.add(part);
        }
        enumStratum.setStratumParts(parts);

        return enumStratum;
    }

    protected StudyDTO getValidStudyDTOStratificationNotBySiteCointoss() {
        StudyDTO studyDTO = getValidStudyDTONoStratificationCointoss();

        List<StratumDTO> strata = new ArrayList<StratumDTO>();
        strata.add(getValidStratumDTOA());
        strata.add(getValidStratumDTOB());
        studyDTO.setEnumeratedStratums(strata);

        return studyDTO;
    }

    protected StudyDTO getValidStudyDTOStratificationOnlyBySiteCointoss() {
        StudyDTO studyDTO = getValidStudyDTONoStratificationCointoss();
        studyDTO.setStratifyBySite(true);
        return studyDTO;
    }

    protected StudyDTO getValidStudyDTOStratificationCointoss() {
        StudyDTO studyDTO = getValidStudyDTOStratificationNotBySiteCointoss();
        studyDTO.setStratifyBySite(true);
        return studyDTO;
    }

    protected StudyDTO getValidStudyDTOStratificationNotBySiteBlocked() {
        StudyDTO studyDTO = getValidStudyDTONoStratificationBlocked();
        studyDTO.setMinBlocksize(2);
        studyDTO.setMaxBlocksize(2);

        List<StratumDTO> strata = new ArrayList<StratumDTO>();
        strata.add(getValidStratumDTOA());
        strata.add(getValidStratumDTOB());
        studyDTO.setEnumeratedStratums(strata);

        return studyDTO;
    }

    protected StudyDTO getValidStudyDTOStratificationBlocked() {
        StudyDTO studyDTO = getValidStudyDTOStratificationNotBySiteBlocked();
        studyDTO.setStratifyBySite(true);
        return studyDTO;
    }

    protected Study createStudy(final StudyDTO studyDTO) {
        final ChangeReason changeReason = new ChangeReason();
        final BindingResult bindingResultStudy = new BeanPropertyBindingResult(studyDTO, "StudyDTO");
        studyController.create("save", studyDTO, bindingResultStudy, redirectAttributes);
        init();
        return studyRepository.findByGuiName(STUDY_GUI_NAME).get(0);
    }

    protected Study activateStudy(Study study) {
        studyController.activate(study.getId(), model, redirectAttributes);
        init();
        return study;
    }

    protected Study createStudyNoStratificationCointoss() {
        return createStudy(getValidStudyDTONoStratificationCointoss());
    }

    protected Study createAndActivateStudyNoStratificationCointoss() {
        return activateStudy(createStudyNoStratificationCointoss());
    }

    protected Study createStudyStratificationNotBySiteCointoss() {
        return createStudy(getValidStudyDTOStratificationNotBySiteCointoss());
    }

    protected Study createAndActivateStudyStratificationNotBySiteCointoss() {
        return activateStudy(createStudyStratificationNotBySiteCointoss());
    }

    protected Study createStudyStratificationOnlyBySiteCointoss() {
        return createStudy(getValidStudyDTOStratificationOnlyBySiteCointoss());
    }

    protected Study createAndActivateStudyStratificationOnlyBySiteCointoss() {
        return activateStudy(createStudyStratificationOnlyBySiteCointoss());
    }

    protected Study createStudyStratificationCointoss() {
        return createStudy(getValidStudyDTOStratificationCointoss());
    }

    protected Study createAndActivateStudyStratificationCointoss() {
        return activateStudy(createStudyStratificationCointoss());
    }

    protected Study createStudyNoStratificationBlocked() {
        return createStudy(getValidStudyDTONoStratificationBlocked());
    }

    protected Study createAndActivateStudyNoStratificationBlocked() {
        return activateStudy(createStudyNoStratificationBlocked());
    }

    protected Study createStudyStratificationBlocked() {
        return createStudy(getValidStudyDTOStratificationBlocked());
    }

    protected Study createAndActivateStudyStratificationBlocked() {
        return activateStudy(createStudyStratificationBlocked());
    }

    protected Study createStudyNoStratificationBlockedPreGenerate() {
        return createStudy(getValidStudyDTONoStratificationBlockedPreGenerated());
    }

    protected Study createAndActivateStudyNoStratificationBlockedPreGenerate() {
        return activateStudy(createStudyNoStratificationBlockedPreGenerate());
    }

    protected Study createStudyNoStratificationBlockedRatio() {
        return createStudy(getValidStudyDTONoStratificationBlockedRatio());
    }

    protected Study createAndActivateStudyNoStratificationBlockedRatio() {
        return activateStudy(createStudyNoStratificationBlockedRatio());
    }

    protected SubjectDTO getValidSubjectDTONoStratification(Study study, Site site) {
        SubjectDTO subjectDTO = new SubjectDTO();

        subjectDTO.setPseudonym("pseudonym-" + pseudonymIndex++);
        subjectDTO.setLocation(site.getGuiName());
        subjectDTO.setStudyId(study.getId());
        subjectDTO.setStudyApiId(study.getApiId());
        subjectDTO.setSiteId(site.getId());
        subjectDTO.setSiteApiId(site.getApiId());
        subjectDTO.setEnumeratedStratums(new String[0]);
        subjectDTO.setIntervalStratums(new Float[0]);

        return subjectDTO;
    }

    protected SubjectDTO getValidSubjectDTOStratificationNotBySite(Study study, Site site, String stratumA,
                                                                   String stratumB) {
        SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
        subjectDTO.setEnumeratedStratums(new String[] { stratumA, stratumB });
        return subjectDTO;
    }

    protected SubjectDTO getValidSubjectDTOStratificationOnlyBySite(Study study, Site site) {
        SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
        return subjectDTO;
    }

    protected SubjectDTO getValidSubjectDTOStratification(Study study, Site site, String stratumA, String stratumB) {
        SubjectDTO subjectDTO = getValidSubjectDTONoStratification(study, site);
        subjectDTO.setEnumeratedStratums(new String[] { stratumA, stratumB});
        return subjectDTO;
    }

    protected UserDTO getValidUserDTO(final boolean skipEMailValidation) {
        final UserDTO userDTO = new UserDTO();

        userDTO.setSkipEMailValidation(skipEMailValidation);
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEMail("email@mail.de");
        userDTO.setUsername("username");

        if (skipEMailValidation) {
            userDTO.setPassword("password");
            userDTO.setRepeatPassword("password");
        }

        final List<UserRoles> roles = Arrays.asList(UserRoles.values());
        userDTO.setUserRoles(roles.stream().map(UserRoles::toString).collect(Collectors.toList()));

        return userDTO;
    }

    protected AccountDetailsDTO getValidAccountDetailsDTO() {
        return accountDetailsMapper.toAccountDetailsDTO(getActiveUser());
    }

    protected AccountDetailsDTO getValidAccountDetailsDTOWithPasswordChange() {
        final AccountDetailsDTO dto = getValidAccountDetailsDTO();
        dto.setUpdatePassword(true);
        dto.setOldPassword("password");
        dto.setNewPassword("changedPassword");
        dto.setRepeatPassword("changedPassword");
        return dto;
    }

    void randomizeSubject(final SubjectDTO subjectDTO) throws Exception {
        mockMvc.perform(post("/subjects/add").with(csrf())
                                             .param("action", "add")
                                             .flashAttr("subject", subjectDTO))
                .andExpect(flash().attributeExists("success"));
    }
}
