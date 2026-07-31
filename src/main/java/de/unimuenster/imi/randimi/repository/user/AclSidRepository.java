package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.AclSid;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the AclSid class.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface AclSidRepository extends CrudRepository<AclSid, Long> {

}
