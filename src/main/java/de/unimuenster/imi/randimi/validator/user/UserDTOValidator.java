package de.unimuenster.imi.randimi.validator.user;

import de.unimuenster.imi.randimi.dto.user.UserDTO;
import de.unimuenster.imi.randimi.model.enumeration.UserRoles;
import de.unimuenster.imi.randimi.model.user.MyUserDetails;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;

import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Optional;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class UserDTOValidator extends AbstractValidator {

	public static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"; // Wtf. Tebi...

	private final RandimiUserRepository userRepository;

	@Autowired
	public UserDTOValidator(MessageService messageService, RandimiUserRepository userRepository) {
		super(messageService);
		this.userRepository = userRepository;
	}

	@Override
	public boolean supports(Class<?> type) {
		return UserDTO.class.isAssignableFrom(type);
	}

	@Override
	public void validate(Object o, Errors errors) {
		UserDTO userDTO = (UserDTO) o;

		switch (userDTO.getStatus()) {
			case INVITATION:
				validateInvitation(userDTO, errors);
				break;
			case ACTIVATION:
				validateActivation(userDTO, errors);
				break;
			case EDIT:
				validateEdit(userDTO, errors);
				break;
			case EDIT_ROLES:
				validateEditRoles(userDTO, errors);
				break;
			case ADMIN_EDIT:

				break;
			default:
				break;
		}
	}


	private void validateInvitation(UserDTO userDTO, Errors errors) {
		validateFirstName(userDTO, errors);
		validateLastName(userDTO, errors);
		validateUsername(userDTO, errors);
		validateEmail(userDTO, errors);

		validateRoles(userDTO, errors);

		if (userDTO.isSkipEMailValidation())
			validatePassword(userDTO, errors);
	}

	private void validateActivation(UserDTO userDTO, Errors errors) {
		if (userDTO.getId() == 0) {
			errors.rejectValue("id", "errormessage", getMsg("validator.general.mustNotBeZero"));
		} else if (!userRepository.existsById(userDTO.getId())) {
			errors.rejectValue("id", "errormessage", "Id was not assigned yet!");
		}

		validateFirstName(userDTO, errors);
		validateLastName(userDTO, errors);
		validateEmail(userDTO, errors);
		validatePassword(userDTO, errors);
		validateUsername(userDTO, errors);
	}


	private void validateEdit(UserDTO userDTO, Errors errors) {
		validateFirstName(userDTO, errors);
		validateLastName(userDTO, errors);
		validateEmail(userDTO, errors);
		validatePassword(userDTO, errors);
	}

	private void validateEditRoles(UserDTO userDTO, Errors errors) {
		validateRoles(userDTO, errors);
	}

	private void validatePassword(UserDTO userDTO, Errors errors) {
		String password = userDTO.getPassword();
		if (password != null && !password.isEmpty()) {
			if (password.length() < 8) {
				errors.rejectValue("password", "errormessage", getMsg("validator.user.password.mustBeLongerThan", 8));
			} else if (!password.equals(userDTO.getRepeatPassword())) {
				errors.rejectValue("repeatPassword", "errormessage", getMsg("validator.user.passwordMismatch"));
			}
		} else {
			errors.rejectValue("password", "errormessage", getMsg("validator.user.password.mustNotBeEmpty"));
		}
	}

	private void validateEmail(UserDTO userDTO, Errors errors) {
		String email = userDTO.getEMail();
		if (email == null || email.trim().isEmpty()) {
			errors.rejectValue("eMail", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} else if (email.length() > 255) {
			errors.rejectValue("eMail", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));
		} else if (!email.matches(EMAIL_REGEX)) {
			errors.rejectValue("eMail", "errormessage", getMsg("validator.user.mailInvalid"));
		}
	}

	private void validateLastName(UserDTO userDTO, Errors errors) {
		String lastName = userDTO.getLastName();
		if (lastName == null || lastName.trim().isEmpty()) {
			errors.rejectValue("lastName", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} else if (lastName.length() > 255) {
			errors.rejectValue("lastName", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));
		}
	}

	private void validateFirstName(UserDTO userDTO, Errors errors) {
		String firstName = userDTO.getFirstName();
		if (firstName == null || firstName.trim().isEmpty()) {
			errors.rejectValue("firstName", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} else if (firstName.length() > 255) {
			errors.rejectValue("firstName", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));
		}
	}

	private void validateUsername(UserDTO userDTO, Errors errors) {
		String username = userDTO.getUsername();
		if (username == null || username.trim().isEmpty()) {
			errors.rejectValue("username", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} else if (username.length() > 255) {
			errors.rejectValue("username", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));
		} else if (username.contains("@")) {
			errors.rejectValue("username", "errormessage", getMsg("validator.user.username.mustNotContainAt"));
		} else if (userRepository.doesUsernameAlreadyExist(username, userDTO.getId())) {
			errors.rejectValue("username", "errormessage", getMsg("validator.general.mustBeUnique"));
		}
	}

	private void validateRoles(final UserDTO userDTO, final Errors errors) {
		Optional<RandimiUser> editedUser = userRepository.findById(userDTO.getId());
		RandimiUser currentUser = ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication()
		                                                                .getPrincipal()).getUser();
		// Only admins should be able to give the admin role
		if (nonAdminWantToBecomeAdmin(userDTO, currentUser)) {
			errors.rejectValue("userRoles", "errormessage", getMsg("validator.user.adminCreationError"));
		}

		// Non-admins are not allowed to remove admin status
		if (editedUser.isPresent() && !currentUser.hasRole(UserRoles.ROLE_ADMIN)
		    && editedUser.get().hasRole(UserRoles.ROLE_ADMIN)) {
			errors.rejectValue("userRoles", "errormessage", getMsg("validator.user.adminRemovingError"));
		}
	}

	private boolean nonAdminWantToBecomeAdmin(UserDTO userDTO, RandimiUser currentUser) {
		boolean isAdmin = currentUser.hasRole(UserRoles.ROLE_ADMIN);
		return !isAdmin && userDTO.getUserRoles().contains("ROLE_ADMIN");
	}
}
