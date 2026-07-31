package de.unimuenster.imi.randimi.model.subject;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.dto.subject.SubjectEntryDTO;
import de.unimuenster.imi.randimi.mapping.subject.SubjectEntryMapper;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.Helper;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.study.StudyArmTest;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class SubjectTest extends RandimiIntegrationTest {

	private static final Random random = new Random();
	private Subject subject;

	@Autowired
	SubjectEntryMapper mapper;

	public SubjectTest() {
	}

	@BeforeEach
	public void beforeTest() {
		subject = new Subject();
	}

	@Test
	public void testGetAndSetRandomizationList() {
		SubjectList testRandomizationList = SubjectListTest.getValidRandomizationList(false, false);
		subject.setSubjectList(testRandomizationList);
		assertEquals(testRandomizationList, subject.getSubjectList(),
		             "The randomization list returned was not the one expected.");
		assertTrue(testRandomizationList.getSubjects().contains(subject),
		           "The randomization entry was not added to the randomization list.");
	}
	
	@Test
	public void testGetAndSetOrderNumber() {
		int testOrderNumber = random.nextInt();
		subject.setOrderNumber(testOrderNumber);
		assertEquals(testOrderNumber, subject.getOrderNumber(), "The order number returned was not the one expected.");
	}
	
	@Test
	public void testGetAndSetStudyArm() {
		StudyArm testStudyArm = StudyArmTest.getValidStudyArm(true);
		subject.setStudyArm(testStudyArm);
		assertEquals(testStudyArm, subject.getStudyArm(),"The study arm returned was not the one expected.");
	}
	
	@Test
	public void testGetAndSetAssignedTo() {
		Site site = new Site();
		site.setGuiName(Helper.getRandomAlphabeticString(random.nextInt(20) + 3));
		String testPseudonym = Helper.getRandomAlphabeticString(random.nextInt(20) + 3);
		subject.setAssignedTo(site, testPseudonym);
		assertEquals(site.getGuiName(), subject.getSite().getGuiName(),
		             "The assigned location returned was not the one expected.");
		assertEquals(testPseudonym, subject.getPseudonym(),
		             "The assigned pseudonym returned was not the one expected.");
	}
	
	@Test
	public void testGetAndSetPseudonym() {
		String testPseudonym = Helper.getRandomAlphabeticString(random.nextInt(20) + 3);
		subject.setPseudonym(testPseudonym);
		assertEquals(testPseudonym, subject.getPseudonym(),
		             "The assigned pseudonym returned was not the one expected.");
	}
	
	@Test
	public void testGetAndSetSite() {
		Site site = new Site();
		site.setGuiName(Helper.getRandomAlphabeticString(random.nextInt(20) + 3));
		subject.setSite(site);
		assertEquals(site.getGuiName(), subject.getSite().getGuiName(),
		             "The assigned location returned was not the one expected.");
	}
	
	@Test
	public void testToRandomizationEntryDTO() {
		subject = getValidRandomizationEntry(true);
		SubjectEntryDTO testSubjectEntryDTO = mapper.toSubjectEntryDTO(subject);
		assertEquals(subject.getId(), testSubjectEntryDTO.getId(),
		             "The id of the randomization entry DTO was not equal to the id of the randomization entry.");
		assertEquals(subject.getOrderNumber(), testSubjectEntryDTO.getOrderNumber(),
		             "The order number of the randomization entry DTO was not equal to the order number of the randomization entry.");
		assertEquals(subject.getSubjectList().getId(), testSubjectEntryDTO.getRandomizationListId(),
		             "The id of the randomization list of the randomization entry DTO was not equal to the id of the randomization list of the randomization entry.");
		assertEquals(subject.getStudyArm().getGuiName(), testSubjectEntryDTO.getStudyArmName(),
		             "The name of the study arm of the randomization entry DTO was not equal to the name of the study arm of the randomization entry.");
		assertEquals(subject.getPseudonym(), testSubjectEntryDTO.getPseudonym(),
		             "The pseudonym of the randomization entry DTO was not equal to the pseudonym of the randomization entry.");
		assertEquals(subject.getSite().getGuiName(), testSubjectEntryDTO.getLocation(),
		             "The location of the randomization entry DTO was not equal to the location of the randomization entry.");
		assertEquals(subject.getSite().getId(), testSubjectEntryDTO.getSiteId().longValue(),
		             "The siteId of the randomization entry DTO was not equal to the siteId of the randomization entry.");
	}
	
	public static Subject getValidRandomizationEntry(boolean withRandomizationList) {
		Subject validRandomizationEntry = new Subject();
		Site site = new Site();
		site.setId(42);
		site.setGuiName(Helper.getRandomAlphabeticString(random.nextInt(20) + 3));

		validRandomizationEntry.setId(random.nextLong());
		validRandomizationEntry.setOrderNumber(random.nextInt());
		validRandomizationEntry.setStudyArm(StudyArmTest.getValidStudyArm(true));
		validRandomizationEntry.setAssignedTo(site, Helper.getRandomAlphabeticString(random.nextInt(20) + 3));

		if(withRandomizationList) {
			validRandomizationEntry.setSubjectList(SubjectListTest.getValidRandomizationList(false, false));
		}
		
		return validRandomizationEntry;
	}
}
