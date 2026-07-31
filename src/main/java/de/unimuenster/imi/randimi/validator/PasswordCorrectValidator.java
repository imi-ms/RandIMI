package de.unimuenster.imi.randimi.validator;

import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import de.unimuenster.imi.randimi.model.user.RandimiUser;
import de.unimuenster.imi.randimi.repository.user.RandimiUserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordCorrectValidator implements ConstraintValidator<PasswordCorrect, AccountDetailsDTO> {

	private final PasswordEncoder passwordEncoder;
	private final RandimiUserRepository randimiUserRepository;

	private String passwordFieldName;

	@Autowired
	public PasswordCorrectValidator(PasswordEncoder passwordEncoder, RandimiUserRepository randimiUserRepository) {
		this.passwordEncoder = passwordEncoder;
		this.randimiUserRepository = randimiUserRepository;
	}

	@Override public void initialize(PasswordCorrect constraintAnnotation) {
		this.passwordFieldName = constraintAnnotation.passwordFieldName();
	}

	@Override
	public boolean isValid(AccountDetailsDTO value, ConstraintValidatorContext context) {
		final RandimiUser randimiUser = randimiUserRepository.findById(value.getId()).get();
		final String password = (String) new BeanWrapperImpl(value).getPropertyValue(passwordFieldName);

		if (!passwordEncoder.matches(password, randimiUser.getPassword())) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
			       .addPropertyNode("oldPassword").addConstraintViolation();
			return false;
		}

		return true;
	}
}
