package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.study.MinimizationParameterDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.MinimizationParameter;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartEnumeration;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import de.unimuenster.imi.randimi.service.StudyUtilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MinimizationRandomizationTest extends RandimiIntegrationTest {

	@Autowired
	private MessageService messageService;
	@Autowired
	private StratumCodeService stratumCodeService;
	@Autowired
	private StudyUtilityService studyUtilityService;

	private MinimizationRandomization minimization;

	private Study study;
	private SubjectList subjectList;
	private Site site;

	@BeforeEach
	public void setUp() {
		subjectList = new SubjectList();
		subjectList.setId(1L);

		site = new Site();
		site.setSeed(123L);

		study = new Study();
		study.setStatus(StudyStatus.ACTIVE);
		study.addSubjectList(subjectList);
		study.addSite(site);
		final StudyArm studyArmA = new StudyArm();
		studyArmA.setId(1L);
		studyArmA.setRatio(1);
		final StudyArm studyArmB = new StudyArm();
		studyArmB.setId(2L);
		studyArmB.setRatio(1);
		study.addStudyArm(studyArmA);
		study.addStudyArm(studyArmB);
		study.setCapacity(24);

		MinimizationParameter parameter = new MinimizationParameter();
		study.setMinimizationParameter(parameter);

		final SubjectRepository subjectRepository = mock(SubjectRepository.class);

		when(subjectRepository.countBlockingSubjectInSubjectListAndStudyArm(subjectList.getId(), studyArmA.getId()))
				.thenAnswer(invocation -> subjectList.getSubjects()
				                                     .stream()
				                                     .filter(subject -> subject.getStudyArm().getId() ==
				                                                        studyArmA.getId())
				                                     .count()
				);
		when(subjectRepository.countBlockingSubjectInSubjectListAndStudyArm(subjectList.getId(), studyArmB.getId()))
				.thenAnswer(invocation -> subjectList.getSubjects()
				                                     .stream()
				                                     .filter(subject -> subject.getStudyArm().getId() ==
				                                                        studyArmB.getId())
				                                     .count()
				);

		when(subjectRepository.countBlockingSubjectForStudyArmAndStratumPart(studyArmA.getId(), 1L))
				.thenAnswer(invocation -> subjectList.getSubjects()
				                                     .stream()
				                                     .filter(subject -> subject.getStudyArm().getId() ==
				                                                        studyArmA.getId())
				                                     .count() + 2

		);

		when(subjectRepository.countBlockingSubjectForStudyArmAndStratumPart(studyArmB.getId(), 1L))
				.thenAnswer(invocation -> subjectList.getSubjects()
				                                     .stream()
				                                     .filter(subject -> subject.getStudyArm().getId() ==
				                                                        studyArmB.getId())
				                                     .count() + 1
				);

		minimization = new MinimizationRandomization(messageService, stratumCodeService, studyUtilityService, subjectRepository);
	}

	//=========================
	//--- onStudyValidation ---
	//=========================

	@Test
	public void onStudyValidationValid() {
		Errors errors = mock(Errors.class);

		StudyDTO dto = getValidStudyDTO();
		dto.setPreGenerateSubjectList(true);

		minimization.onStudyDTOValidation(errors, dto);

		verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
	}

	@Test
	public void onStudyValidationPreGenerate() {
		Errors errors = mock(Errors.class);

		StudyDTO dto = getValidStudyDTO();
		dto.setPreGenerateSubjectList(true);

		minimization.onStudyDTOValidation(errors, dto);
		verify(errors, only()).rejectValue("preGenerateSubjectList",
		                                   "validator.study.preGenerateSubjectListMinimization");
	}

	@Test
	public void onStudyValidationParameterNull() {
		Errors errors = mock(Errors.class);

		StudyDTO dto = getValidStudyDTO();
		dto.setMinimizationParameter(null);

		minimization.onStudyDTOValidation(errors, dto);
		verify(errors, only()).rejectValue("minimizationParameter", "validator.general.mustNotBeNull");
	}

	@Test
	public void onStudyValidationForceRatioNull() {
		Errors errors = mock(Errors.class);

		StudyDTO dto = getValidStudyDTO();
		dto.getMinimizationParameter().setForceRatio(null);

		minimization.onStudyDTOValidation(errors, dto);
		verify(errors, only()).rejectValue("minimizationParameter.forceRatio", "validator.general.mustNotBeNull");
	}

	@Test
	public void onStudyValidationImbalanceBiasNull() {
		Errors errors = mock(Errors.class);

		StudyDTO dto = getValidStudyDTO();
		dto.getMinimizationParameter().setImbalanceBias(null);

		minimization.onStudyDTOValidation(errors, dto);
		verify(errors, only()).rejectValue("minimizationParameter.imbalanceBias", "validator.general.mustNotBeNull");
	}

	@Test
	public void onStudyValidationImbalanceBiasSmall() {
		Errors errors = mock(Errors.class);

		StudyDTO dto = getValidStudyDTO();
		dto.getMinimizationParameter().setImbalanceBias(-0.3f);

		minimization.onStudyDTOValidation(errors, dto);
		verify(errors, only()).rejectValue("minimizationParameter.imbalanceBias", "validator.general.mustBeBetween",
		                                   new Object[]{0, 1}, null);
	}

	@Test
	public void onStudyValidationImbalanceBiasLarge() {
		Errors errors = mock(Errors.class);

		StudyDTO dto = getValidStudyDTO();
		dto.getMinimizationParameter().setImbalanceBias(1.3f);

		minimization.onStudyDTOValidation(errors, dto);
		verify(errors, only()).rejectValue("minimizationParameter.imbalanceBias", "validator.general.mustBeBetween",
		                                   new Object[]{0, 1}, null);
	}

	@Test
	public void onStudyValidationImbalanceFunctionNull() {
		Errors errors = mock(Errors.class);

		StudyDTO dto = getValidStudyDTO();
		dto.getMinimizationParameter().setImbalanceFunction(null);

		minimization.onStudyDTOValidation(errors, dto);
		verify(errors, only()).rejectValue("minimizationParameter.imbalanceFunction", "validator.general.mustNotBeNull");
	}

	//=========================
	//--- getRandomStudyArm ---
	//=========================

	@Test
	public void getRandomStudyArmNoStratification() {
		List<Long> expectedAssignments = List.of(2L, 1L, 2L, 1L, 1L, 2L, 2L, 1L, 2L, 1L, 2L, 1L,
		                                         2L, 1L, 1L, 2L, 2L, 1L, 2L, 1L, 2L, 1L, 1L, 2L);
		List<Long> actualAssignments = new ArrayList<>(study.getCapacity());

		for (int i = 0; i < study.getCapacity(); i++) {
			StudyArm draw = minimization.getRandomStudyArm(site, subjectList);
			actualAssignments.add(draw.getId());
			subjectList.addSubject(new Subject(i, draw));
		}

		assertEquals(expectedAssignments, actualAssignments, "Unexpected assignments after the first allocation!");
	}

	@Test
	public void getRandomStudyArmNoStratificationRation() {
		subjectList.getStudy().getStudyArms().get(0).setRatio(2);

		List<Long> expectedAssignments = List.of(2L, 1L, 2L, 1L, 1L, 1L, 1L, 1L, 2L, 1L, 2L, 1L,
		                                         2L, 1L, 1L, 2L, 2L, 1L, 1L, 1L, 2L, 1L, 1L, 1L);
		List<Long> actualAssignments = new ArrayList<>(study.getCapacity());

		for (int i = 0; i < study.getCapacity(); i++) {
			StudyArm draw = minimization.getRandomStudyArm(site, subjectList);
			actualAssignments.add(draw.getId());
			subjectList.addSubject(new Subject(i, draw));
		}

		assertEquals(expectedAssignments, actualAssignments, "Unexpected assignments after the first allocation!");
	}

	@Test
	public void getRandomStudyArmStratification() {
		Stratum sa = new Stratum();
		study.addStratum(sa);

		Stratum sb = new Stratum();
		study.addStratum(sb);

		StratumPartEnumeration a1 = new StratumPartEnumeration();
		a1.setId(1);
		sa.addStratumPart(a1);
		subjectList.getStratumParts().add(a1);

		StratumPartEnumeration a2 = new StratumPartEnumeration();
		sa.addStratumPart(a2);

		StratumPartEnumeration b = new StratumPartEnumeration();
		b.setId(2);
		sb.addStratumPart(b);
		subjectList.getStratumParts().add(b);

		StratumPartEnumeration b2 = new StratumPartEnumeration();
		sb.addStratumPart(b2);

		List<Long> expectedAssignments = List.of(2L, 1L, 2L, 1L, 1L, 2L);
		List<Long> actualAssignments = new ArrayList<>(study.getCapacity() / 4);

		for (int i = 0; i < study.getCapacity() / 4; i++) {
			StudyArm draw = minimization.getRandomStudyArm(site, subjectList);
			actualAssignments.add(draw.getId());
			subjectList.addSubject(new Subject(i, draw));
		}

		assertEquals(expectedAssignments, actualAssignments, "Unexpected assignments after the first allocation!");
	}

	private StudyDTO getValidStudyDTO() {
		StudyDTO dto = new StudyDTO(123L);
		dto.setPreGenerateSubjectList(false);
		dto.setMinimizationParameter(new MinimizationParameterDTO());
		return dto;
	}

}
