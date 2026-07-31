package de.unimuenster.imi.randimi.migration;

import de.unimuenster.imi.randimi.model.enumeration.StratumType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartSite;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class V2_4_AddStratumPartSiteTest extends RandimiMigrationTest {

	@Autowired
	StudyRepository studyRepository;

	@Test
	public void stratumPartSite() {
		final Study study = studyRepository.findByGuiName("Stratified By Site Study").get(0);

		// Check if location stratum has been updated
		final Stratum siteStratum =  study.getStratums().get(1);
		assertEquals(StratumType.SITE, siteStratum.getStratumType(), "Stratum type is not set correctly!");

		final StratumPartBase firstStratumPart = siteStratum.getStratumParts().get(0);
		assertInstanceOf(StratumPartSite.class, siteStratum.getStratumParts().get(0),
		                 "Stratum Part is not of the correct instance!");

		final StratumPartSite firstSiteStratumPart = (StratumPartSite) firstStratumPart;
		assertNotNull(firstSiteStratumPart.getSite(), "Site has not been set!");

		// Check if enumerated stratum is still the same
		final Stratum enumStratum =  study.getStratums().get(0);
		assertEquals(StratumType.ENUM, enumStratum.getStratumType(), "Stratum type should not have changed!");
	}
}
