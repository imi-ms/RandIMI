package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.UserRole;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the UserRole class.
 *
 * @author Tobais Brix
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
}
