package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Preciado-Marquez
 */
public interface AclEntryCustomRepository {

	void persist(AclEntry aclEntry);

	@Nullable
	AclEntry findFirstByObjectAndAclSidAndPermissionType(Object object, AclSid aclSid, PermissionType permissionType);

	List<Long> getObjectIdsForClassUserAndRight(Class<?> clazz, AclSid aclSid, PermissionType right);

	@Nullable
	Set<PermissionType> getPermissionTypesByObjectAndUser(Object object, RandimiUser user);

	@Nullable
	Map<AclSid, Set<PermissionType>> getUserRightsByObject(Object object);

	List<AclSid> getAllAclSidsForAclObjectIdentityAndRight(@Nullable AclObjectIdentity aclObjectIdentity,
	                                                       PermissionType right);

	void deleteByAclObjectIdentityAndUserNot(AclObjectIdentity aclObjectIdentity, RandimiUser user);

	boolean hasPermission(RandimiUser randimiUser, Object object, PermissionType permissionType);
}
