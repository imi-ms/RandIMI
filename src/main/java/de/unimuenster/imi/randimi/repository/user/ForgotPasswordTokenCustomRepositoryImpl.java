package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class ForgotPasswordTokenCustomRepositoryImpl implements ForgotPasswordTokenCustomRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Nullable
	@Override
	public RandimiUser getUserForToken(String token) {
		ForgotPasswordToken forgotPasswordToken = entityManager.createQuery(
				"select token from ForgotPasswordToken token where token.token = :token",
				ForgotPasswordToken.class)
				.setParameter("token", token)
				.setMaxResults(1)
				.getResultStream()
				.findFirst()
				.orElse(null);
		if (forgotPasswordToken != null) {
			return forgotPasswordToken.getRandimiUser();
		}
		return null;
	}
}
