package de.unimuenster.imi.randimi.validator.user;

import de.unimuenster.imi.randimi.dto.user.ForgotPasswordTokenDTO;
import de.unimuenster.imi.randimi.model.user.ForgotPasswordToken;
import de.unimuenster.imi.randimi.repository.user.ForgotPasswordTokenRepository;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;

import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class ForgotPasswordTokenDTOValidator extends AbstractValidator {

	private final RandimiUserRepository userRepository;
	private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;

	@Autowired
	public ForgotPasswordTokenDTOValidator(MessageService messageService,
	                                       RandimiUserRepository userRepository,
	                                       ForgotPasswordTokenRepository forgotPasswordTokenRepository) {
		super(messageService);
		this.userRepository = userRepository;
		this.forgotPasswordTokenRepository = forgotPasswordTokenRepository;
	}

	@Override
	public boolean supports(Class<?> type) {
		return ForgotPasswordTokenDTO.class.isAssignableFrom(type);
	}
	
	@Override
	public void validate(Object o, Errors errors) {
		ForgotPasswordTokenDTO forgotPasswordTokenDTO = (ForgotPasswordTokenDTO) o;
		
		// Password
		String password = forgotPasswordTokenDTO.getPassword();
		if (password == null || password.trim().isEmpty()) {
			errors.rejectValue("password", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} else {
			if (password.length() < 8) {
				forgotPasswordTokenDTO.setPassword("");
				forgotPasswordTokenDTO.setRepeatPassword("");
				errors.rejectValue("password", "errormessage", getMsg("validator.general.mustBeLongerThanChars", 8));
			} else if (repeatPasswordDiffers(forgotPasswordTokenDTO)) {
				errors.rejectValue("repeatPassword", "errormessage", getMsg("validator.forgotPasswordToken.passwordMismatch"));
			}
		}

		// Token
		ForgotPasswordToken token = forgotPasswordTokenRepository.findById(forgotPasswordTokenDTO.getId()).get();
		if (!token.getToken().equals(forgotPasswordTokenDTO.getToken())) {
			errors.rejectValue("token", "errormessage", getMsg("validator.forgotPasswordToken.tokenMismatch"));
		}
		
		// RandimiUser
		if (!token.getRandimiUser().equals(userRepository.findById(forgotPasswordTokenDTO.getUserId()).orElse(null))) {
			errors.rejectValue("token", "errormessage", getMsg("validator.forgotPasswordToken.userMismatch"));
		}
	}

	private boolean repeatPasswordDiffers(ForgotPasswordTokenDTO forgotPasswordTokenDTO) {
		return !forgotPasswordTokenDTO.getPassword().equals(forgotPasswordTokenDTO.getRepeatPassword());
	}
}
