package de.unimuenster.imi.randimi.model.study.stratum;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumDTO;
import de.unimuenster.imi.randimi.mapping.study.stratum.StratumMapper;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.Helper;
import de.unimuenster.imi.randimi.model.study.StudyTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class StratumTest extends RandimiIntegrationTest {

	@Autowired
	StratumMapper stratumMapper;

	private static final Random random = new Random();
	private Stratum stratum;

	public StratumTest() {
	}

	@BeforeEach
	public void beforeTest() {
		stratum = new Stratum();
	}

	@Test
	public void testGetAndSetStratumType() {
		StratumType testStratumType = Helper.getRandomEnum(StratumType.class);
		stratum.setStratumType(testStratumType);
		assertEquals(testStratumType, stratum.getStratumType(), "The stratum type returned was not the one expected.");
	}

	@Test
	public void testGetAndSetName() {
		String testName = Helper.getRandomAlphanumericString(random.nextInt(50) + 3);
		stratum.setName(testName);
		assertEquals(testName, stratum.getName(), "The name returned was not the one expected.");
	}

	@Test
	public void testGetAndSetStudy() {
		Study testStudy = StudyTest.getValidStudy(false);
		stratum.setStudy(testStudy);
		assertEquals(testStudy, stratum.getStudy(), "The study returned was not the one expected.");
		assertTrue(testStudy.getStratums().contains(stratum), "The stratum was not added to the study.");
	}

	@Test
	public void testGetAndSetOrderNumber() {
		int testOrderNumber = random.nextInt();
		stratum.setOrderNumber(testOrderNumber);
		assertEquals(testOrderNumber, stratum.getOrderNumber(), "The order number returned was not the one expected.");
	}

	@Test
	public void testGetSetAndRemoveStratumParts() {
		stratum.setStratumType(Helper.getRandomEnum(StratumType.class));
		int counter = random.nextInt(7) + 2;
		switch (stratum.getStratumType()) {
			case ENUM:
				List<StratumPartEnumeration> testStratumPartEnumerations = new ArrayList<>();
				for (int i = 0; i < counter; i++) {
					StratumPartEnumeration testStratumPartEnumeration = StratumPartEnumerationTest.getValidStratumPartEnumeration(false);
					testStratumPartEnumerations.add(testStratumPartEnumeration);
					stratum.addStratumPart(testStratumPartEnumeration);
					assertTrue(stratum.getStratumParts().contains(testStratumPartEnumeration),
					           "The stratum part was not added correctly.");
					assertEquals(stratum, testStratumPartEnumeration.getStratum(),
					             "The stratum was not set correctly to the stratum part.");
				}
				assertTrue(stratum.getStratumParts().containsAll(testStratumPartEnumerations),
				           "The stratum parts returned were not the ones expected.");
				for (StratumPartEnumeration stratumPartEnumeration : testStratumPartEnumerations) {
					stratum.removeStratumPart(stratumPartEnumeration);
					assertFalse(stratum.getStratumParts().contains(stratumPartEnumeration),
					            "The stratum part was not removed correctly.");
				}
				assertTrue(stratum.getStratumParts().isEmpty(),
				           "After removing all added stratum parts the list was not empty.");
				break;
			case INTERVAL:
				List<StratumPartInterval> testStratumPartIntervals = new ArrayList<>();
				for (int i = 0; i < counter; i++) {
					StratumPartInterval testStratumPartInterval = StratumPartIntervalTest.getValidStratumPartInterval(false);
					testStratumPartIntervals.add(testStratumPartInterval);
					stratum.addStratumPart(testStratumPartInterval);
					assertTrue(stratum.getStratumParts().contains(testStratumPartInterval),
					           "The stratum part was not added correctly.");
					assertEquals(stratum, testStratumPartInterval.getStratum(),
					             "The stratum was not set correctly to the stratum part.");
				}
				assertTrue(stratum.getStratumParts().containsAll(testStratumPartIntervals),
				           "The stratum parts returned were not the ones expected.");
				for (StratumPartInterval stratumPartInterval : testStratumPartIntervals) {
					stratum.removeStratumPart(stratumPartInterval);
					assertFalse(stratum.getStratumParts().contains(stratumPartInterval),
					            "The stratum part was not removed correctly.");
				}
				assertTrue(stratum.getStratumParts().isEmpty(),
				           "After removing all added stratum parts the list was not empty.");
				break;
		}
	}

	@Test
	public void testToStratumDTO() {
		stratum = getValidStratum(true);
		StratumDTO testStratumDTO = stratumMapper.toStratumDTO(stratum);
		assertEquals(stratum.getId(), testStratumDTO.getId(),
		             "The id of the stratum DTO was not equal to the id of the stratum.");
		assertEquals(stratum.getStudy().getId(), testStratumDTO.getStudyId(),
		             "The study id of the stratum DTO was not equal to the id of the study.");
		assertEquals(stratum.getOrderNumber(), testStratumDTO.getOrderNumber(),
		             "The order number of the stratum DTO was not equal to the order number of the stratum.");
		assertEquals(stratum.getName(), testStratumDTO.getGuiName(),
		             "The name of the stratum DTO was not equal to the name of the stratum.");
		assertEquals(stratum.getStratumType(), testStratumDTO.getStratumType(),
		             "The stratum type of the stratum DTO was not equal to the stratum type of the stratum.");
		assertEquals(stratum.getStratumParts().size(), testStratumDTO.getStratumParts().size(),
		             "The number of stratum parts of the stratum DTO was not equal to the number of stratum parts of the stratum.");
	}

	public static Stratum getValidStratum(boolean withStratumParts) {
		Stratum validStratum = new Stratum();

		validStratum.setStratumType(Helper.getRandomEnum(StratumType.class));
		validStratum.setName(Helper.getRandomAlphanumericString(random.nextInt(50) + 3));
		validStratum.setStudy(StudyTest.getValidStudy(false));
		validStratum.setOrderNumber(random.nextInt());

		if (withStratumParts) {
			int partCounter = random.nextInt(5) + 2;
			switch (validStratum.getStratumType()) {
			case ENUM:
				for (int i = 0; i < partCounter; i++) {
					validStratum.addStratumPart(StratumPartEnumerationTest.getValidStratumPartEnumeration(false));
				}
				break;
			case INTERVAL:
				for (int i = 0; i < partCounter; i++) {
					validStratum.addStratumPart(StratumPartIntervalTest.getValidStratumPartInterval(false));
				}
				break;
			}
		}

		return validStratum;
	}
}
