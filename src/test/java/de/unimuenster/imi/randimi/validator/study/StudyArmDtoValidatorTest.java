package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.StudyArmDTO;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Paul Schaub
 */
public class StudyArmDtoValidatorTest extends ValidatorTestBase {

    @Autowired
    StudyArmDTOValidator validator;

    @Test
    public void validateValid() {
        Errors errors = mock(Errors.class);
        StudyArmDTO dto = getValidStudyArmDTO();

        validator.validate(dto, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    public void validateNamesValidator() {
        Errors errors = mock(Errors.class);
        StudyArmDTO dto = getValidStudyArmDTO();
        dto.setGuiName(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.nameEmpty"));
    }

    @Test
    public void validateRatioNull() {
        Errors errors = mock(Errors.class);
        StudyArmDTO dto = getValidStudyArmDTO();
        dto.setRatio(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("ratio", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateRatioZero() {
        Errors errors = mock(Errors.class);
        StudyArmDTO dto = getValidStudyArmDTO();
        dto.setRatio(0);

        validator.validate(dto, errors);

        verify(errors).rejectValue("ratio", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 1));
    }

    @Test
    public void validateRatioNegative() {
        Errors errors = mock(Errors.class);
        StudyArmDTO dto = getValidStudyArmDTO();
        dto.setRatio(0);

        validator.validate(dto, errors);

        verify(errors).rejectValue("ratio", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 1));
    }

    private StudyArmDTO getValidStudyArmDTO() {
        StudyArmDTO studyArmDTO = new StudyArmDTO();
        studyArmDTO.setGuiName("Name");
        studyArmDTO.setApiId("Name");
        studyArmDTO.setUseApiId(true);
        studyArmDTO.setRatio(1);
        return studyArmDTO;
    }
}
