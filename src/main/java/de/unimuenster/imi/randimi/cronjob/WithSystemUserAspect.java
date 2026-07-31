package de.unimuenster.imi.randimi.cronjob;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Aspect
@Component
public class WithSystemUserAspect {

	public static final String SYSTEM_USERNAME = "SYSTEM";
	public static final String SYSTEM_ROLE = "ROLE_SYSTEM";

	@Around("@annotation(WithSystemUser)")
	public Object withSystemUser(final ProceedingJoinPoint pjp) throws Throwable {
		var systemAuth = new UsernamePasswordAuthenticationToken(
				SYSTEM_USERNAME, null, Collections.singletonList(new SimpleGrantedAuthority(SYSTEM_ROLE))
		);
		SecurityContextHolder.getContext().setAuthentication(systemAuth);

		final var proceed = pjp.proceed();

		SecurityContextHolder.clearContext();

		return proceed;
	}

}
