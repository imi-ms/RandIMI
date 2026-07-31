package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.RandimiUser;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

/**
 * Data access object used for the ForgotPasswordToken class.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface ForgotPasswordTokenRepository extends CrudRepository<ForgotPasswordToken, Long> {

	ForgotPasswordToken findFirstByToken(String token);

	ForgotPasswordToken findFirstByRandimiUser(RandimiUser user);

	List<ForgotPasswordToken> findByTimestampBefore(Timestamp timestamp);

	RandimiUser getUserForToken(String token);
}
