package de.unimuenster.imi.randimi.repository.user;

import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the AclEntry class.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface AclEntryRepository extends AclEntryCrudRepository, AclEntryCustomRepository {
}
