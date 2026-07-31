package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.config.CacheConfig;
import de.unimuenster.imi.randimi.model.study.Study;
import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import de.unimuenster.imi.randimi.repository.CacheAwareCrudRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the RandimiUser class.
 *
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface RandimiUserRepository extends CacheAwareCrudRepository<RandimiUser> {

	RandimiUser findFirstByUsernameIgnoreCase(String username);

	RandimiUser findFirstByInvitationToken(String token);

	List<RandimiUser> findByInvitationTimestampNotNullAndInvitationTimestampLessThan(
			Timestamp invitationTimestampThreshold);

	List<RandimiUser> findByAclSidIn(Collection<AclSid> aclSids);

	// ====
	// Methods implemented in RandimiUserRepositoryImpl
	// ====

	boolean doesUsernameAlreadyExist(String username, long id);

	@Cacheable(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, key = "#id")
	boolean isUserActive(long id);

	List<RandimiUser> getObsoleteUsers();

	List<RandimiUser> getNotifiedUsersOfStudy(Study study);
}
