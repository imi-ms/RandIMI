package de.unimuenster.imi.randimi.model.study;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.study.StudyDTO;
import de.unimuenster.imi.randimi.mapping.study.StudyMapper;
import de.unimuenster.imi.randimi.model.enumeration.PseudonymHandling;
import de.unimuenster.imi.randimi.model.enumeration.RandomizationAlgorithm;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumTest;
import de.unimuenster.imi.randimi.Helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class StudyTest extends RandimiIntegrationTest {

	private static final Random random = new Random();
	private Study study;

	@Autowired
	StudyMapper studyMapper;

	public StudyTest() {
	}

	@BeforeEach
	public void beforeTest() {
		study = new Study();
	}

	@Test
	public void testGetAndSetGuiName() {
		String testGuiName = Helper.getRandomAlphanumericString(random.nextInt(50) + 3);
		study.setGuiName(testGuiName);
		assertEquals(testGuiName, study.getGuiName(), "The gui name returned was not the one expected.");
	}

	@Test
	public void testGetAndSetDescription() {
		String testDescription = Helper.getRandomAlphanumericString(random.nextInt(500) + 10);
		study.setDescription(testDescription);
		assertEquals(testDescription, study.getDescription(), "The description returned was not the one expected.");
	}

	@Test
	public void testGetAndSetRandomizationAlgorithm() {
		RandomizationAlgorithm testRandomizationAlgorithm = Helper.getRandomEnum(RandomizationAlgorithm.class);
		study.setRandomizationAlgorithm(testRandomizationAlgorithm);
		assertEquals(testRandomizationAlgorithm, study.getRandomizationAlgorithm(),
		             "The study size returned was not the one expected.");
	}

	@Test
	public void testGetAndSetMinBlocksize() {
		Integer testMinBlocksize = random.nextInt(50) + 2;
		study.setMinBlocksize(testMinBlocksize);
		assertEquals(testMinBlocksize, study.getMinBlocksize(), "The min blocksize returned was not the one expected.");
	}

	@Test
	public void testGetAndSetMaxBlocksize() {
		Integer testMaxBlocksize = random.nextInt(150) + 2;
		study.setMaxBlocksize(testMaxBlocksize);
		assertEquals(testMaxBlocksize, study.getMaxBlocksize(), "The max blocksize returned was not the one expected.");
	}

	@Test
	public void testGetAndSetPseudonymHandling() {
		PseudonymHandling testPseudonymHandling = Helper.getRandomEnum(PseudonymHandling.class);
		study.setPseudonymHandling(testPseudonymHandling);
		assertEquals(testPseudonymHandling, study.getPseudonymHandling(),
		             "The pseudonym handling returned was not the one expected.");
	}

	@Test
	public void testGetAndSetActivationDate() {
		assertNull(study.getActivationDate(), "The initial activation date was not null.");
		Timestamp testActivationDate = new Timestamp(random.nextLong());
		study.setActivationDate(testActivationDate);
		assertEquals(testActivationDate, study.getActivationDate(),
		             "The activation date returned was not the one expected.");
	}

	@Test
	public void testGetSetAndRemoveStudyArms() {
		List<StudyArm> testStudyArms = new ArrayList<>();
		int counter = random.nextInt(10) + 2;
		for (int i = 0; i < counter; i++) {
			StudyArm testStudyArm = StudyArmTest.getValidStudyArm(false);
			testStudyArms.add(testStudyArm);
			study.addStudyArm(testStudyArm);
			assertTrue(study.getStudyArms().contains(testStudyArm), "The study arm was not added correctly.");
			assertEquals(study, testStudyArm.getStudy(), "The study was not set correctly to the study arm.");
		}
		assertTrue(study.getStudyArms().containsAll(testStudyArms),
		           "The study arms returned were not the ones expected.");
		for (StudyArm studyArm : testStudyArms) {
			study.removeStudyArm(studyArm);
			assertFalse(study.getStudyArms().contains(studyArm), "The study arm was not removed correctly.");
		}
		assertTrue(study.getStudyArms().isEmpty(), "After removing all added study arms the list was not empty.");
	}

	@Test
	public void testGetSetAndRemoveStratums() {
		List<Stratum> testStratums = new ArrayList<>();
		int counter = random.nextInt(10) + 2;
		for (int i = 0; i < counter; i++) {
			Stratum testStratum = StratumTest.getValidStratum(true);
			testStratums.add(testStratum);
			study.addStratum(testStratum);
			assertTrue(study.getStratums().contains(testStratum), "The stratum was not added correctly.");
			assertEquals(study, testStratum.getStudy(), "The study was not set correctly to the stratum.");
		}
		assertTrue(study.getStratums().containsAll(testStratums), "The stratum returned were not the ones expected.");
		for (Stratum stratum : testStratums) {
			study.removeStratum(stratum);
			assertFalse(study.getStratums().contains(stratum), "The stratum was not removed correctly.");
		}
		assertTrue(study.getStratums().isEmpty(), "After removing all added stratums the list was not empty.");
	}

	@Test
	public void testToSimpleStudyDTO() {
		study = getValidStudy(true);
		StudyDTO testStudyDTO = studyMapper.toStudyDTO(study);
		assertEquals(study.getId(), testStudyDTO.getId().longValue(),
		             "The id of the study DTO was not equal to the id of the study.");
		assertEquals(study.getGuiName(), testStudyDTO.getGuiName(),
		             "The gui name of the study DTO was not equal to the gui name of the study.");
		assertEquals(study.getDescription(), testStudyDTO.getDescription(),
		             "The description of the study DTO was not equal to the description of the study.");
		assertEquals(study.getRandomizationAlgorithm(), testStudyDTO.getRandomizationAlgorithm(),
		             "The randomization algorithm of the study DTO was not equal to the randomization algorithm of the study.");
		assertEquals(study.getMinBlocksize(), testStudyDTO.getMinBlocksize(),
		             "The minimal blocksize of the study DTO was not equal to the minimal blocksize of the study.");
		assertEquals(study.getMaxBlocksize(), testStudyDTO.getMaxBlocksize(),
		             "The maximal blocksize of the study DTO was not equal to the maximal blocksize of the study.");
		assertEquals(study.getPseudonymHandling(), testStudyDTO.getPseudonymHandling(),
		             "The pseudonym handling of the study DTO was not equal to the pseudonym handling of the study.");
		assertEquals(study.getActivationDate(), testStudyDTO.getActivationDate(),
		             "The activation date of the study DTO was not equal to the activation date of the study.");
		assertEquals(study.getStudyArms().size(), testStudyDTO.getStudyArms().size(),
		             "The number of study arms of the study DTO was not equal to the number of study arms of the study.");
		assertEquals(study.getStratums().size(),
		             testStudyDTO.getEnumeratedStratums().size() + testStudyDTO.getIntervalStratums().size(),
		             "The number of stratums of the study DTO was not equal to the number of stratums of the study.");
	}

	public static Study getValidStudy(boolean activated) {
		Study validStudy = new Study();

		validStudy.setGuiName(Helper.getRandomAlphanumericString(random.nextInt(50) + 3));
		validStudy.setDescription(Helper.getRandomAlphanumericString(random.nextInt(500) + 10));
		validStudy.setRandomizationAlgorithm(Helper.getRandomEnum(RandomizationAlgorithm.class));
		validStudy.setMinBlocksize(random.nextInt(50) + 2);
		validStudy.setMaxBlocksize(random.nextInt(150) + 2);

		Integer[] testBlocksizes = new Integer[random.nextInt(10) + 2];
		for (int i = 0; i < testBlocksizes.length; i++) {
			testBlocksizes[i] = random.nextInt(150) + 2;
		}

		validStudy.setPseudonymHandling(Helper.getRandomEnum(PseudonymHandling.class));
		if (activated) {
			validStudy.setActivationDate(new Timestamp(random.nextLong()));
		}

		int countStudyArms = random.nextInt(5) + 2;
		for (int i = 0; i < countStudyArms; i++) {
			StudyArm testStudyArm = StudyArmTest.getValidStudyArm(false);
			validStudy.addStudyArm(testStudyArm);
		}

		return new Study();
	}
}
