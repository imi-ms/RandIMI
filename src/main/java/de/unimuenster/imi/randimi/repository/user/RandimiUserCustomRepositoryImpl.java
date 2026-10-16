package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.config.CacheConfig;
import de.unimuenster.imi.randimi.model.enumeration.PermissionType;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class RandimiUserCustomRepositoryImpl implements RandimiUserCustomRepository {

	private final AclObjectIdentityRepository aclObjectIdentityRepository;
	private final AclClassRepository aclClassRepository;
	private final AclEntryRepository aclEntryRepository;
	private final RandimiUserCrudRepository randimiUserCrudRepository;

	public RandimiUserCustomRepositoryImpl(
			final AclObjectIdentityRepository aclObjectIdentityRepository,
			final AclClassRepository aclClassRepository,
			final AclEntryRepository aclEntryRepository,
			final RandimiUserCrudRepository randimiUserCrudRepository
	) {
		this.aclObjectIdentityRepository = aclObjectIdentityRepository;
		this.aclClassRepository = aclClassRepository;
		this.aclEntryRepository = aclEntryRepository;
		this.randimiUserCrudRepository = randimiUserCrudRepository;
	}

	@Override
	public boolean doesUsernameAlreadyExist(String username, long id) {
		RandimiUser randimiUser = randimiUserCrudRepository.findFirstByUsernameIgnoreCase(username);
		return randimiUser != null && randimiUser.getId() != id;
	}

	@Cacheable(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, key = "#id")
	@Override
	public boolean isUserActive(long id) {
		Optional<RandimiUser> randimiUser = randimiUserCrudRepository.findById(id);
		return randimiUser.isPresent() && randimiUser.get().isEnabled();
	}

	@Override
	public List<RandimiUser> getObsoleteUsers() {
		return randimiUserCrudRepository.findByInvitationTimestampNotNullAndInvitationTimestampLessThan(
				Timestamp.valueOf(LocalDateTime.now().minusDays(30)));
	}

	@Override
	public List<RandimiUser> getNotifiedUsersOfStudy(Study study) {
		AclObjectIdentity aclObjectIdentity = aclObjectIdentityRepository.findFirstByObjectIdClassAndObjectIdIdentity(
				aclClassRepository.findFirstByClassNameOrSynonym(Study.class.getName(), Study.class.getName()),
				study.getId());
		List<AclSid> correspondingSids = aclEntryRepository.getAllAclSidsForAclObjectIdentityAndRight(aclObjectIdentity,
		                                                                                              PermissionType.GET_NOTIFICATION);

		if (correspondingSids.isEmpty())
			return new ArrayList<>();

		return randimiUserCrudRepository.findByAclSidIn(correspondingSids);
	}


}
