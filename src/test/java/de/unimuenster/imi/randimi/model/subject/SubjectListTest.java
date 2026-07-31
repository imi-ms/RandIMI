package de.unimuenster.imi.randimi.model.subject;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.subject.SubjectListDTO;
import de.unimuenster.imi.randimi.mapping.subject.SubjectListMapper;
import de.unimuenster.imi.randimi.model.study.StudyTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.unimuenster.imi.randimi.model.study.stratum.StratumPartEnumerationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class SubjectListTest extends RandimiIntegrationTest {

	@Autowired
	private SubjectListMapper mapper;

	private static final Random random = new Random();
	private SubjectList subjectList;

	public SubjectListTest() {
	}

	@BeforeEach
	public void beforeTest() {
		subjectList = new SubjectList();
	}

	@Test
	public void testGetAndSetRemainingAssignments() {
		Integer[] testRemainigAssignments = {1, 1};
		subjectList.setRemainingAssignments(testRemainigAssignments);
		assertArrayEquals(testRemainigAssignments, subjectList.getRemainingAssignments(),
		                  "The remaining assignments returned were not the one expected.");
	}

	@Test
	public void testGetSetAndRemoveRandomizationEntries() {
		List<Subject> testEntries = new ArrayList<>();
		int counter = random.nextInt(10) + 2;
		for (int i = 0; i < counter; i++) {
			Subject testEntry = SubjectTest.getValidRandomizationEntry(false);
			testEntries.add(testEntry);
			subjectList.addSubject(testEntry);
			assertTrue(subjectList.getSubjects().contains(testEntry),
			           "The randomization entry was not added correctly.");
			assertEquals(subjectList, testEntry.getSubjectList(),
			             "The randomization list was not set correctly to the entry.");
		}
		assertTrue(subjectList.getSubjects().containsAll(testEntries),
		           "The randomization entries returned were not the ones expected.");
		for (Subject entry : testEntries) {
			subjectList.removeSubject(entry);
			assertFalse(subjectList.getSubjects().contains(entry),
			            "The randomization entry was not removed correctly.");
		}
		assertTrue(subjectList.getSubjects().isEmpty(),
		           "After removing all added randomization entries the list was not empty.");
	}

	@Test
	public void testToRandomizationListDTO() {
		subjectList = getValidRandomizationList(true, true);
		SubjectListDTO testRandomizationListDTO = mapper.toSubjectListDTO(subjectList);
		assertEquals(subjectList.getId(), testRandomizationListDTO.getId(),
		             "The id of the randomization list DTO was not equal to the id of the randomization list.");
		assertEquals(subjectList.getStudy().getId(), testRandomizationListDTO.getStudyId(),
		             "The id of the study of the randomization list DTO was not equal to the id of the study of the randomization list.");
		assertEquals(subjectList.getStratumParts().size(), testRandomizationListDTO.getStratumParts().size(),
		             "The number of stratum parts of the DTO was not equal to the number stratum parts of the entity.");
		assertEquals(subjectList.getSubjects().size(), testRandomizationListDTO.getSubjectEntries().size(),
		             "The number of randomization entries of the randomization list DTO was not equal to the number of randomization entries of the randomization list.");
		
	}

	public static SubjectList getValidRandomizationList(boolean withStudy, boolean withEntries) {
		SubjectList validRandomizationList = new SubjectList();
		
		validRandomizationList.setId(random.nextLong());
		validRandomizationList.addStratumPart(StratumPartEnumerationTest.getValidStratumPartEnumeration(true));

		if (withStudy) {
			validRandomizationList.setStudy(StudyTest.getValidStudy(false));
		}
		if (withEntries) {
			int counter = random.nextInt(30) + 1;
			for (int i = 0; i < counter; i++) {
				validRandomizationList.addSubject(SubjectTest.getValidRandomizationEntry(false));
			}
		}

		return validRandomizationList;
	}
}
