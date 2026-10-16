package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import org.jspecify.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface AclEntryCrudRepository extends CrudRepository<AclEntry, Long> {

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

	List<AclEntryRepository.PermissionTypeColumn> findByAclObjectIdentityAndAclSid(
			@Nullable AclObjectIdentity aclObjectIdentity,
			AclSid aclSid);

	@Nullable
	AclEntry findFirstByAclObjectIdentityAndAclSidAndPermissionType(
			@Nullable AclObjectIdentity aclObjectIdentity, AclSid aclSid, PermissionType permissionType);

	List<AclEntry> findByAclObjectIdentityIdInAndAclSidAndPermissionType(Collection<Long> aclObjectIdentities,
	                                                                     AclSid aclSid, PermissionType permissionType);

	List<AclEntryRepository.AclSidPermissionTypeColumn> findByAclObjectIdentity(
			@Nullable AclObjectIdentity aclObjectIdentity);

	List<AclEntryRepository.AclSidColumn> findByAclObjectIdentityAndPermissionType(
			@Nullable AclObjectIdentity aclObjectIdentity, PermissionType permissionType);

	void deleteByAclObjectIdentity(AclObjectIdentity aclObjectIdentity);

	void deleteByAclSid(AclSid aclSid);

	void deleteByAclObjectIdentityAndAclSidNot(AclObjectIdentity aclObjectIdentity, AclSid aclSid);
}
