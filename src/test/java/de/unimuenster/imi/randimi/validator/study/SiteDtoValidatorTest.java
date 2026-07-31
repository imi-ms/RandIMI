package de.unimuenster.imi.randimi.validator.study;

import de.unimuenster.imi.randimi.dto.study.SiteDTO;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SiteDtoValidatorTest extends ValidatorTestBase {

    @Autowired
    SiteDTOValidator validator;

    public static final long SITE_ID = 1L;

    public static SiteDTO getValidSiteDto() {
        SiteDTO siteDTO = new SiteDTO();
        siteDTO.setId(SITE_ID);
        siteDTO.setApiId("Test");
        siteDTO.setUseApiId(true);
        siteDTO.setSeed(123L);
        siteDTO.setCapacity(240);
        siteDTO.setGuiName("Test");
        siteDTO.setOrderNumber(0);
        siteDTO.setPseudonymRegex(".*");
        return siteDTO;
    }

    @Test
    public void validateNamesValidator() {
        Errors errors = mock(Errors.class);
        SiteDTO dto = getValidSiteDto();
        dto.setGuiName(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("guiName", "errormessage", getMsg("validator.general.nameEmpty"));
    }

    @Test
    public void validateSeedNull() {
    	Errors errors = mock(Errors.class);
    	SiteDTO dto = getValidSiteDto();
    	dto.setSeed(null);
    	
    	validator.validate(dto, errors);
    	
    	verify(errors).rejectValue("seed", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateCapacityNull() {
        Errors errors = mock(Errors.class);
        SiteDTO dto = getValidSiteDto();
        dto.setCapacity(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.general.mustNotBeNull"));
    }

    @Test
    public void validateCapacityZero() {
        Errors errors = mock(Errors.class);
        SiteDTO dto = getValidSiteDto();
        dto.setCapacity(0);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.general.mustNotBeZero"));
    }

    @Test
    public void validateCapacityNegative() {
        Errors errors = mock(Errors.class);
        SiteDTO dto = getValidSiteDto();
        dto.setCapacity(-12);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 2));
    }

    @Test
    public void validateCapacityTooSmall() {
        Errors errors = mock(Errors.class);
        SiteDTO dto = getValidSiteDto();
        dto.setCapacity(1);

        validator.validate(dto, errors);

        verify(errors).rejectValue("capacity", "errormessage", getMsg("validator.general.mustBeGreaterThanOrEqualTo", 2));
    }

    @Test
    public void validatePseudonymRegexNull() {
        Errors errors = mock(Errors.class);
        SiteDTO dto = getValidSiteDto();
        dto.setPseudonymRegex(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("pseudonymRegex", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validatePseudonymRegexEmpty() {
        Errors errors = mock(Errors.class);
        SiteDTO dto = getValidSiteDto();
        dto.setPseudonymRegex(" ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("pseudonymRegex", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }
    
    @Test
    public void validatePseudonymRegexInvalid() {
        Errors errors = mock(Errors.class);
        SiteDTO dto = getValidSiteDto();
        dto.setPseudonymRegex("[");

        validator.validate(dto, errors);

        verify(errors).rejectValue("pseudonymRegex", "errormessage", getMsg("validator.regex.invalid"));
    }

}
