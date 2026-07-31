package de.unimuenster.imi.randimi.config;

import de.unimuenster.imi.randimi.model.api.ErrorResponse;
import de.unimuenster.imi.randimi.service.ErrorResponseService;
import de.unimuenster.imi.randimi.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler for unauthorized API requests.
 * Implemented in an AuthenticationEntryPoint instead of the ApiExceptionHandler,
 * because the ApiExceptionHandler can't differentiate between denied access and unauthorized requests.
 */
@Component
public class CustomApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final MessageService messageService;
	private final ErrorResponseService errorResponseService;

	@Autowired
	public CustomApiAuthenticationEntryPoint(final MessageService messageService,
	                                         final ErrorResponseService errorResponseService) {
		this.messageService = messageService;
		this.errorResponseService = errorResponseService;
	}

	@Override
	public void commence(final HttpServletRequest request, final HttpServletResponse response,
	                     final AuthenticationException authException) throws IOException {
		final ErrorResponse errorResponse = errorResponseService.createErrorResponse(HttpStatus.UNAUTHORIZED, request,
		                                                                             authException,
		                                                                             messageService.getMessage("error.noAuthorization"));
		// Hint for browsers to show an HTTP basic login
		response.setHeader("WWW-Authenticate", "Basic");
		errorResponseService.writeResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
	}

}
