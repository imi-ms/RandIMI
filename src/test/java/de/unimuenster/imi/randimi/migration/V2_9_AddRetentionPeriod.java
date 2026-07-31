package de.unimuenster.imi.randimi.migration;

import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNull;

public class V2_9_AddRetentionPeriod extends RandimiMigrationTest {

	@Autowired
	StudyRepository studyRepository;

	@Test
	public void siteOrderNumber() {
		final Study activeStudy = studyRepository.findByGuiName("Active Study").get(0);
		assertNull(activeStudy.getRetentionPeriod(), "Retention Period should be null!");
	}
}
