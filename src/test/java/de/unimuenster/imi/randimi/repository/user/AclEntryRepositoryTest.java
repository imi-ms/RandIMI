package de.unimuenster.imi.randimi.repository.user;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel Preciado-Marquez
 */
public class AclEntryRepositoryTest extends RepositoryTestBase {

	@Autowired
	AclEntryRepository aclEntryRepository;

	@Autowired
	AclObjectIdentityRepository aclObjectIdentityRepository;

	@Test
	public void findFirstByObjectAndAclSidAndPermissionTypeTest() {
		AclEntry aclEntry = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(activeStudy,
		                                                                                   apiUser.getAclSid(),
		                                                                                   PermissionType.READ_STUDY);
		assertNotNull(aclEntry, "AclEntry not found!");

		aclEntry = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(activeStudy, apiUser.getAclSid(),
				PermissionType.GET_NOTIFICATION);
		assertNull(aclEntry, "Found AclEntry that should not exist!");

		aclEntry = aclEntryRepository.findFirtsByObjectAndAclSidAndPermissionType(activeStudy, activeUser.getAclSid(),
				PermissionType.READ_AUDIT_SIMPLE);
		assertNull(aclEntry, "Found AclEntry that should not exist!");
	}

	@Test
	public void getObjectIdsForClassUserAndRightTest() {
		List<Long> objectIds = aclEntryRepository.getObjectIdsForClassUserAndRight(Study.class, activeUser.getAclSid(),
		                                                                           PermissionType.READ_STUDY);
		assertEquals(1, objectIds.size());
		Assertions.assertEquals(activeStudy.getId(), objectIds.get(0));

		objectIds = aclEntryRepository.getObjectIdsForClassUserAndRight(Study.class, adminAclSid,
				PermissionType.READ_STUDY);
		assertEquals(0, objectIds.size());

		objectIds = aclEntryRepository.getObjectIdsForClassUserAndRight(Study.class, activeUser.getAclSid(),
				PermissionType.READ_AUDIT_SIMPLE);
		assertEquals(0, objectIds.size());
	}

	@Test
	public void getUserRightsByObjectTest() {
		Map<AclSid, Set<PermissionType>> userRightsActiveStudy = aclEntryRepository.getUserRightsByObject(activeStudy);
		assertEquals(2, userRightsActiveStudy.size());
		assertTrue(userRightsActiveStudy.containsKey(apiUser.getAclSid()));

		Set<PermissionType> permissionsActiveUser = userRightsActiveStudy.get(activeUser.getAclSid());
		assertEquals(2, permissionsActiveUser.size());
		assertTrue(permissionsActiveUser.contains(PermissionType.GET_NOTIFICATION));

		Map<AclSid, Set<PermissionType>> userRightsInactiveStudy = aclEntryRepository
				.getUserRightsByObject(inactiveStudy);
		assertEquals(0, userRightsInactiveStudy.size());
	}

	@Test
	public void getAllAclSidsForAclObjectIdentityAndRightTest() {
		AclObjectIdentity aclObjectIdentityActiveStudy = aclObjectIdentityRepository
				.findFirstByObjectIdClassAndObjectIdIdentity(aclClassStudy, activeStudy.getId());

		List<AclSid> aclSids = aclEntryRepository.getAllAclSidsForAclObjectIdentityAndRight(
				aclObjectIdentityActiveStudy, PermissionType.GET_NOTIFICATION);
		assertEquals(1, aclSids.size());
		assertEquals(activeUser.getAclSid().getId(), aclSids.get(0).getId());

		aclSids = aclEntryRepository.getAllAclSidsForAclObjectIdentityAndRight(aclObjectIdentityActiveStudy,
				PermissionType.CREATE_SUBJECT);
		assertEquals(0, aclSids.size());

		AclObjectIdentity aclObjectIdentityInactiveStudy = aclObjectIdentityRepository
				.findFirstByObjectIdClassAndObjectIdIdentity(aclClassStudy, inactiveStudy.getId());

		aclSids = aclEntryRepository.getAllAclSidsForAclObjectIdentityAndRight(aclObjectIdentityInactiveStudy,
				PermissionType.GET_NOTIFICATION);
		assertEquals(0, aclSids.size());
	}

	@Test
	public void deleteByAclObjectIdentityTest() {
		AclObjectIdentity aclObjectIdentityActiveStudy = aclObjectIdentityRepository
				.findFirstByObjectIdClassAndObjectIdIdentity(aclClassStudy, activeStudy.getId());

		AclObjectIdentity aclObjectIdentityInactiveStudy = aclObjectIdentityRepository
				.findFirstByObjectIdClassAndObjectIdIdentity(aclClassStudy, inactiveStudy.getId());

		aclEntryRepository.deleteByAclObjectIdentity(aclObjectIdentityInactiveStudy);
		assertEquals(10L, aclEntryRepository.count());

		aclEntryRepository.deleteByAclObjectIdentity(aclObjectIdentityActiveStudy);
		assertEquals(7L, aclEntryRepository.count());
	}

	@Test
	public void deleteByAclSidTest() {
		aclEntryRepository.deleteByAclSid(adminAclSid);
		assertEquals(10L, aclEntryRepository.count());

		aclEntryRepository.deleteByAclSid(apiUser.getAclSid());
		assertEquals(4L, aclEntryRepository.count());
	}

	@Test
	public void deleteByAclObjectIdentityAndUserNotTest() {
		AclObjectIdentity aclObjectIdentityActiveStudy = aclObjectIdentityRepository
				.findFirstByObjectIdClassAndObjectIdIdentity(aclClassStudy, activeStudy.getId());

		AclObjectIdentity aclObjectIdentityInactiveStudy = aclObjectIdentityRepository
				.findFirstByObjectIdClassAndObjectIdIdentity(aclClassStudy, inactiveStudy.getId());

		RandimiUser admin = randimiUserRepository.findFirstByUsernameIgnoreCase("ADMIN");

		aclEntryRepository.deleteByAclObjectIdentityAndUserNot(aclObjectIdentityInactiveStudy, activeUser);
		assertEquals(10L, aclEntryRepository.count());

		aclEntryRepository.deleteByAclObjectIdentityAndUserNot(aclObjectIdentityInactiveStudy, admin);
		assertEquals(10L, aclEntryRepository.count());

		aclEntryRepository.deleteByAclObjectIdentityAndUserNot(aclObjectIdentityActiveStudy, admin);
		assertEquals(7L, aclEntryRepository.count());

		aclEntryRepository.deleteByAclObjectIdentityAndUserNot(aclObjectIdentityActiveStudy, apiUser);
		assertEquals(7L, aclEntryRepository.count());
	}

}
