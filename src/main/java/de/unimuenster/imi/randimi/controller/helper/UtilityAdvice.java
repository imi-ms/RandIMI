package de.unimuenster.imi.randimi.controller.helper;

import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ControllerAdvice
public class UtilityAdvice {

	@ModelAttribute("contextPath")
	public String getContextPath(final HttpServletRequest request) {
		return request.getContextPath();
	}

	@ModelAttribute("authenticatedUser")
	public RandimiUser getAuthenticatedUser(final Authentication authentication) {
		if (authentication == null) {
			return null;
		}

		return ((MyUserDetails) authentication.getPrincipal()).getUser();
	}

	/**
	 * Adding the current base url to the model
	 * @param request Current request.
	 * @return The base url.
	 */
	@ModelAttribute("baseUrl")
	public String getBaseUrl(final HttpServletRequest request) {
		return ServletUriComponentsBuilder.fromRequestUri(request).replacePath(null).build().toUriString();
	}

	/**
	 * Adding the current url to the model
	 * @param request Current request.
	 * @return The url.
	 */
	@ModelAttribute("url")
	public String getUrl(final HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		if (request.getQueryString() != null) {
			url += "?" + request.getQueryString();
		}
		return url;
	}

	@ModelAttribute("path")
	public String getPath(final HttpServletRequest request) {
		var a = request.getRequestURI().substring(request.getContextPath().length());
		return request.getRequestURI().substring(request.getContextPath().length());
	}
}
