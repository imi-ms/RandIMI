package de.unimuenster.imi.randimi.validator;

import de.unimuenster.imi.randimi.service.MessageService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class PasswordStrongValidator implements ConstraintValidator<PasswordStrong, String> {

	private static final int MIN_PASSWORD_LENGTH = 8;

	private final MessageService messageService;

	@Autowired
	public PasswordStrongValidator(MessageService messageService) {
		this.messageService = messageService;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value.length() < MIN_PASSWORD_LENGTH) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
					       messageService.getMessage("validator.user.password.mustBeLongerThan", MIN_PASSWORD_LENGTH))
			       .addConstraintViolation();
			return false;
		}

		return true;
	}
}
