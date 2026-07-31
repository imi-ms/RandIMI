package de.unimuenster.imi.randimi.repository.user;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.model.user.AclEntry;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class AclEntryRepositoryImpl {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private AclClassRepository aclClassRepository;

	@Autowired
	@Lazy
	private AclEntryRepository aclEntryRepository;

	@Autowired
	private AclObjectIdentityRepository aclObjectIdentityRepository;

	public void persist(AclEntry aclEntry) {
		entityManager.persist(aclEntry);
	}

	public AclEntry findFirtsByObjectAndAclSidAndPermissionType(Object object, AclSid aclSid, PermissionType right) {
		// Get the corresponding ACLClass object for the given object
		AclClass aclClass = aclClassRepository.findFirstByClassNameOrSynonym(object.getClass().getName(),
		                                                                     object.getClass().getName());
		try {
			// Get the getId method for the element
			Method method = getMethodFromString(object.getClass(), "getId");
			// Get the database Id for the element
			Long objectId = (Long) method.invoke(object);
			// Get the acl object identity for the given object
			AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository
					.findFirstByObjectIdClassAndObjectIdIdentity(aclClass, objectId);

			return aclEntryRepository.findFirstByAclObjectIdentityAndAclSidAndPermissionType(aclObjectIdentity, aclSid,
					right);
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException exception) {
			return null;
		}
	}

	public List<Long> getObjectIdsForClassUserAndRight(Class<?> clazz, AclSid aclSid, PermissionType right) {
		// Initialize result list
		List<Long> objectIds = new ArrayList<>();
		// Get the corresponding ACLClass object for the given class
		AclClass aclClass = aclClassRepository.findFirstByClassNameOrSynonym(clazz.getName(), clazz.getName());
		// Get all managed object identities for given class
		List<AclObjectIdentity> aclObjectIdentities = aclObjectIdentityRepository.findByObjectIdClass(aclClass);
		// [bt] querying the database only has to happen if at least one object is managed by ACLs
		if (!aclObjectIdentities.isEmpty()) {
			Collection<Long> aclObjectIdentityIds = new ArrayList<>();

			for (AclObjectIdentity aclObjectIdentity : aclObjectIdentities)
				aclObjectIdentityIds.add(aclObjectIdentity.getId());

			List<AclEntry> aclEntries = aclEntryRepository
					.findByAclObjectIdentityIdInAndAclSidAndPermissionType(aclObjectIdentityIds, aclSid, right);

			for (AclEntry aclEntry : aclEntries)
				objectIds.add(aclEntry.getAclObjectIdentity().getObjectIdIdentity());
		}
		return objectIds;
	}

	public Set<PermissionType> getPermissionTypesByObjectAndUser(final Object object, final RandimiUser user) {
		// Get the corresponding ACLClass object for the given object
		AclClass aclClass = aclClassRepository.findFirstByClassNameOrSynonym(object.getClass().getName(),
				object.getClass().getName());
		try {
			// Get the getId method for the element
			Method method = getMethodFromString(object.getClass(), "getId");
			// Get the database Id for the element
			Long objectId = (Long) method.invoke(object);
			// Get the acl object identity for the given object
			AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository
					.findFirstByObjectIdClassAndObjectIdIdentity(aclClass, objectId);

			List<AclEntryRepository.PermissionTypeColumn> columns = aclEntryRepository
					.findByAclObjectIdentityAndAclSid(aclObjectIdentity, user.getAclSid());
			Set<PermissionType> userRights = new HashSet<>();

			for (AclEntryRepository.PermissionTypeColumn column : columns)
				userRights.add(column.getPermissionType());

			return userRights;
		} catch (NoResultException | NoSuchMethodException | SecurityException | IllegalAccessException
		         | IllegalArgumentException | InvocationTargetException ex) {
			return null;
		}
	}

	public Map<AclSid, Set<PermissionType>> getUserRightsByObject(Object object) {
		// Get the corresponding ACLClass object for the given object
		AclClass aclClass = aclClassRepository.findFirstByClassNameOrSynonym(object.getClass().getName(),
				object.getClass().getName());
		try {
			// Get the getId method for the element
			Method method = getMethodFromString(object.getClass(), "getId");
			// Get the database Id for the element
			Long objectId = (Long) method.invoke(object);
			// Get the acl object identity for the given object
			AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository
					.findFirstByObjectIdClassAndObjectIdIdentity(aclClass, objectId);

			List<AclEntryRepository.AclSidPermissionTypeColumn> aclSidPermissionTypeColumns = aclEntryRepository
					.findByAclObjectIdentity(aclObjectIdentity);
			Map<AclSid, Set<PermissionType>> userRights = new ConcurrentHashMap<>(aclSidPermissionTypeColumns.size());

			for (AclEntryRepository.AclSidPermissionTypeColumn aclSidPermissionTypeColumn : aclSidPermissionTypeColumns)
				userRights.computeIfAbsent(aclSidPermissionTypeColumn.getAclSid(), key -> new HashSet<>())
						.add(aclSidPermissionTypeColumn.getPermissionType());

			return userRights;
		} catch (NoResultException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException ex) {
			return null;
		}
	}

	public List<AclSid> getAllAclSidsForAclObjectIdentityAndRight(AclObjectIdentity aclObjectIdentity,
			PermissionType right) {
		List<AclEntryRepository.AclSidColumn> aclSidColumns = aclEntryRepository
				.findByAclObjectIdentityAndPermissionType(aclObjectIdentity, right);
		return aclSidColumns.stream().map(aclSidColumn -> aclSidColumn.getAclSid()).collect(Collectors.toList());
	}

	public void deleteByAclObjectIdentityAndUserNot(AclObjectIdentity aclObjectIdentity, RandimiUser user) {
		aclEntryRepository.deleteByAclObjectIdentityAndAclSidNot(aclObjectIdentity, user.getAclSid());
	}

	private Method getMethodFromString(Class<?> clazz, String methodName) throws NoSuchMethodException {
		if (clazz == null) {
			throw new NoSuchMethodException();
		}
		try {
			return clazz.getDeclaredMethod(methodName);

		} catch (NoSuchMethodException ex) {
			return getMethodFromString(clazz.getSuperclass(), methodName);
		}
	}

	public boolean hasPermission(final RandimiUser randimiUser, final Object object, final PermissionType permissionType) {
		final AclEntry aclEntry = findFirtsByObjectAndAclSidAndPermissionType(object, randimiUser.getAclSid(), permissionType);
		return aclEntry != null;
	}
}
