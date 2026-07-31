package de.unimuenster.imi.randimi.model.study;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.mapping.study.StudyArmMapper;
import de.unimuenster.imi.randimi.Helper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class StudyArmTest extends RandimiIntegrationTest {

	private static final Random random = new Random();
	private StudyArm studyArm;

	@Autowired
	StudyArmMapper studyArmMapper;

	public StudyArmTest() {
	}

	@BeforeEach
	public void beforeTest() {
		studyArm = new StudyArm();
	}

	@Test
	public void testGetAndSetStudy() {
		Study testStudy = StudyTest.getValidStudy(false);
		studyArm.setStudy(testStudy);
		assertEquals(testStudy, studyArm.getStudy(), "The study returned was not the one expected.");
		assertTrue(testStudy.getStudyArms().contains(studyArm), "The study arm was not added to the study.");
	}

	@Test
	public void testGetAndSetOrderNumber() {
		int testOrderNumber = random.nextInt();
		studyArm.setOrderNumber(testOrderNumber);
		assertEquals(testOrderNumber, studyArm.getOrderNumber(), "The order number returned was not the one expected.");
	}

	@Test
	public void testGetAndSetGuiName() {
		String testGuiName = Helper.getRandomAlphanumericString(random.nextInt(50)+3);
		studyArm.setGuiName(testGuiName);
		assertEquals(testGuiName, studyArm.getGuiName(), "The gui name returned was not the one expected.");
	}

	@Test
	public void testToStudyArmDTO() {
		studyArm = getValidStudyArm(true);
		StudyArmDTO testStudyArmDTO = studyArmMapper.toStudyArmDTO(studyArm);
		assertEquals(studyArm.getId(), testStudyArmDTO.getId(),
		             "The id of the study arm DTO was not equal to the id of the study arm.");
		assertEquals(studyArm.getStudy().getId(), testStudyArmDTO.getStudyId(),
		             "The study id of the study arm DTO was not equal to the id of the study.");
		assertEquals(studyArm.getOrderNumber(), testStudyArmDTO.getOrderNumber(),
		             "The order number of the study arm DTO was not equal to the order number of the study arm.");
		assertEquals(studyArm.getGuiName(), testStudyArmDTO.getGuiName(),
		             "The gui name of the study arm DTO was not equal to the gui name of the study arm.");
	}

	public static StudyArm getValidStudyArm(boolean withStudy) {
		StudyArm validStudyArm = new StudyArm();
		validStudyArm.setOrderNumber(random.nextInt());
		validStudyArm.setGuiName(Helper.getRandomAlphanumericString(random.nextInt(50)+3));
		if(withStudy) {
			validStudyArm.setStudy(StudyTest.getValidStudy(false));
		}
		return validStudyArm;
	}
}
