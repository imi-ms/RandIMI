package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.AclSid;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.CacheAwareCrudRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface RandimiUserCrudRepository extends CacheAwareCrudRepository<RandimiUser> {

	@Nullable
	RandimiUser findFirstByUsernameIgnoreCase(String username);

	@Nullable
	RandimiUser findFirstByInvitationToken(@Nullable String token);

	List<RandimiUser> findByInvitationTimestampNotNullAndInvitationTimestampLessThan(
			Timestamp invitationTimestampThreshold);

	List<RandimiUser> findByAclSidIn(Collection<AclSid> aclSids);

}
