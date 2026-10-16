package de.unimuenster.imi.randimi.repository.user;

import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the ForgotPasswordToken class.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface ForgotPasswordTokenRepository extends ForgotPasswordTokenCrudRepository, ForgotPasswordTokenCustomRepository {
}
