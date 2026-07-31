package de.unimuenster.imi.randimi.migration;

import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class V2_2_AddSiteOrderNumberTest extends RandimiMigrationTest {

	@Autowired
	StudyRepository studyRepository;

	@Test
	public void siteOrderNumber() {
		final Study activeStudy = studyRepository.findByGuiName("Active Study").get(0);

		final Site site0 = activeStudy.getSites().get(0);
		assertEquals(0, site0.getOrderNumber(), "Order number of the first site does not match!");

		final Site site1 = activeStudy.getSites().get(1);
		assertEquals(1, site1.getOrderNumber(), "Order number of the second site does not match!");

		final Study inactiveStudy = studyRepository.findByGuiName("Inactive Study").get(0);
		assertEquals(0, inactiveStudy.getSites().get(0).getOrderNumber(),
		             "Order number of the second site in the inactive study does not match!");
	}

}
