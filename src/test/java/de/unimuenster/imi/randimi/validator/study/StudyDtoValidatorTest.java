package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.model.enumeration.*;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.repository.study.SiteRepository;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.RandimiExceptionFactoryService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import de.unimuenster.imi.randimi.service.StudyUtilityService;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import de.unimuenster.imi.randimi.validator.study.stratum.StratumDTOValidator;
import de.unimuenster.imi.randimi.service.algorithms.Randomization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Paul Schaub
 */
public class StudyDtoValidatorTest extends ValidatorTestBase {

    @Autowired NamesDTOValidator namesDTOValidator;
    @Autowired StudyArmDTOValidator studyArmDTOValidator;
    @Autowired StratumDTOValidator stratumDTOValidator;
    @Autowired SiteDTOValidator siteDTOValidator;

    @Autowired List<Randomization> availableAlgorithms;

    @Autowired RandimiExceptionFactoryService ex;
    @Autowired StratumCodeService stratumCodeService;
    @Autowired StudyUtilityService studyUtilityService;

	private StudyDTOValidator validator;

    private static final long INEXISTENT = 0L;
    private static final long ACTIVE = 1L;
    private static final long INACTIVE = 2L;

    @BeforeEach
    public void setup() {
        // Create test studies
        Study active = new Study();
        active.setId(ACTIVE);
        active.setGuiName("Active");
        active.setStatus(StudyStatus.ACTIVE);
        active.setActivationDate(new Timestamp(System.currentTimeMillis()));
        active.getSites().add(new Site());

        Study inactive = new Study();
        inactive.setId(INACTIVE);
        inactive.setGuiName("Inactive");

        // mock study dao to return test study for id 1L
	    StudyRepository studyRepository = mock(StudyRepository.class);
        when(studyRepository.existsByGuiNameOrApiId("Active", "Active")).thenReturn(true);
        when(studyRepository.findById(ACTIVE)).thenReturn(Optional.of(active));
        when(studyRepository.existsByGuiNameOrApiId("Inactive", "Inactive")).thenReturn(true);
        when(studyRepository.findById(INACTIVE)).thenReturn(Optional.of(inactive));
        when(studyRepository.existsByGuiNameOrApiId("Inexistent", "Inexistent")).thenReturn(false);
        when(studyRepository.findById(INEXISTENT)).thenReturn(Optional.empty());

	    SubjectRepository subjectRepository = mock(SubjectRepository.class);
		when(subjectRepository.countBlockingSubjectInStudy(ACTIVE)).thenReturn(20L);
		when(subjectRepository.countBlockingSubjectInStudyAndSite(ACTIVE, SiteDtoValidatorTest.SITE_ID)).thenReturn(20L);
		when(subjectRepository.countBySubjectListStudyIdAndSiteIdAndStatusAndPseudonymNotNull(ACTIVE, SiteDtoValidatorTest.SITE_ID, SubjectStatus.ACTIVE)).thenReturn(20L);

	    SiteRepository siteRepository = mock(SiteRepository.class);
		when(siteRepository.getNewAndDeletedSites(ArgumentMatchers.<StudyDTO>any()))
				.thenReturn(Pair.of(new ArrayList<>(), new ArrayList<Site>()));

		// Create validator with mocked study dao
        validator = new StudyDTOValidator(messageService, siteRepository, studyRepository, subjectRepository,
                                          namesDTOValidator, studyArmDTOValidator, stratumDTOValidator,
                                          siteDTOValidator, availableAlgorithms, stratumCodeService,
                                          studyUtilityService);
    }

