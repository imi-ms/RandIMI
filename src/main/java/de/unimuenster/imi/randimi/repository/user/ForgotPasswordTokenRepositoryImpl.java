package de.unimuenster.imi.randimi.repository.user;

import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class ForgotPasswordTokenRepositoryImpl {

	@Autowired
	@Lazy
	ForgotPasswordTokenRepository forgotPasswordTokenRepository;

	public RandimiUser getUserForToken(String token) {
		ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findFirstByToken(token);
		if (forgotPasswordToken != null) {
			return forgotPasswordToken.getRandimiUser();
		}
		return null;
	}

}
