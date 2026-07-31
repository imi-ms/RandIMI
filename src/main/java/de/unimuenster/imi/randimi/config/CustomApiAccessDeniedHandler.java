package de.unimuenster.imi.randimi.config;

import de.unimuenster.imi.randimi.model.api.ErrorResponse;
import de.unimuenster.imi.randimi.service.ErrorResponseService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler for API requests with denied access.
 * Implemented in an AccessDeniedHandler instead of the ApiExceptionHandler,
 * because the ApiExceptionHandler can't differentiate between denied access and unauthorized requests.
 */
@Component
public class CustomApiAccessDeniedHandler implements AccessDeniedHandler {

	private final ErrorResponseService errorResponseService;

	public CustomApiAccessDeniedHandler(final ErrorResponseService errorResponseService) {
		this.errorResponseService = errorResponseService;
	}

	@Override
	public void handle(final HttpServletRequest request, final HttpServletResponse response,
	                   final AccessDeniedException accessDeniedException) throws IOException, ServletException {
		final ErrorResponse errorResponse = errorResponseService.createErrorResponse(HttpStatus.FORBIDDEN, request,
		                                                                             accessDeniedException,
		                                                                             "Access Denied");
		errorResponseService.writeResponse(response, HttpStatus.FORBIDDEN, errorResponse);
	}
}
