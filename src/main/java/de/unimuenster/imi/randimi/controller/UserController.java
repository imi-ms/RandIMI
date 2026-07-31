package de.unimuenster.imi.randimi.controller;

import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import de.unimuenster.imi.randimi.dto.user.ForgotPasswordTokenDTO;
import de.unimuenster.imi.randimi.dto.user.UserDTO;
import de.unimuenster.imi.randimi.mapping.user.AccountDetailsMapper;
import de.unimuenster.imi.randimi.mapping.user.UserMapper;
import de.unimuenster.imi.randimi.model.enumeration.UserEditStatus;
import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.ForgotPasswordTokenRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.service.RandimiMailService;
import de.unimuenster.imi.randimi.service.RandimiUserService;
import de.unimuenster.imi.randimi.validator.user.ForgotPasswordTokenDTOValidator;
import de.unimuenster.imi.randimi.validator.user.UserDTOValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * Class to handle all user related requests.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 * @author Daniel Preciado-Marquez
 */
@Controller
@RequestMapping(value = "/users")
public class UserController {

	private static final Logger LOGGER = LogManager.getLogger(UserController.class);

	private static final String ACCOUNT_DETAILS_KEY = "accountDetails";
	private static final String FORGOT_PASSWORT_TOKEN_KEY = "forgotPasswordToken";
	private static final String INVITATION_TOKEN_KEY = "token";
	private static final String USER_KEY = "user";

	@Autowired
	RandimiUserRepository userRepository;
	@Autowired
	ForgotPasswordTokenRepository forgotPasswordTokenRepository;

	@Autowired
	UserDTOValidator userDTOValidator;

	@Autowired
	ForgotPasswordTokenDTOValidator forgotPasswordTokenDTOValidator;
	@Autowired
	RandimiMailService mailService;
	@Autowired
	MessageService messageService;
	@Autowired
	RandimiUserService randimiUserService;

	@Autowired
	AccountDetailsMapper accountDetailsMapper;
	@Autowired
	UserMapper userMapper;

