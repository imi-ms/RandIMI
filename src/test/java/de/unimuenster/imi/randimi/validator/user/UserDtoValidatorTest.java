package de.unimuenster.imi.randimi.validator.user;

import de.unimuenster.imi.randimi.dto.user.UserDTO;
import de.unimuenster.imi.randimi.model.enumeration.UserEditStatus;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.validation.Errors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Daniel Preciado-Marquez
 */
@WithUserDetails(value = "admin", userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
public class UserDtoValidatorTest extends ValidatorTestBase {

	private UserDTOValidator validator;
	private Errors errors;

	@BeforeEach
	public void mockValidator() {
		RandimiUserRepository randimiUserRepository = mock(RandimiUserRepository.class);
		validator = new UserDTOValidator(messageService, randimiUserRepository);
		errors = mock(Errors.class);
	}

	@Test
	public void validInvitationUser() {
		UserDTO user = getValidInvitationUser();
		validator.validate(user, errors);
		verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
	}

	@Test
	public void validUsernameContainsAt() {
		UserDTO user = getValidInvitationUser();
		user.setUsername("user@name");

		validator.validate(user, errors);

		verify(errors).rejectValue("username", "errormessage", getMsg("validator.user.username.mustNotContainAt"));
	}

	public UserDTO getValidInvitationUser() {
		UserDTO user = new UserDTO();
		user.setId(0);
		user.setUsername("blau");
		user.setPassword("changeme");
		user.setRepeatPassword("changeme");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setEMail("email@mail.de");
		user.setStatus(UserEditStatus.INVITATION);
		user.setSkipEMailValidation(true);
		return user;
	}

}
