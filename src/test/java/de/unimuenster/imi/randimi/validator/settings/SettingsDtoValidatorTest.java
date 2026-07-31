package de.unimuenster.imi.randimi.validator.settings;

import de.unimuenster.imi.randimi.dto.settings.SettingsDTO;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * @author Paul Schaub
 */
public class SettingsDtoValidatorTest extends ValidatorTestBase {

    @Autowired
    SettingsDTOValidator validator;

    @Test
    public void validateValidDto() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();

        validator.validate(dto, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), anyString());
    }

    private SettingsDTO getValidDto() {
        SettingsDTO dto = new SettingsDTO();
        dto.setId(1L);
        dto.setDefaultLanguage(SupportedLanguage.ENGLISH);
        dto.setMailHost("secmail.uni-muenster.de");
        dto.setMailPort(567);
        dto.setMailTLS(true);
        dto.setMailSMTPAuth(true);
        dto.setMailUsername("sendmail");
        dto.setMailPassword("s3cr3t");
        dto.setMailSender("Send van Mail");
        dto.setSupportMail("support@rand.imi");
        dto.setSupportPhone("012345678910");
		//layout
		dto.setMainColor("#00632e");
		dto.setHighlightColor("#97bf0d");
		dto.setBackgroundColor("#ffffff");
        dto.setMainTextColor("#000000");
		dto.setHighlightTextColor("#ffffff");
		//lists
		dto.setPseudonymRegexList(new ArrayList<>());
		dto.setFooterMessageSettingsList(new ArrayList<>());

        return dto;
    }

    @Test
    public void validateNullLanguage() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setDefaultLanguage(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("defaultLanguage", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailHostNull() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailHost(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailHost", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailHostEmpty() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailHost(" ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailHost", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailPortNegative() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailPort(-12);

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailPort", "errormessage", getMsg("validator.general.mustBeBetween", 0, 65536));
    }

    @Test
    public void validateMailPortTooLarge() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailPort(65537);

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailPort", "errormessage", getMsg("validator.general.mustBeBetween", 0, 65536));
    }

    @Test
    public void validateMailTLSNull() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailTLS(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailTLS", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailSMTPAuthNull() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailSMTPAuth(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailSMTPAuth", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailUsernameNull() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailSMTPAuth(true);
        dto.setMailUsername(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailUsername", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailUsernameEmpty() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailSMTPAuth(true);
        dto.setMailUsername(" ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailUsername", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailPasswordNull() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailSMTPAuth(true);
        dto.setMailPassword(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailPassword", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailPasswordEmpty() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailSMTPAuth(true);
        dto.setMailPassword(" ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailPassword", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailSenderNull() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailSender(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailSender", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateMailSenderEmpty() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setMailSender(" ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("mailSender", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateSupportMailNull() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setSupportMail(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("supportMail", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateSupportMailEmpty() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setSupportMail(" ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("supportMail", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateSupportPhoneNull() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setSupportPhone(null);

        validator.validate(dto, errors);

        verify(errors).rejectValue("supportPhone", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }

    @Test
    public void validateSupportPhoneEmpty() {
        Errors errors = mock(Errors.class);
        SettingsDTO dto = getValidDto();
        dto.setSupportPhone(" ");

        validator.validate(dto, errors);

        verify(errors).rejectValue("supportPhone", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
    }
}
