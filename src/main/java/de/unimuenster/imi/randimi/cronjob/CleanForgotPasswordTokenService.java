package de.unimuenster.imi.randimi.cronjob;

import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.ForgotPasswordTokenRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.service.RandimiUserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for cleaning up the {@link ForgotPasswordToken} table.
 */
@Service
public class CleanForgotPasswordTokenService {

	private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;
	private final RandimiUserRepository randimiUserRepository;
	private final RandimiUserService randimiUserService;

	public CleanForgotPasswordTokenService(final ForgotPasswordTokenRepository forgotPasswordTokenRepository,
	                                       final RandimiUserRepository randimiUserRepository,
	                                       final RandimiUserService randimiUserService) {
		this.forgotPasswordTokenRepository = forgotPasswordTokenRepository;
		this.randimiUserRepository = randimiUserRepository;
		this.randimiUserService = randimiUserService;
	}

	/**
	 * Deletes all expired {@link ForgotPasswordToken} every day at 2:00 AM.
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	public void cleanExpiredTokens() {
		final List<ForgotPasswordToken> tokens = forgotPasswordTokenRepository.findByTimestampBefore(
				randimiUserService.getForgotPasswordTokenExpirationTimestamp());

		for (final ForgotPasswordToken token : tokens) {
			final RandimiUser user = token.getRandimiUser();
			user.setForgotPasswordToken(null);
			randimiUserRepository.save(user);
			forgotPasswordTokenRepository.delete(token);
		}
	}

}
