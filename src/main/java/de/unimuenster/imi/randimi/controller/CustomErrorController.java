package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.model.api.ErrorResponse;
import de.unimuenster.imi.randimi.service.ErrorResponseService;
import de.unimuenster.imi.randimi.service.MessageService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling redirects to '/error' after an exception occurred.
 *
 * @author Daniel Preciado-Marquez
 */
@Controller
public class CustomErrorController extends AbstractErrorController {
	private static final String ERROR_PATH = "/error";

	private final MessageService messageService;

	private final ErrorResponseService errorResponseService;

	@Autowired
	public CustomErrorController(final ErrorAttributes errorAttributes, final MessageService messageService,
	                             final ErrorResponseService errorResponseService) {
		super(errorAttributes);
		this.messageService = messageService;
		this.errorResponseService = errorResponseService;
	}

	/**
	 * Redirects to the start page in case of an error.
	 *
	 * @param request            Request injected by Spring.
	 * @param redirectAttributes RedirectAttributes injected by Spring.
	 * @return Redirect path.
	 */
	@RequestMapping(ERROR_PATH)
	public Object handleError(final HttpServletRequest request, final RedirectAttributes redirectAttributes) {
		final HttpStatus status = getStatus(request);

		final String messageCode = switch (status) {
			case BAD_REQUEST -> "error.invalidRequest";
			case NOT_FOUND -> "error.pageNotFound";
			default -> "error.unexpected";
		};
		final String message = messageService.getMessage(messageCode);

		String requestURI = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
		if (requestURI.contains("/api")) {
			final ErrorResponse errorResponse = errorResponseService.createErrorResponse(status, requestURI, null,
			                                                                             message);
			return errorResponseService.createResponseEntity(status, errorResponse);
		}

		redirectAttributes.addFlashAttribute("error", message);

		return "redirect:/";
	}
}
