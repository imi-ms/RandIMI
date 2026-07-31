package de.unimuenster.imi.randimi.model.study.stratum;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.study.stratum.StratumPartBaseDTO;
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
public class StratumPartIntervalTest extends RandimiIntegrationTest {

	@Autowired
	de.unimuenster.imi.randimi.mapping.study.stratum.StratumPartMapper StratumPartMapper;

	private static final Random random = new Random();
	private StratumPartInterval stratumPart;

	public StratumPartIntervalTest() {
	}

	@BeforeEach
	public void beforeTest() {
		stratumPart = new StratumPartInterval();
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
	public void testGetAndSetIntervalBegin() {
		float testIntervalBegin = random.nextFloat();
		stratumPart.setIntervalBegin(testIntervalBegin);
		assertEquals(testIntervalBegin, stratumPart.getIntervalBegin(), 0,
		             "The interval begin returned was not the one expected.");
	}
	
	@Test
	public void testGetAndSetIntervalEnd() {
		float testIntervalEnd = random.nextFloat();
		stratumPart.setIntervalEnd(testIntervalEnd);
		assertEquals(testIntervalEnd, stratumPart.getIntervalEnd(), 0,
		             "The interval end returned was not the one expected.");
	}
	
	@Test
	public void testToStratumBaseDTO() {
		stratumPart = getValidStratumPartInterval(true);
		StratumPartBaseDTO testStratumPartIntervalDTO = StratumPartMapper.toStratumPartBaseDTO(stratumPart);
		assertEquals(stratumPart.getId(), testStratumPartIntervalDTO.getId(),
		             "The id of the stratum part interval DTO was not equal to the id of the stratum part interval.");
		assertEquals(stratumPart.getOrderNumber(), testStratumPartIntervalDTO.getOrderNumber(),
		             "The order number of the stratum part interval DTO was not equal to the order number of the stratum part interval.");
		assertEquals(stratumPart.getStratum().getId(), testStratumPartIntervalDTO.getStratumId(),
		             "The id of the stratum of the stratum part interval DTO was not equal to the id of the stratum of the stratum part interval.");
		assertEquals(stratumPart.getIntervalBegin(), testStratumPartIntervalDTO.getIntervalBegin(), 0,
		             "The interval begin of the stratum part interval DTO was not equal to the interval begin of the stratum part interval.");
		assertEquals(stratumPart.getIntervalEnd(), testStratumPartIntervalDTO.getIntervalEnd(), 0,
		             "The interval end of the stratum part interval DTO was not equal to the interval end of the stratum part interval.");
	}
	
	public static StratumPartInterval getValidStratumPartInterval(boolean withStratum) {
		StratumPartInterval validStratumPartInterval = new StratumPartInterval();

		validStratumPartInterval.setOrderNumber(random.nextInt());
		validStratumPartInterval.setIntervalBegin(random.nextFloat());
		validStratumPartInterval.setIntervalEnd(random.nextFloat());
		if (withStratum) {
			validStratumPartInterval.setStratum(StratumTest.getValidStratum(false));
		}

		return validStratumPartInterval;
	}
}
