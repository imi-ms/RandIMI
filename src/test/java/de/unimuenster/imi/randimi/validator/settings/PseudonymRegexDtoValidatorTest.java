package de.unimuenster.imi.randimi.validator.settings;

import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDTO;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Paul Schaub
 */
public class PseudonymRegexDtoValidatorTest extends ValidatorTestBase {

    @Autowired
    PseudonymRegexDTOValidator validator;

    @Test
    public void validateOrderNumberZero() {
        Errors errors = mock(Errors.class);
        PseudonymRegexDTO dto = getValidPseudonymRegexDTO();
        dto.setOrderNumber(-1);

        validator.validate(dto, errors);

        verify(errors).rejectValue("orderNumber", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 0));
    }

    @Test
    public void validateRegexNull() {
        Errors errors = mock(Errors.class);
        PseudonymRegexDTO dto = getValidPseudonymRegexDTO();
        dto.setRegex(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("regex", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateRegexEmpty() {
        Errors errors = mock(Errors.class);
        PseudonymRegexDTO dto = getValidPseudonymRegexDTO();
        dto.setRegex("\t "); // whitespace only

        validator.validate(dto, errors);

        verify(errors).rejectValue("regex", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateRegexTooLong() {
        Errors errors = mock(Errors.class);
        PseudonymRegexDTO dto = getValidPseudonymRegexDTO();
        dto.setRegex("\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d"); // whitespace only

        validator.validate(dto, errors);

        verify(errors).rejectValue("regex", "errormessage", getMsg("validator.general.mustNotBeLongerThanChars", 255));
    }

    @Test
    public void validateRegexInvalid() {
        Errors errors = mock(Errors.class);
        PseudonymRegexDTO dto = getValidPseudonymRegexDTO();
        dto.setRegex("[");

        validator.validate(dto, errors);

        verify(errors).rejectValue("regex", "errormessage", getMsg("validator.regex.invalid"));
    }

    private PseudonymRegexDTO getValidPseudonymRegexDTO() {
        PseudonymRegexDTO dto = new PseudonymRegexDTO();
        dto.setOrderNumber(1);
        dto.getPseudonymRegexDescriptionDTOList().add(PseudonymRegexDescriptionValidatorTest.getValidPseudonymRegexDescriptionDTO());
        dto.setRegex("\\d");
        return dto;
    }
}
