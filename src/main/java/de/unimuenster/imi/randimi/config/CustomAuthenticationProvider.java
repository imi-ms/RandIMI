package de.unimuenster.imi.randimi.config;

import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider {

	private final RandimiUserDetailsService randimiUserDetailsService;

	@Autowired
	public CustomAuthenticationProvider(final RandimiUserDetailsService randimiUserDetailsService) {
		this.randimiUserDetailsService = randimiUserDetailsService;
	}

	/**
	 * Used to specify the encoder used do encrypt the password in the database.
	 *
	 * @return The used PasswordEncoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(randimiUserDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		authProvider.setHideUserNotFoundExceptions(false);
		return authProvider;
	}

	@Bean
	public DaoAuthenticationProvider apiAuthenticationProvider(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(randimiUserDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

}
