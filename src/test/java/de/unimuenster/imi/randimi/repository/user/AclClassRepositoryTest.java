package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Daniel Preciado-Marquez
 */
public class AclClassRepositoryTest extends RepositoryTestBase {

	@Test
	public void addAclClass() {
		AclClass newObject = new AclClass("foo");
		aclClassRepository.save(newObject);
		assertEquals(3, aclClassRepository.count());
	}

	@Test
	public void findFirstByClassNameOrSynonymTest() {
		// find nothing
		AclClass foundNull = aclClassRepository.findFirstByClassNameOrSynonym(null, null);
		assertNull(foundNull);

		// find by class
		AclClass foundByClass = aclClassRepository.findFirstByClassNameOrSynonym("de.unimuenster.imi.randimi.model.study.Study", null);
		assertEquals("de.unimuenster.imi.randimi.model.study.Study", foundByClass.getClassName());
		assertEquals("de.unimuenster.imi.randimi.dto.study.StudyDTO", foundByClass.getSynonym());

		// find by Synonym
		AclClass foundBySynonym = aclClassRepository.findFirstByClassNameOrSynonym(null, "de.unimuenster.imi.randimi.dto.study.SiteDTO");
		assertEquals("de.unimuenster.imi.randimi.model.study.Site", foundBySynonym.getClassName());
		assertEquals("de.unimuenster.imi.randimi.dto.study.SiteDTO", foundBySynonym.getSynonym());
	}

}
