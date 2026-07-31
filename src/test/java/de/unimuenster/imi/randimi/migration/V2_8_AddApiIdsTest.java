package de.unimuenster.imi.randimi.migration;

import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartEnumeration;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class V2_8_AddApiIdsTest extends RandimiMigrationTest {

	@Autowired
	StudyRepository studyRepository;

	@Override
	@BeforeEach
	public void setUp() {
//		targetVersion = "2.8";
		super.setUp();
	}

	@Test
	public void apiIds() {
		final Study activeStudy = studyRepository.findByGuiName("Active Study").get(0);
		assertFalse(activeStudy.isSynchronizeApiId(), "Unexpected value of synchronizeApiId");
		assertEquals(Long.toString(activeStudy.getId()), activeStudy.getApiId(), "Unexpected value of apiId");

		final Site site = activeStudy.getSites().get(0);
		assertFalse(site.isSynchronizeApiId(), "Unexpected value of synchronizeApiId");

		final StudyArm studyArm = activeStudy.getStudyArms().get(0);
		assertTrue(studyArm.isSynchronizeApiId(), "Unexpected value of synchronizeApiId");
		assertEquals(studyArm.getGuiName(), studyArm.getApiId(), "Unexpected value of apiId");

		final Stratum stratum = activeStudy.getStratums().get(0);
		assertTrue(stratum.isSynchronizeApiId(), "Unexpected value of synchronizeApiId");
		assertEquals(stratum.getName(), stratum.getApiId(), "Unexpected value of apiId");

		final StratumPartEnumeration stratumPartEnum = (StratumPartEnumeration) stratum.getStratumParts().get(0);
		assertTrue(stratumPartEnum.isSynchronizeApiId(), "Unexpected value of synchronizeApiId");
		assertEquals(stratumPartEnum.getEnumValue(), stratumPartEnum.getApiId(), "Unexpected value of apiId");
	}

}
