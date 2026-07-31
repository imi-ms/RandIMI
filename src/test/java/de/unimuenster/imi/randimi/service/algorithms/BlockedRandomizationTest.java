package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandomService;
import de.unimuenster.imi.randimi.service.StratumCodeService;
import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.repository.subject.SubjectRepository;
import de.unimuenster.imi.randimi.service.StudyUtilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlockedRandomizationTest extends RandimiIntegrationTest {

	@Autowired
	private MessageService messageService;
	@Autowired
	private RandomService randomService;
	@Autowired
	private StratumCodeService stratumCodeService;
	@Autowired
	private StudyUtilityService studyUtilityService;

	private BlockedRandomization blockedRandomization;

	private Study study;
	private SubjectList subjectList;
	private Site site;

	@BeforeEach
	public void setUp() {
		subjectList = new SubjectList();
		subjectList.setId(1L);
		subjectList.setRemainingAssignments(new Integer[]{0, 0});

		site = new Site();
		site.setSeed(123L);

		study = new Study();
		study.setStatus(StudyStatus.ACTIVE);
		study.addSubjectList(subjectList);
		study.addSite(site);
		final StudyArm studyArmA = new StudyArm();
		studyArmA.setId(1L);
		studyArmA.setRatio(1);
		final StudyArm studyArmB = new StudyArm();
		studyArmB.setId(2L);
		studyArmB.setRatio(1);
		study.addStudyArm(studyArmA);
		study.addStudyArm(studyArmB);
		study.setCapacity(24);
		study.setMinBlocksize(2);
		study.setMaxBlocksize(12);

		final SubjectRepository subjectRepository = mock(SubjectRepository.class);
		when(subjectRepository.countBlockingSubjectInSubjectList(subjectList.getId())).thenReturn(12L);

		blockedRandomization = new BlockedRandomization(messageService, stratumCodeService, subjectRepository, randomService,
		                                                studyUtilityService);
	}

	@Test
	public void getPossibleBlockSizesNoStratificationTest() {
		List<Integer> possibleBlockSizes = blockedRandomization.getPossibleBlockSizes(site, subjectList);
		List<Integer> expectedPossibleBlockSizes = Arrays.asList(2, 4, 6, 8, 10, 12);
		assertEquals(expectedPossibleBlockSizes, possibleBlockSizes, "Unexpected possible block sizes for no stratification!");

		study.setCapacity(18);
		possibleBlockSizes = blockedRandomization.getPossibleBlockSizes(site, subjectList);
		expectedPossibleBlockSizes = Arrays.asList(2, 4, 6);
		assertEquals(expectedPossibleBlockSizes, possibleBlockSizes, "Unexpected possible block sizes for no stratification!");
	}

	@Test
	public void getPossibleBlockSizesNoStratificationRatioTest() {
		subjectList.getStudy().getStudyArms().get(0).setRatio(3);
		List<Integer> possibleBlockSizes = blockedRandomization.getPossibleBlockSizes(site, subjectList);
		List<Integer> expectedPossibleBlockSizes = Arrays.asList(4, 8, 12);
		assertEquals(expectedPossibleBlockSizes, possibleBlockSizes, "Unexpected possible block sizes for no stratification!");
	}

	@Test
	public void getRandomStudyArmNoStratificationTest() {
		StudyArm studyArm = blockedRandomization.getRandomStudyArm(site, subjectList);
		Integer[] expectedRemainingAssignments = new Integer[]{2, 3};
		assertArrayEquals(expectedRemainingAssignments, subjectList.getRemainingAssignments(), "Unexpected remaining assignments after the first allocation!");
		assertEquals(1L, studyArm.getId(), "Unexpected study arm for the first allocation!");

		studyArm = blockedRandomization.getRandomStudyArm(site, subjectList);
		expectedRemainingAssignments = new Integer[]{2, 2};
		assertArrayEquals(expectedRemainingAssignments, subjectList.getRemainingAssignments(), "Unexpected remaining assignments after the second allocation!");
		assertEquals(2L, studyArm.getId(), "Unexpected study arm for the second allocation!");
	}

	@Test
	public void getRandomStudyArmNoStratificationRatioTest() {
		subjectList.getStudy().getStudyArms().get(0).setRatio(3);

		StudyArm studyArm = blockedRandomization.getRandomStudyArm(site, subjectList);
		Integer[] expectedRemainingAssignments = new Integer[]{8, 3};
		assertArrayEquals(expectedRemainingAssignments, subjectList.getRemainingAssignments(), "Unexpected remaining assignments after the first allocation!");
		assertEquals(1L, studyArm.getId(), "Unexpected study arm for the first allocation!");

		studyArm = blockedRandomization.getRandomStudyArm(site, subjectList);
		expectedRemainingAssignments = new Integer[]{8, 2};
		assertArrayEquals(expectedRemainingAssignments, subjectList.getRemainingAssignments(), "Unexpected remaining assignments after the second allocation!");
		assertEquals(2L, studyArm.getId(), "Unexpected study arm for the second allocation!");
	}
}
