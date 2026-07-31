package de.unimuenster.imi.randimi.repository.study;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.subject.Subject;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.unimuenster.imi.randimi.repository.user.AclEntryRepository;

import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
public class StudyRepositoryTest extends RepositoryTestBase {

	@Autowired
	AclEntryRepository aclEntryRepository;

	@Test
	public void findRegisteredTest() {
		Optional<Subject> registered = studyRepository.findRegistered(activeStudy,
		                                                              activeStudy.getSites().get(0).getId(),
		                                                              "pseudonym1");
		assertTrue(registered.isPresent(), "Pseudonym should be registered!");

		registered = studyRepository.findRegistered(activeStudy, 0L, "");
		assertFalse(registered.isPresent(), "Pseudonym should not be registered!");

		registered = studyRepository.findRegistered(activeStudy, activeStudy.getSites().get(0).getId(), "");
		assertFalse(registered.isPresent(), "Pseudonym should not be registered!");

		registered = studyRepository.findRegistered(activeStudy, 0L, "pseudonym1");
		assertFalse(registered.isPresent(), "Pseudonym should not be registered!");
	}

	@Test
	public void revokeAndGrandRightTest() {
		AclEntry elementUserAccess = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(activeStudy, adminAclSid,
		                                                                                            PermissionType.UPDATE_STUDY);
		assertNull(elementUserAccess, "Permission already exists!");

		studyRepository.grantRight(activeStudy, adminAclSid, PermissionType.UPDATE_STUDY);

		elementUserAccess = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(activeStudy, adminAclSid, PermissionType.UPDATE_STUDY);
		assertNotNull(elementUserAccess, "Right not granted!");

		studyRepository.revokeRight(activeStudy, adminAclSid, PermissionType.UPDATE_STUDY);

		elementUserAccess = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(activeStudy, adminAclSid, PermissionType.UPDATE_STUDY);
		assertNull(elementUserAccess, "Right not revoked!");
	}

}
