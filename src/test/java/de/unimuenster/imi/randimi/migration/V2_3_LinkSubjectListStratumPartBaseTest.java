package de.unimuenster.imi.randimi.migration;

import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.stratum.Stratum;
import de.unimuenster.imi.randimi.model.study.stratum.StratumPartBase;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.study.StudyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class V2_3_LinkSubjectListStratumPartBaseTest extends RandimiMigrationTest {

	@Autowired
	StudyRepository studyRepository;

	@Test
	public void link() {
		final Study study = studyRepository.findByGuiName("Stratified By Site Study").get(0);

		Stratum enumStratum = study.getStratums().get(0);
		final Stratum siteStratum = study.getStratums().get(1);

		SubjectList firstList = study.getSubjectLists().get(0);
		assertEquals(2, firstList.getStratumParts().size(), "Number of linked stratum parts in not correct!");

		List<StratumPartBase> firstStratumParts = firstList.getStratumParts();

		StratumPartBase firstPart = firstStratumParts.get(0);
		assertEquals(enumStratum, firstPart.getStratum(), "Stratum of first part is not correct!");
		assertEquals(firstPart.getStratum().getStratumParts().get(0), firstPart, "Frist part is not correct!");

		StratumPartBase secondPart = firstStratumParts.get(1);
		assertEquals(siteStratum, secondPart.getStratum(), "Stratum of second part is not correct!");
		assertEquals(secondPart.getStratum().getStratumParts().get(0), secondPart, "Second part is not correct!");

		final SubjectList secondList = study.getSubjectLists().get(0);
		assertEquals(2, secondList.getStratumParts().size(), "Number of linked stratum parts in not correct!");
		assertEquals(enumStratum, secondList.getStratumParts().get(0).getStratum(), "Stratum of first part is not correct!");
		assertEquals(siteStratum, secondList.getStratumParts().get(1).getStratum(), "Stratum of second part is not correct!");

		// Active study
		final Study activeStudy = studyRepository.findByGuiName("Active Study").get(0);

		enumStratum = activeStudy.getStratums().get(0);
		final Stratum intevalStratum = activeStudy.getStratums().get(1);

		firstList = activeStudy.getSubjectLists().get(0);
		assertEquals(2, firstList.getStratumParts().size(), "Number of linked stratum parts in not correct!");

		firstStratumParts = firstList.getStratumParts();

		firstPart = firstStratumParts.get(0);
		assertEquals(enumStratum, firstPart.getStratum(), "Stratum of first part is not correct!");
		assertEquals(firstPart.getStratum().getStratumParts().get(0), firstPart, "Frist part is not correct!");

		secondPart = firstStratumParts.get(1);
		assertEquals(intevalStratum, secondPart.getStratum(), "Stratum of second part is not correct!");
		assertEquals(secondPart.getStratum().getStratumParts().get(0), secondPart, "Second part is not correct!");
	}

}
