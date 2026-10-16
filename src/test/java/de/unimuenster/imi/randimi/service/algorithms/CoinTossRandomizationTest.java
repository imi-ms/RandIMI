package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.model.enumeration.StudyStatus;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.SubjectList;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandomService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
public class CoinTossRandomizationTest extends RandimiIntegrationTest {

	@Autowired
	private CoinTossRandomization coinTossRandomization;

	@Autowired
	private CoinTossRandomizationV1 coinTossRandomizationV1;

	@Autowired
	private MessageService messageService;

	private Site site;

	public CoinTossRandomizationTest() {
	}

	@BeforeEach
	public void beforeTest() {
		Study study = new Study();

		StudyArm arm1 = new StudyArm();
		arm1.setGuiName("Arm 1");
		arm1.setOrderNumber(12);
		arm1.setRatio(1);
		study.addStudyArm(arm1);

		StudyArm arm2 = new StudyArm();
		arm2.setGuiName("Arm 2");
		arm2.setOrderNumber(13);
		arm2.setRatio(1);
		study.addStudyArm(arm2);

		site = new Site();
		site.setSeed(123L);
		site.setRandomCalls(0);

		study.addSite(site);
	}

	@Test
	public void getRandomStudyArmTest() {
		StudyArm studyArm = coinTossRandomization.getRandomStudyArm(site, new SubjectList());
		assertEquals(site.getStudy().getStudyArms().get(1), studyArm, "Returned unexpected StudyArm");
	}

	@Test
	public void drawStudyArmIndexRatioTest() {
		site.getStudy().getStudyArms().get(1).setRatio(2);
		final SubjectList subjectList = new SubjectList();
		final RandomService randomService = mock(RandomService.class);
		final CoinTossRandomization randomization = new CoinTossRandomization(messageService, randomService);

		// the drawn number in [0, 3) covers one slot of the first study arm and two slots of the second one
		final int[] expectedStudyArmIndices = {0, 1, 1};
		for (int drawn = 0; drawn < expectedStudyArmIndices.length; drawn++) {
			when(randomService.nextRandomInt(any(), any(), eq(3))).thenReturn(drawn);
			assertEquals(expectedStudyArmIndices[drawn], randomization.drawStudyArmIndex(site, subjectList),
			             "Unexpected study arm for the drawn number " + drawn + "!");
		}
	}

	@Test
	public void getRandomStudyArmRatioTest() {
		assertEquals(2.0 / 3, shareOfSecondStudyArm(coinTossRandomization), 0.03,
		             "The share of the second study arm should follow the ratio 1:2!");
	}

	@Test
	public void getRandomStudyArmRatioV1Test() {
		assertEquals(1.0 / 2, shareOfSecondStudyArm(coinTossRandomizationV1), 0.03,
		             "Version 1 should ignore the ratio 1:2!");
	}

	private double shareOfSecondStudyArm(final CoinTossRandomization randomization) {
		site.getStudy().getStudyArms().get(1).setRatio(2);
		// only active studies draw with the seed of the site
		site.getStudy().setStatus(StudyStatus.ACTIVE);
		final StudyArm secondStudyArm = site.getStudy().getStudyArms().get(1);
		final SubjectList subjectList = new SubjectList();

		final int draws = 3000;
		int hits = 0;
		for (int i = 0; i < draws; i++)
			if (randomization.getRandomStudyArm(site, subjectList) == secondStudyArm)
				hits++;
		return (double) hits / draws;
	}

}
