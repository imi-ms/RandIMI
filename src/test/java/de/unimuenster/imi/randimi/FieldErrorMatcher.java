package de.unimuenster.imi.randimi;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FieldErrorMatcher extends TypeSafeMatcher<BeanPropertyBindingResult> {

	private final Map<String, String> expectedFieldErrors;
	private final Set<String> missingFieldErrors;
	private final Map<String, String> additionalFieldErrors;
	private final Map<String, String> wrongFieldErrors;
	private final Map<String, String> foundFieldErrors;

	public FieldErrorMatcher(Map<String, String> expectedFieldErrors) {
		this.expectedFieldErrors = expectedFieldErrors;

		missingFieldErrors = new HashSet<>();
		missingFieldErrors.addAll(expectedFieldErrors.keySet());

		additionalFieldErrors = new HashMap<>();
		foundFieldErrors = new HashMap<>();
		wrongFieldErrors = new HashMap<>();
	}

	@Override
	protected boolean matchesSafely(BeanPropertyBindingResult item) {
		for (FieldError fieldError : item.getFieldErrors()) {
			final String fieldName = fieldError.getField();
			final String message = fieldError.getDefaultMessage();

			if (!expectedFieldErrors.containsKey(fieldName)) {
				additionalFieldErrors.put(fieldName, message);
				continue;
			}

			missingFieldErrors.remove(fieldName);

			final String expectedMessage = expectedFieldErrors.get(fieldName);
			if (!expectedMessage.equals(message)) {
				wrongFieldErrors.put(fieldName, message);
				continue;
			}

			foundFieldErrors.put(fieldName, message);
		}

		return missingFieldErrors.isEmpty() && additionalFieldErrors.isEmpty() && wrongFieldErrors.isEmpty();
	}

	@Override
	protected void describeMismatchSafely(BeanPropertyBindingResult item, Description mismatchDescription) {
		mismatchDescription.appendText("Found field errors: ");
		mismatchDescription.appendValueList("[", ",", "]", foundFieldErrors.entrySet());
		mismatchDescription.appendText(", Missing field errors: ");
		mismatchDescription.appendValueList("[", ",", "]", missingFieldErrors);
		mismatchDescription.appendText(", Additional field errors: ");
		mismatchDescription.appendValueList("[", ",", "]", additionalFieldErrors.entrySet());
		mismatchDescription.appendText(", Wrong field errors: ");
		mismatchDescription.appendValueList("[", ",", "]", wrongFieldErrors.entrySet());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Found field errors: ");
		description.appendValueList("[", ",", "]", expectedFieldErrors.entrySet());
		description.appendText(", Missing field errors: ");
		description.appendValueList("[", ",", "]");
		description.appendText(", Additional field errors: ");
		description.appendValueList("[", ",", "]");
		description.appendText(", Wrong field errors: ");
		description.appendValueList("[", ",", "]");
	}
}