	@RequestMapping(value = {"", "/", "/list"}, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', T(de.unimuenster.imi.randimi.model.enumeration.UserRoles).ROLE_LOCAL_MANAGER, 'ROLE_USER_MANAGER')")
	public String getUserPage(Model model) {
		List<UserDTO> allUserDTOs = new ArrayList<>();
		for (RandimiUser user : userRepository.findAll()) {
			allUserDTOs.add(userMapper.toUserDTO(user));
		}
		model.addAttribute("users", allUserDTOs);
		return "/users/list";
	}

	@RequestMapping(value = "/invite", method = RequestMethod.GET)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionUser(authentication, #userId)")
	public String getInvitationPage(@RequestParam(value = "id", required = false) Long userId,
	                                Model model,
	                                HttpServletRequest request) {
		// Check if redirected
		UserDTO requestedUserDTO = (UserDTO) model.getAttribute(USER_KEY);
		RandimiUser requestedUser = null;

		if (requestedUserDTO == null) {
			// Check whether there is a user to edit or not
			if (userId != null && userRepository.existsById(userId)) {
				requestedUser = userRepository.findById(userId).get();
				requestedUserDTO = userMapper.toUserDTO(requestedUser);
				model.addAttribute(USER_KEY, requestedUserDTO);
			} else {
				requestedUserDTO = userMapper.toUserDTO(new RandimiUser());
			}
		}

		if (requestedUser != null) {
			model.addAttribute("invitationTimestamp", requestedUser.getInvitationTimestamp());
			model.addAttribute("isInvitationExpired", randimiUserService.isInvitationExpired(requestedUser));
			model.addAttribute("invitationLink", randimiUserService.assembleInvitationLink(request, requestedUser));
		}

		// The role api user will be handled separately
		final List<UserRoles> userRoles = new ArrayList<>(Arrays.asList(UserRoles.values()));
		userRoles.remove(UserRoles.ROLE_API_USER);

		model.addAttribute("apiRole", UserRoles.ROLE_API_USER);
		model.addAttribute(USER_KEY, requestedUserDTO);
		model.addAttribute("userRoles", userRoles);
		return "/users/invite";
	}

	@RequestMapping(value = "/invite", method = RequestMethod.POST)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionUser(authentication, #userDTO)")
	public String inviteUser(@RequestParam String action, @Valid @ModelAttribute("user") UserDTO userDTO,
	                         BindingResult result, HttpServletRequest request, RedirectAttributes ra,
	                         final Authentication authentication) {
		if (action.equalsIgnoreCase("cancel")) {
			return "redirect:/users";
		}
		userDTO.setStatus(UserEditStatus.INVITATION);

		// Validate the user DTO
		userDTOValidator.validate(userDTO, result);

		if (result.hasErrors()) {
			ra.addAttribute("id", userDTO.getId());
			ra.addFlashAttribute(USER_KEY, userDTO);
			ra.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + USER_KEY, result);
			ra.addFlashAttribute("error", messageService.getMessage("general.error.invalidForm"));
			return "redirect:/users/invite";
		}

		RandimiUser currentUser = ((MyUserDetails) authentication.getPrincipal()).getUser();
		currentUser = userRepository.findById(currentUser.getId()).get();

		RandimiUser user;

		// Get the right user or create a new one if the user does not exist
		if (userDTO.getId() == 0 || !userRepository.existsById(userDTO.getId())) {
			user = userMapper.toUser(userDTO, new RandimiUser(), currentUser);
			user.setInvitedBy(currentUser);
		} else {
			user = userMapper.toUser(userDTO, userRepository.findById(userDTO.getId()).get(), currentUser);
			if (userDTO.getInvitedBy() != null) {
				user.setInvitedBy(userRepository.findFirstByUsernameIgnoreCase(userDTO.getInvitedBy()));
			}
		}

		if (userDTO.isSkipEMailValidation()) {
			user.setEnabled(true);
			user.setInvitationToken(null);
			user.setInvitationTimestamp(null);

			ra.addFlashAttribute("success", messageService.getMessage("users.invite.success"));
		} else {
			user.setEnabled(false);
			if (user.getInvitationToken() == null) {
				boolean isInvited = randimiUserService.inviteUser(user, request);
				if (isInvited) {
					ra.addFlashAttribute("success", messageService.getMessage("invitation.mail.success"));
				} else {
					ra.addFlashAttribute("error", messageService.getMessage("mail.error"));
				}
			}
		}

		userRepository.save(user);
		return "redirect:/users";
	}

	@RequestMapping(value = "/invitation", method = RequestMethod.GET)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionUser(authentication, #userId)")
	public String getInvitation(@RequestParam(value = "id") Long userId,
	                            Model model,
	                            RedirectAttributes redirectAttributes,
	                            HttpServletRequest request) {
		final RandimiUser requestedUser = checkExistence(userId, redirectAttributes);
		if (requestedUser == null) {
			return "redirect:/users";
		}

		if (requestedUser.getInvitationTimestamp() == null) {
			messageService.addError(redirectAttributes, "users.invitation.error.noInvitation");
			return "redirect:/users";
		}

		model.addAttribute("userId", requestedUser.getId());
		model.addAttribute("username", requestedUser.getUsername());
		model.addAttribute("invitationTimestamp", requestedUser.getInvitationTimestamp().toLocalDateTime());
		model.addAttribute("invitationExpirationTimestamp", randimiUserService.getInvitationExpirationTimestamp(requestedUser).toLocalDateTime());
		model.addAttribute("isInvitationExpired", randimiUserService.isInvitationExpired(requestedUser));
		model.addAttribute("invitationLink", randimiUserService.assembleInvitationLink(request, requestedUser));
		return "/users/invitation";
	}

	@RequestMapping(value = "/invitation", method = RequestMethod.POST)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionUser(authentication, #userId)")
	public String postInvitation(@RequestParam(value = "userId") Long userId,
	                             @RequestParam String action,
	                             RedirectAttributes redirectAttributes,
	                             HttpServletRequest request) {
		if (action.equalsIgnoreCase("cancel")) {
			return "redirect:/users";
		}

		final RandimiUser user = checkExistence(userId, redirectAttributes);
		if (user == null) {
			return "redirect:/users";
		}

		boolean isInvited = randimiUserService.inviteUser(user, request);
		if (isInvited) {
			redirectAttributes.addFlashAttribute("success", messageService.getMessage("invitation.mail.success"));
		} else {
			redirectAttributes.addFlashAttribute("error", messageService.getMessage("invitation.mail.success"));
		}

		userRepository.save(user);
		return "redirect:/users";
	}

	@RequestMapping(value = "/enableUser", method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')")
	public String enableUser(@RequestParam(value = "id", required = true) Long userId, Model model,
	                         final RedirectAttributes ra,
	                         final Authentication authentication) {
		final RandimiUser requestedUser = checkExistence(userId, ra);
		if (requestedUser == null) {
			return "redirect:/users";
		}

		// Check permissions
		final RandimiUser user = ((MyUserDetails) authentication.getPrincipal()).getUser();
		if (!checkPermissions(requestedUser, user, ra)) {
			return "redirect:/users";
		}

		randimiUserService.enableUser(requestedUser);

		return "redirect:/users";
	}

	@RequestMapping(value = "/disableUser", method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')")
	public String disableUser(@RequestParam(value = "id", required = true) Long userId,
	                          final RedirectAttributes ra,
	                          final Authentication authentication) {
		final RandimiUser requestedUser = checkExistence(userId, ra);
		if (requestedUser == null) {
			return "redirect:/users";
		}

		final RandimiUser user = ((MyUserDetails) authentication.getPrincipal()).getUser();
		if (!checkPermissions(requestedUser, user, ra)) {
			return "redirect:/users";
		}

		randimiUserService.disableUser(requestedUser);

		return "redirect:/users";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String editUserDetails(final Model model) {
		if (!model.containsAttribute(ACCOUNT_DETAILS_KEY)) {
			RandimiUser currentUser = ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
			currentUser = userRepository.findById(currentUser.getId()).get();
			model.addAttribute(ACCOUNT_DETAILS_KEY, accountDetailsMapper.toAccountDetailsDTO(currentUser));
		}
		return "/users/edit";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public String saveUserChanges(@RequestParam final String action,
	                              @Valid @ModelAttribute("accountDetails") final AccountDetailsDTO accountDetailsDTO,
	                              final BindingResult result, final RedirectAttributes redirectAttributes) {
		if (action.equalsIgnoreCase("cancel"))
			return "redirect:/";

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute(ACCOUNT_DETAILS_KEY, accountDetailsDTO);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + ACCOUNT_DETAILS_KEY, result);
			redirectAttributes.addFlashAttribute("error", messageService.getMessage("users.edit.error"));
			return "redirect:/users/edit";
		}

		randimiUserService.updateUser(accountDetailsDTO);

		redirectAttributes.addFlashAttribute("success", messageService.getMessage("users.edit.success"));
		return "redirect:/users/edit";
	}

	@RequestMapping(value = "/activate", method = RequestMethod.GET)
	public String getActivationPage(@RequestParam(value = INVITATION_TOKEN_KEY, required = false) String token,
	                                Model model) {
		if (!model.containsAttribute(USER_KEY)) {

			//enables tokenless requests to show proper error page
			if (token != null) {
				// Get current user and transform it to a DTO
				RandimiUser requestedUser = userRepository.findFirstByInvitationToken(token);
				if (requestedUser != null) {
					// 604800000 equals 7 days in milliseconds
					if (randimiUserService.isInvitationExpired(requestedUser)) {
						model.addAttribute("error", messageService.getMessage("users.activate.invitationExpired"));
					} else {
						model.addAttribute(USER_KEY, userMapper.toUserDTO(requestedUser));
					}
				} else {
					model.addAttribute("error", messageService.getMessage("users.activate.noUserForToken"));
				}
			} else {
				model.addAttribute("error", messageService.getMessage("users.activate.noUserForToken"));
			}

		}

		return "/users/activate";
	}

	@RequestMapping(value = "/activate", method = RequestMethod.POST)
	public String activateUser(@RequestParam String action, @Valid @ModelAttribute(USER_KEY) UserDTO userDTO,
	                           BindingResult result, RedirectAttributes ra) {
		if (action.equalsIgnoreCase("cancel")) {
			return "redirect:/";
		}
		userDTO.setStatus(UserEditStatus.ACTIVATION);

		// Validate the user DTO
		userDTOValidator.validate(userDTO, result);

		// Get the requested user
		RandimiUser user = userRepository.findById(userDTO.getId()).get();

		//if error occurs Token is resubmitted to make sure it does not get lost
		if (result.hasErrors()) {
			userDTO.setInvitationToken(user.getInvitationToken());
			ra.addFlashAttribute(USER_KEY, userDTO);
			ra.addAttribute(INVITATION_TOKEN_KEY, userDTO.getInvitationToken());
			ra.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + USER_KEY, result);
			messageService.addError(ra, "general.error.invalidForm");
			return "redirect:/users/activate";
		}

		// Check if the user has already been activated
		if (user.getInvitationTimestamp() == null || user.getInvitationToken() == null) {
			ra.addFlashAttribute(USER_KEY, userDTO);
			ra.addAttribute(INVITATION_TOKEN_KEY, userDTO.getInvitationToken());
			messageService.addError(ra, "users.activate.alreadyActivated");
			return "redirect:/users/activate";
		}

		// Check if the invitation token is valid
		if (randimiUserService.isInvitationExpired(user)) {
			ra.addFlashAttribute(USER_KEY, userDTO);
			ra.addAttribute(INVITATION_TOKEN_KEY, userDTO.getInvitationToken());
			messageService.addError(ra, "users.activate.invitationExpired");
			return "redirect:/users/activate";
		}

		// Set the possibly changed parameters and the new password
		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		user.setEMail(userDTO.getEMail());
		user.setUsername(userDTO.getUsername());
		user.setPassword(userDTO.getPassword());
		// Remove the invitation parameters and enable the account
		user.setInvitationToken(null);
		user.setInvitationTimestamp(null);
		user.setEnabled(true);

		userRepository.save(user);

		ra.addAttribute("success", messageService.getMessage("users.activate.success"));
		return "redirect:/";
	}

	@RequestMapping(value = "/editRoles", method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')")
	public String getEditRolesPage(@RequestParam(value = "id", required = true) Long userId, Model model,
	                               final RedirectAttributes ra,
	                               final Authentication authentication) {
		// Get the requested user and abort if the user does not exist
		final RandimiUser requestedUser = checkExistence(userId, ra);
		if (requestedUser == null) {
			return "redirect:/users";
		}

		// Check permissions
		final RandimiUser user = ((MyUserDetails) authentication.getPrincipal()).getUser();
		if (!checkPermissions(requestedUser, user, ra)) {
			return "redirect:/users";
		}

		// The role api user will be handled separately
		final List<UserRoles> userRoles = new ArrayList<>(Arrays.asList(UserRoles.values()));
		userRoles.remove(UserRoles.ROLE_API_USER);

		// Add the user and the user roles to the model
		model.addAttribute("apiRole", UserRoles.ROLE_API_USER);
		model.addAttribute("user", userMapper.toUserDTO(requestedUser));
		model.addAttribute("userRoles", userRoles);
		return "users/editRoles";
	}

	@RequestMapping(value = "/editRoles", method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')")
	public String editRoles(@RequestParam String action, @Valid @ModelAttribute("user") UserDTO userDTO,
	                        BindingResult result, Model model, final RedirectAttributes ra,
	                        final Authentication authentication) {
		if (action.equalsIgnoreCase("cancel")) {
			return "redirect:/users";
		}

		userDTO.setStatus(UserEditStatus.EDIT_ROLES);

		// Validate the user DTO
		userDTOValidator.validate(userDTO, result);

		if (result.hasErrors()) {
			model.addAttribute("userRoles", UserRoles.values());
			return "/users/editRoles";
		}

		// Get the edited user and abort if the user does not exist
		final RandimiUser editedUser = checkExistence(userDTO.getId(), ra);
		if (editedUser == null) {
			return "redirect:/users";
		}

		// Check permissions
		final RandimiUser user = ((MyUserDetails) authentication.getPrincipal()).getUser();
		if (!checkPermissions(editedUser, user, ra)) {
			return "redirect:/users";
		}

		// Apply the new roles
		userMapper.applyRoles(userDTO, editedUser, user);

		// Save
		userRepository.save(editedUser);

		return "redirect:/users";
	}

	@RequestMapping(value = "/password", method = RequestMethod.GET)
	public String getForgetPasswordPage(Model model) {
		model.addAttribute("user", new UserDTO());
		return "users/password";
	}

	@RequestMapping(value = "/password", method = RequestMethod.POST)
	@Transactional
	public String getResetPasswordMail(@RequestParam String action, @ModelAttribute("user") UserDTO userDTO, BindingResult result, Model model, HttpServletRequest request, RedirectAttributes ra) {
		if (action.equalsIgnoreCase("cancel")) {
			return "redirect:/welcome";
		}

		RandimiUser user = userRepository.findFirstByUsernameIgnoreCase(userDTO.getUsername());

		if (user != null) {
			ForgotPasswordToken forgotPasswordToken = user.getForgotPasswordToken();
			if (forgotPasswordToken == null) {
				forgotPasswordToken = randimiUserService.createForgotPasswordToken(user);
			} else {
				randimiUserService.renewForgotPasswordToken(forgotPasswordToken);
			}
			String resetPasswordLink = request.getRequestURL().subSequence(0, request.getRequestURL().lastIndexOf("/password")) + "/resetPassword?token=" + forgotPasswordToken.getToken();

			final String subject = mailService.assembleMailSubject("password.mail.subject");
			final String content = mailService.assembleMailText("password.mail.content", new Object[]{resetPasswordLink});

			if (mailService.sendSimpleMessage(user.getEMail(), subject, content)) {
				ra.addFlashAttribute("success", messageService.getMessage("password.mail.success"));
			} else {
				ra.addFlashAttribute("error", messageService.getMessage("mail.error"));
			}
		} else {
			ra.addFlashAttribute("error", messageService.getMessage("password.userNameDoesNotExist"));
		}

		return "redirect:/users/password";
	}

	@RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
	public String getResetPasswordPage(
			final @RequestParam(value = "token", required = true) String token,
			final Model model
	) {
		// Get current ForgotPasswordToken and transform it to a DTO
		final ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findFirstByToken(token);

		if (randimiUserService.isForgotPasswordTokenExpired(forgotPasswordToken)) {
			model.addAttribute("error", messageService.getMessage("password.reset.tokenExpiredOrInvalid"));
		} else {
			if (!model.containsAttribute(FORGOT_PASSWORT_TOKEN_KEY)) {
				model.addAttribute(FORGOT_PASSWORT_TOKEN_KEY, forgotPasswordToken.toForgotPasswordTokenDTO());
			}
		}

		return "users/resetPassword";
	}

	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	@Transactional
	public String resetPassword(
			@RequestParam final String action,
			@ModelAttribute(FORGOT_PASSWORT_TOKEN_KEY) final ForgotPasswordTokenDTO forgotPasswordTokenDTO,
			final BindingResult result,
			final RedirectAttributes ra
	) {
		if (action.equalsIgnoreCase("cancel")) {
			return "redirect:/welcome";
		}

		forgotPasswordTokenDTOValidator.validate(forgotPasswordTokenDTO, result);
		if(result.hasErrors()) {
			ra.addAttribute("token", forgotPasswordTokenDTO.getToken());
			ra.addFlashAttribute(FORGOT_PASSWORT_TOKEN_KEY, forgotPasswordTokenDTO.getToken());
			ra.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORGOT_PASSWORT_TOKEN_KEY, result);
			ra.addFlashAttribute("error", messageService.getMessage("general.error.invalidForm"));
			return "redirect:/users/resetPassword";
		}

		final ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findFirstByToken(forgotPasswordTokenDTO.getToken());
		if (randimiUserService.isForgotPasswordTokenExpired(forgotPasswordToken)) {
			ra.addAttribute("error", messageService.getMessage("password.reset.tokenExpiredOrInvalid"));
		} else {
			final RandimiUser user = forgotPasswordToken.getRandimiUser();
			user.setPassword(forgotPasswordTokenDTO.getPassword());
			user.setForgotPasswordToken(null);

			userRepository.save(user);
			forgotPasswordTokenRepository.delete(forgotPasswordToken);
			ra.addAttribute("success", messageService.getMessage("password.reset.success"));
		}

		return "redirect:/welcome";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@PreAuthorize("@customPermissionEvaluator.hasPermissionUser(authentication, #userId)")
	public String deleteUser(@RequestParam(value = "userId", required = true) final Long userId,
	                         final RedirectAttributes redirectAttributes,
	                         final Authentication authentication) {
		final RandimiUser requestedUser = checkExistence(userId, redirectAttributes);
		if (requestedUser == null) {
			return "redirect:/users";
		}

		// Check permissions
		final RandimiUser user = ((MyUserDetails) authentication.getPrincipal()).getUser();
		if (!checkPermissions(requestedUser, user, redirectAttributes)) {
			return "redirect:/users";
		}

		randimiUserService.deleteUser(requestedUser);
		redirectAttributes.addFlashAttribute("success",
		                                     messageService.getMessage("users.monitor.deleteUserPopup.success"));

		return "redirect:/users";
	}

	@Nullable
	private RandimiUser checkExistence(@Nullable final Long userId, final RedirectAttributes ra) {
		if (userId == null) {
			ra.addFlashAttribute("error", messageService.getMessage("users.error.userNotExist"));
			return null;
		}

		Optional<RandimiUser> userOptional = userRepository.findById(userId);
		if (userOptional.isEmpty()) {
			ra.addFlashAttribute("error", messageService.getMessage("users.error.userNotExist"));
			return null;
		}

		return userOptional.get();
	}

	/**
	 * Checks whether the accessing user has the permissions to modify the accessed user.
	 * Adds an error to the given redirect attribute.
	 * @param accessedUser The accessed user which should be modified.
	 * @param accessingUser The accessing user which wants to modify.
	 * @param ra RedirectAttributes.
	 * @return True if the user has the permissions.
	 */
	private boolean checkPermissions(final RandimiUser accessedUser,
	                                 final RandimiUser accessingUser,
	                                 final RedirectAttributes ra) {
		// Users are not allowed to modify their own roles
		if (Objects.equals(accessedUser.getId(), accessingUser.getId())) {
			ra.addAttribute("error", messageService.getMessage("users.editRoles.error.ownRoles"));
			return false;
		}

		// Only admins are allowed to
		if (accessedUser.hasUserRole(UserRoles.ROLE_ADMIN) && !accessingUser.hasUserRole(UserRoles.ROLE_ADMIN)) {
			ra.addAttribute("error", messageService.getMessage("users.edit.error.adminModification"));
			return false;
		}

		return true;
	}
}
