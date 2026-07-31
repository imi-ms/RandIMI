package de.unimuenster.imi.randimi.validator.user;

import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsernameConstraint, AccountDetailsDTO> {

	private final RandimiUserRepository userRepository;

	@Autowired
	public UniqueUsernameValidator(final RandimiUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public boolean isValid(final AccountDetailsDTO value, final ConstraintValidatorContext context) {
		RandimiUser randimiUser = userRepository.findFirstByUsernameIgnoreCase(value.getUsername());

		var isValid = true;
		if (randimiUser != null && value.getId() != randimiUser.getId()) {
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
			       .addPropertyNode("username")
			       .addConstraintViolation()
			       .disableDefaultConstraintViolation();

			isValid = false;
		}

		return isValid;
	}
}
