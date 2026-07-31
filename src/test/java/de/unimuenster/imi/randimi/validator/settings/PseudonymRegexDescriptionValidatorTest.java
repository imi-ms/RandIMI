package de.unimuenster.imi.randimi.validator.settings;

import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDescriptionDTO;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PseudonymRegexDescriptionValidatorTest extends ValidatorTestBase {

	@Autowired
	private PseudonymRegexDescriptionDTOValidator validator;

	@Test
	public void validateCurrentLanguageNull() {
		final Errors errors = mock(Errors.class);
		final PseudonymRegexDescriptionDTO dto = getValidPseudonymRegexDescriptionDTO();
		dto.setCurrentLanguage(null);

		validator.validate(dto, errors);

		verify(errors).rejectValue("currentLanguage", "errormessage", getMsg("validator.general.mustNotBeNull"));
	}

	@Test
	public void validateDescriptionNull() {
		final Errors errors = mock(Errors.class);
		final PseudonymRegexDescriptionDTO dto = getValidPseudonymRegexDescriptionDTO();
		dto.setDescription(null);

		validator.validate(dto, errors);

		verify(errors).rejectValue("description", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
	}

	@Test
	public void validateDescriptionEmpty() {
		final Errors errors = mock(Errors.class);
		final PseudonymRegexDescriptionDTO dto = getValidPseudonymRegexDescriptionDTO();
		dto.setDescription("");

		validator.validate(dto, errors);

		verify(errors).rejectValue("description", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
	}

	@Test
	public void validateNameNull() {
		final Errors errors = mock(Errors.class);
		final PseudonymRegexDescriptionDTO dto = getValidPseudonymRegexDescriptionDTO();
		dto.setName(null);

		validator.validate(dto, errors);

		verify(errors).rejectValue("name", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
	}

	@Test
	public void validateNameEmpty() {
		Errors errors = mock(Errors.class);
		final PseudonymRegexDescriptionDTO dto = getValidPseudonymRegexDescriptionDTO();
		dto.setName("   ");

		validator.validate(dto, errors);

		verify(errors).rejectValue("name", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
	}

	@Test
	public void validateNameTooLong() {
		Errors errors = mock(Errors.class);
		final PseudonymRegexDescriptionDTO dto = getValidPseudonymRegexDescriptionDTO();
		dto.setName("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");

		validator.validate(dto, errors);

		verify(errors).rejectValue("name", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));
	}

	public static PseudonymRegexDescriptionDTO getValidPseudonymRegexDescriptionDTO() {
		final PseudonymRegexDescriptionDTO dto = new PseudonymRegexDescriptionDTO();
		dto.setCurrentLanguage(SupportedLanguage.ENGLISH);
		dto.setDescription("foo");
		dto.setName("foo");
		return dto;
	}

}
