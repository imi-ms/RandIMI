package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.RandimiTest;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StudyUtilityServiceTest extends RandimiTest {

	private StudyUtilityService studyUtilityService;

	private StudyDTO study;

	@BeforeEach
	public void setUp() {
		studyUtilityService = new StudyUtilityService();

		List<StudyArmDTO> studyArms = new ArrayList<>();
		studyArms.add(new StudyArmDTO());
		studyArms.add(new StudyArmDTO());
		studyArms.get(0).setRatio(1);
		studyArms.get(1).setRatio(1);

		study = new StudyDTO(0);
		study.setStudyArms(studyArms);
	}

	@Test
	public void calculateNumberOfStudyArmParts() {
		int numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(study);
		assertEquals(2, numberStudyArmParts, "Unexpected number of study arm parts");

		study.getStudyArms().get(0).setRatio(3);
		numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(study);
		assertEquals(4, numberStudyArmParts, "Unexpected number of study arm parts");
	}

	@Test
	public void calculateNumberOfStudyArmPartsNull() {
		study.getStudyArms().get(0).setRatio(null);
		int numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(study);
		assertEquals(-1, numberStudyArmParts, "Unexpected number of study arm parts");
	}

	@Test
	public void calculateNumberOfStudyArmPartsZero() {
		study.getStudyArms().get(0).setRatio(0);
		int numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(study);
		assertEquals(-1, numberStudyArmParts, "Unexpected number of study arm parts");
	}

	@Test
	public void calculateNumberOfStudyArmPartsNegative() {
		study.getStudyArms().get(0).setRatio(-1);
		int numberStudyArmParts = studyUtilityService.calculateNumberOfStudyArmParts(study);
		assertEquals(-1, numberStudyArmParts, "Unexpected number of study arm parts");
	}

	@Test
	public void simplifyStudyArmRatios() {
		study.getStudyArms().add(new StudyArmDTO());
		study.getStudyArms().get(0).setRatio(9);
		study.getStudyArms().get(1).setRatio(15);
		study.getStudyArms().get(2).setRatio(18);

		studyUtilityService.simplifyStudyArmRatios(study);
		assertEquals(List.of(3, 5, 6), studyUtilityService.getRatios(study), "Ratios are not simplified correctly!");
	}

	@Test
	public void simplifyStudyArmRatiosSimplified() {
		studyUtilityService.simplifyStudyArmRatios(study);
		assertEquals(List.of(1, 1), studyUtilityService.getRatios(study), "Ratios should not have been modified!");
	}

	@Test
	public void simplifyStudyArmRatiosInvalid() {
		study.getStudyArms().get(0).setRatio(null);
		studyUtilityService.simplifyStudyArmRatios(study);
		assertEquals(Arrays.asList(null, 1), studyUtilityService.getRatios(study), "Ratios should not have been modified!");
	}
}
