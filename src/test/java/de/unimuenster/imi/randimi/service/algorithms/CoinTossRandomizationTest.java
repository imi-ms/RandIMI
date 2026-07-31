package de.unimuenster.imi.randimi.service.algorithms;

import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.study.StudyArm;
import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.model.study.Site;
import de.unimuenster.imi.randimi.model.subject.SubjectList;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

	private Site site;

	public CoinTossRandomizationTest() {
	}

	@BeforeEach
	public void beforeTest() {
		Study study = new Study();

		StudyArm arm1 = new StudyArm();
		arm1.setGuiName("Arm 1");
		arm1.setOrderNumber(12);
		study.addStudyArm(arm1);

		StudyArm arm2 = new StudyArm();
		arm2.setGuiName("Arm 2");
		arm2.setOrderNumber(13);
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

}