    @Test
    public void validateValidActiveStudy() {
        Errors errors = mock(Errors.class);
        StudyDTO studyDTO = getValidStudyDto(ACTIVE, RandomizationAlgorithm.COINTOSS);

        validator.validate(studyDTO, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    public void validateValidInactiveStudy() {
        Errors errors = mock(Errors.class);
        StudyDTO studyDTO = getValidStudyDto(INACTIVE, RandomizationAlgorithm.BLOCKED);

        validator.validate(studyDTO, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    public void validateValidInexistentStudy() {
        Errors errors = mock(Errors.class);
        StudyDTO studyDTO = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);

        validator.validate(studyDTO, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    //=====================================
    //--- NamesDTOValidator integration ---
    //=====================================

    @Test
    public void validateNamesValidator() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setGuiName(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.nameEmpty"));
    }

    //===============
    //--- GuiName ---
    //===============

    @Test
    public void validateStudyWithGuiNameAlreadyExists() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setGuiName("Active");

        validator.validate(dto, errors);

        verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.mustBeUnique"));
    }

    //===========
    //-- ApiId --
    //===========

    @Test
    public void validateStudyWithApiNameAlreadyExists() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setApiId("Active");

        validator.validate(dto, errors);

        verify(errors).rejectValue("apiId", "errormessage", getMsg("validator.general.mustBeUnique"));
    }

    //=========================
    //--- PseudonymHandling ---
    //=========================

    @Test
    public void validatePseudonymHandlingNull() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setPseudonymHandling(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("pseudonymHandling", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    //==============================
    //--- RandomizationAlgorithm ---
    //==============================

    @Test
    public void validateRandomizationAlgorithmNull() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setRandomizationAlgorithm(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("randomizationAlgorithm", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    //================
    //--- Capacity ---
    //================

    @Test
    public void validateCapacityNull()
    {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setCapacity(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateCapacityToSmall()
    {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setCapacity(0);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 1));
    }

    @Test
    public void validateCapacityNotDivisibleByStudyArms()
    {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setCapacity(7);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.study.studySizeNotDivisibleByStudyArms", 2));
    }

    @Test
    public void validateCapacityNotDivisibleByStudyArmsInActiveStudy()
    {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(ACTIVE, RandomizationAlgorithm.COINTOSS);
        dto.setCapacity(7);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.study.studySizeNotDivisibleByStudyArms", 2));
    }

    @Test
    public void validateCapacityNotDivisibleByStudyArmsRatio()
    {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.getStudyArms().get(0).setRatio(3);
        dto.setCapacity(6);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.study.studySizeNotDivisibleByStudyArms", 4));
    }

    @Test
    public void validateCapacityNotDivisibleByProductStratumsStudyArms()
    {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setCapacity(20);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.study.studySizeNotDivisibleByProductStratumsStudyArms", 12));
    }

	@Test
	public void validateCapacitySmallerThanSize() {
		Errors errors = mock(Errors.class);
		StudyDTO dto = getValidStudyDto(ACTIVE, RandomizationAlgorithm.COINTOSS);
		dto.setCapacity(12);

		validator.validate(dto, errors);

		verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.study.capacitySmallerThanSize", 20));
	}

    @Test
    public void validateCapacityNotDivisibleByStudyArmsStratifiedBySite()
    {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setStratifyBySite(true);
        dto.getSites().get(0).setCapacity(11);

        validator.validate(dto, errors);

        verify(errors).rejectValue("sites[0].capacity", "errormessage", getMsg("validator.site.capacityNotDivisibleByStudyArms", 2));
    }

    @Test
    public void validateCapacityNotDivisibleByProductStratumsStudyArmsStratifiedBySite()
    {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setStratifyBySite(true);
        dto.getSites().get(0).setCapacity(14);

        validator.validate(dto, errors);

        verify(errors).rejectValue("sites[0].capacity", "errormessage", getMsg("validator.site.capacityNotDivisibleByProductStratumsStudyArms", 12));
    }

    //=====================
    //--- Site Capacity ---
    //=====================

    @Test
    public void validateSiteCapacitySmallerThanSize() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(ACTIVE, RandomizationAlgorithm.COINTOSS);
        dto.getSites().get(0).setCapacity(14);

        validator.validate(dto, errors);

        verify(errors).rejectValue("sites[0].capacity", "errormessage", getMsg("validator.site.capacitySmallerThanSize", 20));
    }

    //===============================
    //--- Study and Site Capacity ---
    //===============================

    @Test
    public void validateStudySizeGreaterThanSumOfSiteCapacities() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(ACTIVE, RandomizationAlgorithm.COINTOSS);
        dto.getSites().get(0).setCapacity(14);

        validator.validate(dto, errors);

        String message = getMsg("validator.study.studySizeGreaterThanSumOfSiteCapacities", 14, 48);
        verify(errors).rejectValue("capacity", "errormessage", message);
        verify(errors).rejectValue("sites[0].capacity", "errormessage", message);
    }

    //======================
    //--- StratifyBySite ---
    //======================

    @Test
    public void validateStratifyActiveStudyBySite() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(ACTIVE, RandomizationAlgorithm.COINTOSS);
        dto.setStratifyBySite(true);

        validator.validate(dto, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    public void validateStratifyActivePreGeneratedStudyBySite() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(ACTIVE, RandomizationAlgorithm.COINTOSS);
        dto.setPreGenerateSubjectList(true);
        dto.setStratifyBySite(true);
        dto.getSites().get(0).setCapacity(dto.getCapacity());

        validator.validate(dto, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    public void validateStratifyActivePreGeneratedStudyBySiteCapacityMismatch() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(ACTIVE, RandomizationAlgorithm.COINTOSS);
        dto.setPreGenerateSubjectList(true);
        dto.setStratifyBySite(true);
        dto.getSites().get(0).setCapacity(0);

        validator.validate(dto, errors);

        verify(errors).rejectValue("stratifyBySite", "errormessage",
                                   getMsg("validator.study.stratifyBySite.activeAndCapacityMismatch"));
    }

    //=================
    //--- StudyArms ---
    //=================

    @Test
    public void validateStudyArmValidator() {
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        Errors errors = new BeanPropertyBindingResult(dto, "dto");
        dto.getStudyArms().get(0).setGuiName(null);

        validator.validate(dto, errors);

        assertEquals(1, errors.getErrorCount(), "Unexpected number of errors!");
        assertTrue(errors.hasFieldErrors("studyArms[0].guiName"), "Field error has not been created!");
        assertEquals(getMsg("validator.general.nameEmpty"),
                     errors.getFieldError("studyArms[0].guiName").getDefaultMessage(), "Error contains the wrong message!");
    }

    @Test
    public void validateNoStudyArms() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setStudyArms(Collections.emptyList());

        validator.validate(dto, errors);

        verify(errors).rejectValue("studyArms", "errormessage", getMsg("validator.study.studyArmsLessThanTwo"));
    }

    @Test
    public void validateTooFewStudyArms() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.setStudyArms(Collections.singletonList(dto.getStudyArms().get(0)));

        validator.validate(dto, errors);

        verify(errors).rejectValue("studyArms", "errormessage", getMsg("validator.study.studyArmsLessThanTwo"));
    }

    @Test
    public void validateDuplicateStudyArmNames() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);

        dto.getStudyArms().get(0).setGuiName("A");
        dto.getStudyArms().get(1).setGuiName("A");

        validator.validate(dto, errors);

        verify(errors).rejectValue("studyArms[0].guiName", "errormessage", getMsg("validator.general.nameNotUnique"));
        verify(errors).rejectValue("studyArms[1].guiName", "errormessage", getMsg("validator.general.nameNotUnique"));
    }

    @Test
    public void validateDuplicateStudyArmApiIds() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);

        dto.getStudyArms().get(0).setApiId("same");
        dto.getStudyArms().get(1).setApiId("same");

        validator.validate(dto, errors);

        verify(errors).rejectValue("studyArms[0].apiId", "errormessage", getMsg("validator.general.apiIdNotUnique"));
        verify(errors).rejectValue("studyArms[1].apiId", "errormessage", getMsg("validator.general.apiIdNotUnique"));
    }

