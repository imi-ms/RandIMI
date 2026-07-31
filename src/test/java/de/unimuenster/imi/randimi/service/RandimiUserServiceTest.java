package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class RandimiUserServiceTest extends RepositoryTestBase {

	@Autowired
	private RandimiUserService randimiUserService;

	@Test
	public void deleteUser() {
		long numberUsers = randimiUserRepository.count();
		long activeStudyUsers =  activeStudy.getAssignedUsers().size();
		randimiUserService.deleteUser(apiUser);

		assertEquals(numberUsers - 1, randimiUserRepository.count(), "User has not been deleted");
		assertFalse(randimiUserRepository.existsById(apiUser.getId()));
		assertEquals(activeStudyUsers - 1, activeStudy.getAssignedUsers().size());
	}

	@Test
	public void isNotExpiredForgotPasswordTokenExpired() {
		var token =  new ForgotPasswordToken();
		token.setTimestamp(new Timestamp(System.currentTimeMillis()));
		assertFalse(randimiUserService.isForgotPasswordTokenExpired(token));
	}

	@Test
	public void isExpiredForgotPasswordTokenExpired() {
		var token =  new ForgotPasswordToken();
		token.setTimestamp(new Timestamp(
				System.currentTimeMillis() - RandimiUserService.FORGOT_PASSWORD_TOKEN_EXPIRATION_THRESHOLD - 1));
		assertTrue(randimiUserService.isForgotPasswordTokenExpired(token));
	}
}
