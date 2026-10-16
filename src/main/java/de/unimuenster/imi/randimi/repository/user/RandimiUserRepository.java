package de.unimuenster.imi.randimi.repository.user;

import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the RandimiUser class.
 *
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface RandimiUserRepository extends RandimiUserCrudRepository, RandimiUserCustomRepository {
}
