package de.unimuenster.imi.randimi.repository.study;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.unimuenster.imi.randimi.model.study.Site;

/**
 * @author Daniel Preciado-Marquez
 */
public class SiteRepositoryTest extends RepositoryTestBase {

	@Autowired
	SiteRepository siteRepository;

	@Autowired
	StudyRepository studyRepository;

	@Test
	public void findByStudyTest() {
		List<Site> sites = siteRepository.findByStudy(activeStudy);

		assertEquals(2, sites.size(), "Number of found sites incorrect!");
		assertEquals("First Site of active study", sites.get(0).getGuiName(), "Name of first study incorrect!");
		assertEquals("Second Site of active study", sites.get(1).getGuiName(), "Name of second study incorrect!");
	}

	@Test
	public void findByStudyTestId() {
		List<Site> sites = siteRepository.findByStudyId(activeStudy.getId());

		assertEquals(2, sites.size(), "Number of found sites incorrect!");
		assertEquals("First Site of active study", sites.get(0).getGuiName(), "Name of first study incorrect!");
		assertEquals("Second Site of active study", sites.get(1).getGuiName(), "Name of second study incorrect!");
	}

}
