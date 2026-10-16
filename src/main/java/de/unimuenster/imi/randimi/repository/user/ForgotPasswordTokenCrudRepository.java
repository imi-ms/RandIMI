package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import org.jspecify.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface ForgotPasswordTokenCrudRepository extends CrudRepository<ForgotPasswordToken, Long> {

	@Nullable
	ForgotPasswordToken findFirstByToken(String token);

	@Nullable
	ForgotPasswordToken findFirstByRandimiUser(RandimiUser user);

	List<ForgotPasswordToken> findByTimestampBefore(Timestamp timestamp);
}
