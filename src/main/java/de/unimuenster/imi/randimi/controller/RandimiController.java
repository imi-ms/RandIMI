package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;

import de.unimuenster.imi.randimi.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Class to handle the welcome and access denied page requests.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Controller
public class RandimiController {

	private final RandimiUserRepository userRepository;
	private final MessageService messageService;

	public RandimiController(final RandimiUserRepository userRepository, final MessageService messageService) {
		this.userRepository = userRepository;
		this.messageService = messageService;
	}

	/**
	 * @param error  Displaces an error message in the login box.
	 *               Will be set if an invalid username or password has been used.
	 * @param logout Shows a successful logout message in the login box. Will be shown if the logout was successful.
	 * @return Returns The welcome or startpage.
	 */
	@RequestMapping(value = {"/", "/welcome"}, method = RequestMethod.GET)
	public String getWelcomePage(@RequestParam(value = "error", required = false) String error,
	                             @ModelAttribute("success") String success,
	                             @RequestParam(value = "logout", required = false) String logout, Model model) {
		if (error != null) {
			final String errorCode = "login.error." + error;
			model.addAttribute("error",
			                   messageService.getMessage(errorCode, new Object[]{}, LocaleContextHolder.getLocale()));
		} else if (success != null) {
			if (logout != null)
				model.addAttribute("success", messageService.getMessage("logout.success"));
			else
				model.addAttribute("success", success);
		}
		return "/welcome";
	}

	@RequestMapping(value = "/AccessDenied", method = RequestMethod.GET)
	public String getAccessDeniedPage(Model model, HttpServletRequest request) {
		//check if user is login
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (!(auth instanceof AnonymousAuthenticationToken)) {
			MyUserDetails userDetail = (MyUserDetails) auth.getPrincipal();
			model.addAttribute("username", userDetail.getUsername());

			RandimiUser user = userDetail.getUser();
			if (!userRepository.isUserActive(user.getId()))
				new SecurityContextLogoutHandler().logout(request, null, null);
		}
		return "/utils/accessdenied";
	}
}
