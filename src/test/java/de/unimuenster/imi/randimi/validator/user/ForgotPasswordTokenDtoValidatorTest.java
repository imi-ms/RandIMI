package de.unimuenster.imi.randimi.validator.user;

import de.unimuenster.imi.randimi.dto.user.ForgotPasswordTokenDTO;
import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.ForgotPasswordTokenRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;

import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.*;

import java.util.Optional;

/**
 * @author Paul Schaub
 */
public class ForgotPasswordTokenDtoValidatorTest extends ValidatorTestBase {

    private RandimiUserRepository userRepository;
    private ForgotPasswordTokenRepository tokenRepository;
    private ForgotPasswordTokenDTOValidator validator;

    @BeforeEach
    public void mockValidator() {
        userRepository = mock(RandimiUserRepository.class);
        tokenRepository = mock(ForgotPasswordTokenRepository.class);
        validator = new ForgotPasswordTokenDTOValidator(messageService, userRepository, tokenRepository);

        // Token1 with User1
        RandimiUser user1 = new RandimiUser();
        user1.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        ForgotPasswordToken token1 = new ForgotPasswordToken(user1);
        token1.setToken("foo");
        when(tokenRepository.findById(1L)).thenReturn(Optional.of(token1));

        // Token2 with User1!
        // Use this to test mismatch
        RandimiUser user2 = new RandimiUser();
        user2.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
    }

    @Test
    public void validateNullPassword() {
        Errors errors = mock(Errors.class);

        ForgotPasswordTokenDTO dto = new ForgotPasswordTokenDTO();
        dto.setId(1);
        dto.setToken("foo");

        validator.validate(dto, errors);

        verify(errors).rejectValue("password", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateEmptyPassword() {
        Errors errors = mock(Errors.class);

        ForgotPasswordTokenDTO dto = new ForgotPasswordTokenDTO();
        dto.setId(1);
        dto.setToken("foo");
        dto.setPassword("  ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("password", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validatePasswordTooShort() {
        Errors errors = mock(Errors.class);

        ForgotPasswordTokenDTO dto = new ForgotPasswordTokenDTO();
        dto.setId(1);
        dto.setToken("foo");
        dto.setPassword("short");

        validator.validate(dto, errors);

        verify(errors).rejectValue("password", "errormessage", getMsg("validator.general.mustBeLongerThanChars", 8));
    }

    @Test
    public void validateRepeatPassword() {
        Errors errors = mock(Errors.class);

        ForgotPasswordTokenDTO dto = new ForgotPasswordTokenDTO();
        dto.setId(1);
        dto.setToken("foo");
        dto.setPassword("problemsInP");
        dto.setRepeatPassword("problemsInNP");

        validator.validate(dto, errors);

        verify(errors).rejectValue("repeatPassword", "errormessage", getMsg("validator.forgotPasswordToken.passwordMismatch"));
    }

    @Test
    public void validateTokenMismatch() {
        Errors errors = mock(Errors.class);

        ForgotPasswordTokenDTO dto = new ForgotPasswordTokenDTO();
        dto.setId(1);
        dto.setToken("bar");
        dto.setPassword("sw0rdfish");
        dto.setRepeatPassword("sw0rdfish");

        validator.validate(dto, errors);

        verify(errors).rejectValue("token", "errormessage", getMsg("validator.forgotPasswordToken.tokenMismatch"));
    }

    @Test
    public void validateUserMismatch() {
        Errors errors = mock(Errors.class);

        ForgotPasswordTokenDTO dto = new ForgotPasswordTokenDTO();
        dto.setId(1);
        dto.setUserId(2);
        dto.setToken("foo");
        dto.setPassword("sw0rdfish");
        dto.setRepeatPassword("sw0rdfish");

        validator.validate(dto, errors);

        verify(errors).rejectValue("token", "errormessage", getMsg("validator.forgotPasswordToken.userMismatch"));
    }
}
