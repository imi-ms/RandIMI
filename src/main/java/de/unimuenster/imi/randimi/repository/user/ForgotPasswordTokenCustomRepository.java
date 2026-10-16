package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.RandimiUser;
import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Preciado-Marquez
 */
public interface ForgotPasswordTokenCustomRepository {
	@Nullable
	RandimiUser getUserForToken(String token);
}