    //==============
    //--- Strata ---
    //==============

    @Test
    public void validateStratumValidator() {
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        Errors errors = new BeanPropertyBindingResult(dto, "dto");
        dto.getEnumeratedStratums().get(0).setGuiName(null);

        validator.validate(dto, errors);

        assertEquals(1, errors.getErrorCount(), "Unexpected number of errors!");
        assertTrue(errors.hasFieldErrors("enumeratedStratums[0].guiName"), "Field error has not been created!");
        assertEquals(getMsg("validator.general.nameEmpty"),
                     errors.getFieldError("enumeratedStratums[0].guiName").getDefaultMessage(), "Error contains the wrong message!");
    }

    @Test
    public void validateDuplicateStrataNames() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.getEnumeratedStratums().add(getValidEnum());

        validator.validate(dto, errors);

        verify(errors).rejectValue("enumeratedStratums[0].guiName", "errormessage", getMsg("validator.general.nameNotUnique"));
        verify(errors).rejectValue("enumeratedStratums[1].guiName", "errormessage", getMsg("validator.general.nameNotUnique"));
    }

    @Test
    public void validateDuplicateStrataApiIds() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        dto.getEnumeratedStratums().add(getValidEnum());

        validator.validate(dto, errors);

        verify(errors).rejectValue("enumeratedStratums[0].apiId", "errormessage", getMsg("validator.general.apiIdNotUnique"));
        verify(errors).rejectValue("enumeratedStratums[1].apiId", "errormessage", getMsg("validator.general.apiIdNotUnique"));
    }

    //==================
    //--- Blocksizes ---
    //==================

    @Test
    public void validateMinBlockSizeNull() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);
        dto.setMinBlocksize(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("minBlocksize", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateMinBlockTooSmall() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);
        dto.setMinBlocksize(1);

        validator.validate(dto, errors);

        verify(errors).rejectValue("minBlocksize", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 2));
    }

    @Test
    public void validateMaxBlockSizeNull() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);
        dto.setMaxBlocksize(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("maxBlocksize", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateMaxBlockTooSmall() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);
        dto.setMaxBlocksize(1);

        validator.validate(dto, errors);

        verify(errors).rejectValue("maxBlocksize", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 2));
    }

    @Test
    public void validateMinBlockSizeLargerThanMaxBlockSize() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);
        dto.setMinBlocksize(6);
        dto.setMaxBlocksize(4); // smaller.

        validator.validate(dto, errors);

        verify(errors).rejectValue("minBlocksize", "errormessage", getMsg("validator.study.minBlocksizeLargerThanMaxBlocksize"));
    }


    @Test
    public void validateNumberOfPatientsPerStratumCombinationSmallerMinBlockSize() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);

        dto.setMinBlocksize(10);
        dto.setMaxBlocksize(12);

        validator.validate(dto, errors);
        verify(errors).rejectValue("minBlocksize", "errormessage", getMsg("validator.study.minBlocksizeLargerThanStratumSize", 8));
    }

    @Test
    public void validateNumberOfPatientsPerStratumCombinationSmallerMaxBlockSize() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);

        dto.setMaxBlocksize(12);

        validator.validate(dto, errors);
        verify(errors).rejectValue("maxBlocksize", "errormessage", getMsg("validator.study.maxBlocksizeLargerThanStratumSize", 8));
    }

    //============
    //--- Sites ---
    //============

    @Test
    public void validateSiteValidator() {
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.COINTOSS);
        Errors errors = new BeanPropertyBindingResult(dto, "dto");

        dto.getSites().get(0).setGuiName(null);

        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("sites[0].guiName"), "Field error has not been created!");
        assertEquals(getMsg("validator.general.nameEmpty"),
                     errors.getFieldError("sites[0].guiName").getDefaultMessage(), "Error contains the wrong message!");
    }

    @Test
    public void validateDuplicateSiteNames() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);

        SiteDTO newSite = SiteDtoValidatorTest.getValidSiteDto();
        newSite.setApiId("Test2");
        dto.getSites().add(newSite);

        validator.validate(dto, errors);
        String errorMessage = getMsg("validator.general.nameNotUnique");
        verify(errors).rejectValue("sites[0].guiName", "errormessage", errorMessage);
        verify(errors).rejectValue("sites[1].guiName", "errormessage", errorMessage);
    }

    @Test
    public void validateDuplicateSiteApiIds() {
        Errors errors = mock(Errors.class);
        StudyDTO dto = getValidStudyDto(INEXISTENT, RandomizationAlgorithm.BLOCKED);

        SiteDTO newSite = SiteDtoValidatorTest.getValidSiteDto();
        newSite.setGuiName("Test2");
        dto.getSites().add(newSite);

        validator.validate(dto, errors);
        String errorMessage = getMsg("validator.general.apiIdNotUnique");
        verify(errors).rejectValue("sites[0].apiId", "errormessage", errorMessage);
        verify(errors).rejectValue("sites[1].apiId", "errormessage", errorMessage);
    }

    private StudyDTO getValidStudyDto(long which, RandomizationAlgorithm randomizationAlgorithm) {
        StudyDTO dto;
        switch ((int) which) {
            case (int) ACTIVE:
                dto = baseActiveStudy();
                break;
            case (int) INACTIVE:
                dto = baseInactiveStudy();
                break;
            default:
                dto = baseInexistentStudy();
                break;
        }
        dto.setPreGenerateSubjectList(false);
        dto.setPseudonymHandling(PseudonymHandling.UNIQUE_IN_LOCATION);
        dto.setDescription("A study.");

        dto.setRandomizationAlgorithm(randomizationAlgorithm);
        switch (randomizationAlgorithm) {
            case BLOCKED:
                dto.setMinBlocksize(4);
                dto.setMaxBlocksize(6);
                break;
            case COINTOSS:
                break;
//            case UPLOAD_LIST:
//                break;
        }

        dto.setCapacity(48);

        StudyArmDTO arm1 = getValidStudyArm("A");
        StudyArmDTO arm2 = getValidStudyArm("B");
        dto.setStudyArms(Arrays.asList(arm1, arm2));

        StratumDTO intervalStratum = new StratumDTO();
        intervalStratum.setStratumType(StratumType.INTERVAL);
        intervalStratum.setGuiName("Interval");
        intervalStratum.setApiId("Interval");
        intervalStratum.setUseApiId(true);
        StratumPartBaseDTO part1 = new StratumPartBaseDTO();
        part1.setIntervalBegin(1f);
        part1.setIntervalEnd(2f);
        StratumPartBaseDTO part2 = new StratumPartBaseDTO();
        part2.setIntervalBegin(3f);
        part2.setIntervalEnd(4f);
        intervalStratum.setStratumParts(Arrays.asList(part1, part2));
        dto.setIntervalStratums(Arrays.asList(intervalStratum));

        StratumDTO enumStratum = getValidEnum();
        dto.setEnumeratedStratums(new ArrayList<>(List.of(enumStratum)));

		List<SiteDTO> siteDTOs = new ArrayList<>();
		siteDTOs.add(SiteDtoValidatorTest.getValidSiteDto());
		dto.setSites(siteDTOs);

        return dto;
    }
    private StudyDTO baseInexistentStudy() {
        StudyDTO dto = new StudyDTO(INEXISTENT);
        dto.setGuiName("Inexistent");
        dto.setApiId("Inexistent");
        dto.setUseApiId(true);
        dto.setStatus(StudyStatus.INEXISTENT);
        dto.setActivationDate(null);
        return dto;
    }

    private StudyDTO baseActiveStudy() {
        StudyDTO dto = new StudyDTO(ACTIVE);
        dto.setGuiName("Active");
        dto.setApiId("Active");
        dto.setUseApiId(true);
        dto.setStatus(StudyStatus.ACTIVE);
        dto.setActivationDate(new Timestamp(System.currentTimeMillis()));
        return dto;
    }

    private StudyDTO baseInactiveStudy() {
        StudyDTO dto = new StudyDTO(INACTIVE);
        dto.setGuiName("Inactive");
        dto.setApiId("Inactive");
        dto.setUseApiId(true);
        dto.setStatus(StudyStatus.CREATED);
        dto.setActivationDate(null);
        return dto;
    }

    private StudyArmDTO getValidStudyArm(final String name) {
        StudyArmDTO arm = new StudyArmDTO();
        arm.setGuiName(name);
        arm.setApiId(name);
        arm.setUseApiId(true);
        arm.setRatio(1);
        return arm;
    }

    private StratumDTO getValidEnum() {
        StratumDTO enumStratum = new StratumDTO();
        enumStratum.setStratumType(StratumType.ENUM);
        enumStratum.setGuiName("Enumeration");
        enumStratum.setApiId("Enumeration");
        enumStratum.setUseApiId(true);

        StratumPartBaseDTO p1 = getValidEnumPart("One");
        StratumPartBaseDTO p2 = getValidEnumPart("Two");
        StratumPartBaseDTO p3 = getValidEnumPart("Three");

        enumStratum.setStratumParts(Arrays.asList(p1, p2, p3));

        return enumStratum;
    }

    private StratumPartBaseDTO getValidEnumPart(final String name) {
        StratumPartBaseDTO part = new StratumPartBaseDTO();
        part.setEnumValue(name);
        part.setApiId(name);
        part.setUseApiId(true);
        return part;
    }

}
