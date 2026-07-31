package de.unimuenster.imi.randimi.controller.api;

import de.unimuenster.imi.randimi.exceptions.RandimiException;
import de.unimuenster.imi.randimi.model.api.ErrorResponse;
import de.unimuenster.imi.randimi.model.api.ErrorResponseDetails;
import de.unimuenster.imi.randimi.service.ErrorResponseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

@Log4j2
@ControllerAdvice(assignableTypes = {APIControllerV1.class, APIControllerV2.class})
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private final ErrorResponseService errorResponseService;

	public ApiExceptionHandler(ErrorResponseService errorResponseService) {
		this.errorResponseService = errorResponseService;
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
	                                                               HttpHeaders headers, HttpStatusCode status,
	                                                               WebRequest request) {
		final ErrorResponse errorResponse = errorResponseService.createErrorResponse(HttpStatus.FORBIDDEN, request, ex,
		                                                                             "Access Denied");
		// Compatibility for API V1
		errorResponse.setErrors("Access Denied");
		return errorResponseService.createResponseEntity(HttpStatus.FORBIDDEN, errorResponse);
	}

	/**
	 * Custom handler for RandimiExceptions.
	 */
	@ExceptionHandler(RandimiException.class)
	public final ResponseEntity<Object> handleRandimiException(RandimiException ex, WebRequest request) {
		log.info("A handled error occurred:", ex);
		final ErrorResponse errorResponse = errorResponseService.createErrorResponse(ex.getHttpStatusCode(), request,
		                                                                             ex, ex.getMessage());
		errorResponse.setErrorCode(ex.getErrorCode());
		errorResponse.setDetails(ex.getDetails());
		return errorResponseService.createResponseEntity(ex.getHttpStatusCode(), errorResponse);
	}

	/**
	 * Fallback handler for all unhandled exceptions.
	 */
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleUnexpectedException(Exception ex, WebRequest request) {
		if (ex instanceof AccessDeniedException) {
			throw (RuntimeException) ex;
		}

		log.error("An unexpected error occurred:", ex);
		final ErrorResponse errorResponse = errorResponseService.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
		                                                                             request, ex,
		                                                                             "An unexpected error occurred");
		return errorResponseService.createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
	                                                              HttpHeaders headers, HttpStatusCode status,
	                                                              WebRequest request) {
		log.info("Error while parsing a request:", ex);
		final ErrorResponse errorResponse = errorResponseService.createErrorResponse(status, request, ex,
		                                                                             "Message not readable");
		return errorResponseService.createResponseEntity(status, errorResponse);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	                                                              HttpHeaders headers, HttpStatusCode status,
	                                                              WebRequest request) {
		log.info("Error while parsing a request:", ex);

		final ErrorResponse errorResponse = errorResponseService.createErrorResponse(status, request, ex,
		                                                                             "Validation failed. See validationErrors for more details.");

		Map<String, List<String>> errors = new HashMap<>();
		for (final ObjectError error : ex.getBindingResult().getAllErrors()) {
			final String errorKey = (error instanceof FieldError) ? ((FieldError) error).getField()
			                                                      : ex.getBindingResult().getObjectName();

			if (errors.containsKey(errorKey)) {
				errors.get(errorKey).add(error.getDefaultMessage());
			} else {
				List<String> fieldErrors = new ArrayList<>();
				String message = error.getDefaultMessage();

				// TODO hacked in message for failed conversion
				if (message.startsWith("Failed to convert property value of type")) {
					message = "Failed to convert value";
				}

				fieldErrors.add(message);
				errors.put(errorKey, fieldErrors);
			}

		}

		final var details = new ErrorResponseDetails();
		details.setValidationErrors(errors);
		errorResponse.setDetails(details);

		return errorResponseService.createResponseEntity(headers, status, errorResponse);
	}
}
