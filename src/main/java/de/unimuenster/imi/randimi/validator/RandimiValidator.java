package de.unimuenster.imi.randimi.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class RandimiValidator implements Validator {

	@Override
	public boolean supports(Class<?> type) {
		return true;
	}

	@Override
	public void validate(Object o, Errors errors) {
	}

}
