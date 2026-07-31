package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.AclClass;
import de.unimuenster.imi.randimi.model.user.AclObjectIdentity;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the AclObjectIdentity class.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Transactional(readOnly = true)
public interface AclObjectIdentityRepository extends CrudRepository<AclObjectIdentity, Long> {

	@Nullable
	AclObjectIdentity findFirstByObjectIdClassAndObjectIdIdentity(AclClass objectIdClass, Long objectIdIdentity);

	List<AclObjectIdentity> findByObjectIdClass(AclClass objectIdClass);
}
