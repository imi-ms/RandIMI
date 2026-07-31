package de.unimuenster.imi.randimi.model.study.stratum;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
import de.unimuenster.imi.randimi.mapping.study.stratum.StratumPartMapper;
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
public class StratumPartEnumerationTest extends RandimiIntegrationTest {

	@Autowired
	StratumPartMapper stratumPartMapper;

	private static final Random random = new Random();
	private StratumPartEnumeration stratumPart;

	public StratumPartEnumerationTest() {
	}

	@BeforeEach
	public void beforeTest() {
		stratumPart = new StratumPartEnumeration();
	}

	@Test
	public void testGetAndSetStratum() {
		Stratum testStratum = StratumTest.getValidStratum(false);
		stratumPart.setStratum(testStratum);
		assertEquals(testStratum, stratumPart.getStratum(), "The stratum returned was not the one expected.");
		assertTrue(testStratum.getStratumParts().contains(stratumPart),
		           "The stratum part was not added to the stratum.");
	}

	@Test
	public void testGetAndSetOrderNumber() {
		int testOrderNumber = random.nextInt();
		stratumPart.setOrderNumber(testOrderNumber);
		assertEquals(testOrderNumber, stratumPart.getOrderNumber(),
		             "The order number returned was not the one expected.");
	}

	@Test
	public void testToStratumBaseDTO() {
		stratumPart = getValidStratumPartEnumeration(true);
		StratumPartBaseDTO testStratumPartEnumerationDTO = stratumPartMapper.toStratumPartBaseDTO(stratumPart);
		assertEquals(stratumPart.getId(), testStratumPartEnumerationDTO.getId(),
		             "The id of the stratum part enumeration DTO was not equal to the id of the stratum part enumeration.");
		assertEquals(stratumPart.getOrderNumber(), testStratumPartEnumerationDTO.getOrderNumber(),
		             "The order number of the stratum part enumeration DTO was not equal to the order number of the stratum part enumeration.");
		assertEquals(stratumPart.getStratum().getId(), testStratumPartEnumerationDTO.getStratumId(),
		               "The id of the stratum of the stratum part enumeration DTO was not equal to the id of the stratum of the stratum part enumeration.");
		assertEquals(testStratumPartEnumerationDTO.getGuiName(), stratumPart.getEnumValue(),
		             "The id of the stratum part enumeration DTO was not equal to the id of the stratum part enumeration.");
	}

	public static StratumPartEnumeration getValidStratumPartEnumeration(boolean withStratum) {
		StratumPartEnumeration validStratumPartEnumeration = new StratumPartEnumeration();

		validStratumPartEnumeration.setOrderNumber(random.nextInt());
		validStratumPartEnumeration.setEnumValue(Helper.getRandomAlphanumericString(random.nextInt(50) + 3));
		if (withStratum) {
			validStratumPartEnumeration.setStratum(StratumTest.getValidStratum(false));
		}

		return validStratumPartEnumeration;
	}
}
