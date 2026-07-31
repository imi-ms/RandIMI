package de.unimuenster.imi.randimi.repository.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Daniel Preciado-Marquez
 */
public class ForgotPasswordTokenRepositoryTest extends RepositoryTestBase {

	@Autowired
	ForgotPasswordTokenRepository forgotPasswordTokenRepository;

	String token;

	@BeforeEach
	public void init() {
		// user is stored as bytes so the tokens have to be created here
		// Create token for ADMIN
		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(admin);
		token = forgotPasswordToken.getToken();
		forgotPasswordTokenRepository.save(forgotPasswordToken);

		// Create token for TEST_USER
		forgotPasswordToken = new ForgotPasswordToken(activeUser);
		forgotPasswordTokenRepository.save(forgotPasswordToken);
	}

	@Test
	public void findFirstByTokenTest() {
		ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findFirstByToken(token);
		assertNotNull(forgotPasswordToken, "Token not found!");
		assertEquals(token, forgotPasswordToken.getToken(), "Found wrong token!");

		forgotPasswordToken = forgotPasswordTokenRepository.findFirstByToken("not existing token");
		assertNull(forgotPasswordToken, "Found not existing token!");
	}

	@Test
	public void getUserForTokenTest() {
		RandimiUser user = forgotPasswordTokenRepository.getUserForToken(token);
		assertNotNull(user, "User not found!");
		assertEquals("ADMIN", user.getUsername(), "Found wrong user!");

		user = forgotPasswordTokenRepository.getUserForToken("not existing token");
		assertNull(user, "Found user from not existing token!");
	}

	@Test
	public void findFirstByRandimiUserTest() {
		ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findFirstByRandimiUser(admin);
		assertNotNull(forgotPasswordToken, "Token not found!");
		assertEquals(token, forgotPasswordToken.getToken(), "Found wrong token!");

		forgotPasswordToken = forgotPasswordTokenRepository.findFirstByRandimiUser(inactiveUser);
		assertNull(forgotPasswordToken, "Found not existing token!");
	}

}
