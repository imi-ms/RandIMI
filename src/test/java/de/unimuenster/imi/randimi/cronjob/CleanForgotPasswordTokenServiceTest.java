package de.unimuenster.imi.randimi.cronjob;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.service.RandimiUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class CleanForgotPasswordTokenServiceTest extends RandimiIntegrationTest {

	@Autowired RandimiUserRepository randimiUserRepository;
	@Autowired CleanForgotPasswordTokenService cleanForgotPasswordTokenService;
	@Autowired RandimiUserService randimiUserService;

	@Test
	public void cleanExpired() {
		RandimiUser admin = randimiUserRepository.findFirstByUsernameIgnoreCase("ADMIN");

		ForgotPasswordToken token = randimiUserService.createForgotPasswordToken(admin);
		token.setTimestamp(new Timestamp(
				System.currentTimeMillis() - RandimiUserService.FORGOT_PASSWORD_TOKEN_EXPIRATION_THRESHOLD - 1));
		randimiUserRepository.save(admin);

		cleanForgotPasswordTokenService.cleanExpiredTokens();

		assertNull(admin.getForgotPasswordToken());
	}

	@Test
	public void cleanNonExpired() {
		RandimiUser admin = randimiUserRepository.findFirstByUsernameIgnoreCase("ADMIN");

		ForgotPasswordToken token = randimiUserService.createForgotPasswordToken(admin);
		token.setTimestamp(new Timestamp(System.currentTimeMillis()));
		randimiUserRepository.save(admin);

		cleanForgotPasswordTokenService.cleanExpiredTokens();

		assertNotNull(admin.getForgotPasswordToken());
	}

}
