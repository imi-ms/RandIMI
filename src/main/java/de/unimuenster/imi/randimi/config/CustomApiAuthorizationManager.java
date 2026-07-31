package de.unimuenster.imi.randimi.config;

import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import org.springframework.lang.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Authorization manager that checks if a user (authentication) has access rights to the API.
 */
@Component
public class CustomApiAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

	@Nullable @Override
	public AuthorizationDecision check(final Supplier<Authentication> authentication,
	                                   final RequestAuthorizationContext object) {
		if (authentication.get().getPrincipal() instanceof String) {
			// User is not logged in
			return null;
		}

		final RandimiUser user = ((MyUserDetails) authentication.get().getPrincipal()).getUser();
		return new AuthorizationDecision((user.hasUserRole(UserRoles.ROLE_API_USER) ||
		                                  user.hasUserRole(UserRoles.ROLE_ADMIN)) &&
		                                 user.isEnabled());
	}
}
