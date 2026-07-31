package de.unimuenster.imi.randimi.mapping.study.stratum;

import de.unimuenster.imi.randimi.mapping.study.NamesMapper;
import de.unimuenster.imi.randimi.mapping.study.SiteMapper;
import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartEnumeration;
import de.unimuenster.imi.randimi.validator.study.stratum.StratumDtoValidatorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class StratumPartMapperTest {

	private StratumPartMapper mapper;

	@BeforeEach
	public void setUp() {
		this.mapper = new StratumPartMapper(new SiteMapper(new NamesMapper()));
	}

	@Test
	public void toStratumPartBase() {
		var name = "Old";
		var orderNumber = 1;

		var dto = StratumDtoValidatorTest.getValidEnumPartDto(name);
		var stratum = new Stratum();
		stratum.setStratumType(StratumType.ENUM);

		var a = mapper.toStratumPartBase(dto, stratum, orderNumber);

		assertInstanceOf(StratumPartEnumeration.class, a, "Wrong stratum part instance!");
		var enumPart = (StratumPartEnumeration) a;

		assertEquals(dto.getGuiName(), enumPart.getName());
		assertEquals(dto.getApiId(), enumPart.getApiId());
		assertEquals(!dto.getUseApiId(), enumPart.isSynchronizeApiId());
		assertEquals(orderNumber, enumPart.getOrderNumber());
		assertEquals(dto.getEnumValue(), enumPart.getEnumValue());
	}

}
