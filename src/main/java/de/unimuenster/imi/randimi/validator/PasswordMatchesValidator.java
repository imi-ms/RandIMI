package de.unimuenster.imi.randimi.validator;

import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, AccountDetailsDTO> {

	@Override
	public boolean isValid(AccountDetailsDTO value, ConstraintValidatorContext context) {
		if (!value.getNewPassword().equals(value.getRepeatPassword())) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
			       .addPropertyNode("repeatPassword").addConstraintViolation();
			return false;
		}

		return true;
	}
}
