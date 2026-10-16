package de.unimuenster.imi.randimi.service;

import de.unimuenster.imi.randimi.model.api.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Service
public class ErrorResponseService {

	private final JsonMapper jsonMapper;

	public ErrorResponseService(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public ErrorResponse createErrorResponse(final HttpStatusCode status, final WebRequest request,
	                                         final Exception exception, final String detail) {
		return createErrorResponse(status, ((ServletWebRequest) request).getRequest(), exception, detail);
	}

	public ErrorResponse createErrorResponse(final HttpStatusCode status, final HttpServletRequest request,
	                                         final Exception exception, final String detail) {
		return createErrorResponse(status, request.getRequestURI(), exception.getMessage(), detail);
	}

	public ErrorResponse createErrorResponse(final HttpStatusCode status, final String instance,
	                                         @Nullable final String errors, final String detail) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTitle(HttpStatus.valueOf(status.value()).name());
		errorResponse.setStatus(status.value());
		errorResponse.setInstance(instance);
		errorResponse.setDetail(detail);
		errorResponse.setErrors(errors);

		return errorResponse;
	}

	public ResponseEntity<Object> createResponseEntity(final HttpStatusCode status, final ErrorResponse errorResponse) {
		return createResponseEntity(new HttpHeaders(), status, errorResponse);
	}

	public ResponseEntity<Object> createResponseEntity(final HttpHeaders headers, final HttpStatusCode status,
	                                                   final ErrorResponse errorResponse) {
		return ResponseEntity.status(status)
		                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
		                     .headers(headers)
		                     .body(errorResponse);
	}

	public void writeResponse(HttpServletResponse response, final HttpStatusCode status,
	                          final ErrorResponse errorResponse) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.getOutputStream().println(jsonMapper.writeValueAsString(errorResponse));
	}
}
