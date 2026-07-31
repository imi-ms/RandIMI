package de.unimuenster.imi.randimi.migration;

import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class V2_6_AddStudyStatusTest extends RandimiMigrationTest {

	@Autowired
	StudyRepository studyRepository;

	@Test
	public void studyStatus() {
		final Study activeStudy = studyRepository.findByGuiName("Active Study").get(0);
		final Study inactiveStudy = studyRepository.findByGuiName("Inactive Study").get(0);

		assertEquals(StudyStatus.ACTIVE, activeStudy.getStatus(), "Unexpected status of active study!");
		assertEquals(StudyStatus.CREATED, inactiveStudy.getStatus(), "Unexpected status of inactive study!");
	}

}
