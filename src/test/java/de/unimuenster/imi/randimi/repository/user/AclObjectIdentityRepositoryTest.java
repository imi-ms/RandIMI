package de.unimuenster.imi.randimi.repository.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Daniel Preciado-Marquez
 */
public class AclObjectIdentityRepositoryTest extends RepositoryTestBase {

	@Autowired
	AclObjectIdentityRepository aclObjectIdentityRepository;

	@Test
	public void findFirstByObjectIdClassAndObjectIdIdentityTest() {
		AclObjectIdentity aclObjectIdentityStudy = aclObjectIdentityRepository
				.findFirstByObjectIdClassAndObjectIdIdentity(aclClassStudy, activeStudy.getId());
		assertNotNull(aclObjectIdentityStudy);
		Assertions.assertEquals(activeStudy.getId(), aclObjectIdentityStudy.getObjectIdIdentity());

		// both missing
		AclObjectIdentity foundNull = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(null,
				null);
		assertNull(foundNull);

		// objectIdClass missing
		foundNull = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(null, activeStudy.getId());
		assertNull(foundNull);

		// objectIdIdentity missing
		foundNull = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(aclClassStudy, null);
		assertNull(foundNull);
	}

}
