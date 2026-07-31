package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;

import java.util.*;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the AclEntry class.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface AclEntryRepository extends CrudRepository<AclEntry, Long> {

	interface PermissionTypeColumn {
		PermissionType getPermissionType();
	}

	interface AclSidColumn {
		AclSid getAclSid();
	}

	interface AclSidPermissionTypeColumn {
		AclSid getAclSid();

		PermissionType getPermissionType();
	}

	public List<PermissionTypeColumn> findByAclObjectIdentityAndAclSid(AclObjectIdentity aclObjectIdentity,
	                                                                   AclSid aclSid);

	public AclEntry findFirstByAclObjectIdentityAndAclSidAndPermissionType(AclObjectIdentity aclObjectIdentity,
			AclSid aclSid, PermissionType permissionType);

	public List<AclEntry> findByAclObjectIdentityIdInAndAclSidAndPermissionType(Collection<Long> aclObjectIdentities,
			AclSid aclSid, PermissionType permissionType);

	public List<AclSidPermissionTypeColumn> findByAclObjectIdentity(AclObjectIdentity aclObjectIdentity);

	public List<AclSidColumn> findByAclObjectIdentityAndPermissionType(AclObjectIdentity aclObjectIdentity,
			PermissionType permissionType);

	public void deleteByAclObjectIdentity(AclObjectIdentity aclObjectIdentity);

	void deleteByAclSid(AclSid aclSid);

	public void deleteByAclObjectIdentityAndAclSidNot(AclObjectIdentity aclObjectIdentity, AclSid aclSid);

	public void persist(AclEntry aclEntry);

	public AclEntry findFirtsByObjectAndAclSidAndPermissionType(Object object, AclSid aclSid,
			PermissionType permissionType);

	public List<Long> getObjectIdsForClassUserAndRight(Class<?> clazz, AclSid aclSid, PermissionType right);

	Set<PermissionType> getPermissionTypesByObjectAndUser(Object object, RandimiUser user);

	public Map<AclSid, Set<PermissionType>> getUserRightsByObject(Object object);

	public List<AclSid> getAllAclSidsForAclObjectIdentityAndRight(AclObjectIdentity aclObjectIdentity,
			PermissionType right);

	public void deleteByAclObjectIdentityAndUserNot(AclObjectIdentity aclObjectIdentity, RandimiUser user);

	public boolean hasPermission(final RandimiUser randimiUser, final Object object, final PermissionType permissionType);
}
