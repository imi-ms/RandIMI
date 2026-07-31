package de.unimuenster.imi.randimi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Class used for the security definitions.
 * It handles, which users are allowed to access which part of the application.
 *
 * @author Tobias Brix
 */
@Configuration
@EnableWebSecurity
public class MultiHttpSecurityConfig {

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	/**
	 * Handles the authorization for the API calls.
	 */
	@Configuration
	@Order(1)
	public static class APIWebSecurityConfig {

		private final CustomApiAccessDeniedHandler customApiAccessDeniedHandler;
		private final CustomApiAuthenticationEntryPoint customApiAuthenticationEntryPoint;
		private final CustomApiAuthorizationManager customApiAuthorizationManager;
		private final DaoAuthenticationProvider apiAuthenticationProvider;
		private final SessionRegistry sessionRegistry;

		@Autowired
		public APIWebSecurityConfig(final CustomApiAccessDeniedHandler customApiAccessDeniedHandler,
		                            final CustomApiAuthenticationEntryPoint customApiAuthenticationEntryPoint,
		                            final CustomApiAuthorizationManager customApiAuthorizationManager,
		                            final DaoAuthenticationProvider apiAuthenticationProvider,
		                            final SessionRegistry sessionRegistry) {
			this.customApiAccessDeniedHandler = customApiAccessDeniedHandler;
			this.customApiAuthenticationEntryPoint = customApiAuthenticationEntryPoint;
			this.customApiAuthorizationManager = customApiAuthorizationManager;
			this.apiAuthenticationProvider = apiAuthenticationProvider;
			this.sessionRegistry = sessionRegistry;
		}

		@Bean
		public SecurityFilterChain apiFilterChain(final HttpSecurity http) throws Exception {
			http.securityMatcher("/api/**")
			    .cors(Customizer.withDefaults())
			    .csrf(AbstractHttpConfigurer::disable)
			    .authorizeHttpRequests(auth -> auth
					    .anyRequest().access(customApiAuthorizationManager))
			    .httpBasic(Customizer.withDefaults())
			    .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
					    .accessDeniedHandler(customApiAccessDeniedHandler)
					    .authenticationEntryPoint(customApiAuthenticationEntryPoint))
			    .authenticationProvider(apiAuthenticationProvider)
			    .sessionManagement(sessionManagement -> sessionManagement
					.maximumSessions(1)
					.sessionRegistry(sessionRegistry));

			return http.build();
		}
	}

	/**
	 * Handles the authorization for the web-login.
	 */
	@Configuration
	@Order(2)
	public static class FormWebSecurityConfig {

		private final DaoAuthenticationProvider authenticationProvider;
		private final CustomAuthorizationManager customAuthorizationManager;
		private final SessionRegistry sessionRegistry;

		@Autowired
		public FormWebSecurityConfig(final DaoAuthenticationProvider authenticationProvider,
		                             final CustomAuthorizationManager customAuthorizationManager,
		                             final SessionRegistry sessionRegistry) {
			this.authenticationProvider = authenticationProvider;
			this.customAuthorizationManager = customAuthorizationManager;
			this.sessionRegistry = sessionRegistry;
		}

		@Bean
		public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
			http.csrf(Customizer.withDefaults())
			    .authorizeHttpRequests(auth -> auth
					    .anyRequest().access(customAuthorizationManager))
			    .formLogin(httpSecurityFormLoginConfigurer -> httpSecurityFormLoginConfigurer
					    .loginProcessingUrl("/j_spring_security_check")
					    .loginPage("/")
					    .failureUrl("/?error=badCredentials")
					    .usernameParameter("user")
					    .passwordParameter("pass"))
			    .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
					    .logoutSuccessUrl("/?logout")
					    .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults()
					                                                   .matcher("/j_spring_security_logout")))
			    .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
					    .accessDeniedPage("/AccessDenied"))
			    .authenticationProvider(authenticationProvider)
			    .sessionManagement(sessionManagement -> sessionManagement
					    .maximumSessions(1)
					    .sessionRegistry(sessionRegistry));

			return http.build();
		}
	}

}
