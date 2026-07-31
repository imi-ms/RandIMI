package de.unimuenster.imi.randimi.validator.settings;

import de.unimuenster.imi.randimi.dto.settings.SettingsDTO;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Tobias Brix <tobias.brix@uni-muenster.de>
 */
@Component
public class SettingsDTOValidator extends AbstractValidator {

	private final PseudonymRegexDTOValidator pseudonymRegexDTOValidator;
	private final FooterMessageSettingsDTOValidator footerMessageSettingsDTOValidator;

	@Autowired
	public SettingsDTOValidator(MessageService messageService,
	                            PseudonymRegexDTOValidator pseudonymRegexDTOValidator,
	                            FooterMessageSettingsDTOValidator footerMessageSettingsDTOValidator) {
		super(messageService);
		this.pseudonymRegexDTOValidator = pseudonymRegexDTOValidator;
		this.footerMessageSettingsDTOValidator = footerMessageSettingsDTOValidator;
	}

	@Override
	public boolean supports(Class<?> type) {
		return SettingsDTO.class.isAssignableFrom(type);
	}

	@Override
	public void validate(Object o, Errors errors) {
		SettingsDTO settingsDTO = (SettingsDTO) o;

		if (settingsDTO.getDefaultLanguage() == null) {
			errors.rejectValue("defaultLanguage", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}

		if (settingsDTO.getMailHost() == null || settingsDTO.getMailHost().trim().isEmpty()) {
			errors.rejectValue("mailHost", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}

		if (settingsDTO.getMailPort() == null) {
			errors.rejectValue("mailPort", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} else if (settingsDTO.getMailPort() < 0 || settingsDTO.getMailPort() > 65535) {
			settingsDTO.setMailPort(0);
			errors.rejectValue("mailPort", "errormessage", getMsg("validator.general.mustBeBetween", 0, 65536));
		}

		if (settingsDTO.getMailTLS() == null) {
			errors.rejectValue("mailTLS", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}

		if (settingsDTO.getMailSMTPAuth() == null) {
			errors.rejectValue("mailSMTPAuth", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} else {
			if (settingsDTO.getMailSMTPAuth() &&
			    (settingsDTO.getMailUsername() == null || settingsDTO.getMailUsername().trim().isEmpty())) {
				errors.rejectValue("mailUsername", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
			}

			if (settingsDTO.getMailSMTPAuth() &&
			    (settingsDTO.getMailPassword() == null || settingsDTO.getMailPassword().trim().isEmpty())) {
				errors.rejectValue("mailPassword", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
			}
		}

		if (settingsDTO.getMailSender() == null || settingsDTO.getMailSender().trim().isEmpty()) {
			errors.rejectValue("mailSender", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}

		if (settingsDTO.getSupportMail() == null || settingsDTO.getSupportMail().trim().isEmpty()) {
			errors.rejectValue("supportMail", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}

		if (settingsDTO.getSupportPhone() == null || settingsDTO.getSupportPhone().trim().isEmpty()) {
			errors.rejectValue("supportPhone", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}

		//Validate Layout
		if (settingsDTO.getMainColor() == null || settingsDTO.getMainColor().trim().isEmpty()) {
			errors.rejectValue("mainColor", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}
		if (settingsDTO.getHighlightColor() == null || settingsDTO.getHighlightColor().trim().isEmpty()) {
			errors.rejectValue("highlightColor", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}
		if (settingsDTO.getBackgroundColor() == null || settingsDTO.getBackgroundColor().trim().isEmpty()) {
			errors.rejectValue("backgroundColor", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}
		if (settingsDTO.getMainTextColor() == null || settingsDTO.getMainTextColor().trim().isEmpty()) {
			errors.rejectValue("mainTextColor", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}
		if (settingsDTO.getHighlightTextColor() == null || settingsDTO.getHighlightTextColor().trim().isEmpty()) {
			errors.rejectValue("highlightTextColor", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}

		//Validate Lists
		for (int i = 0; i < settingsDTO.getPseudonymRegexList().size(); i++) {
			errors.pushNestedPath("pseudonymRegexList[" + i + "]");
			pseudonymRegexDTOValidator.validate(settingsDTO.getPseudonymRegexList().get(i), errors);
			errors.popNestedPath();
		}
		for (int i = 0; i < settingsDTO.getFooterMessageSettingsList().size(); i++) {
			errors.pushNestedPath("footerMessageSettingsList[" + i + "]");
			footerMessageSettingsDTOValidator.validate(settingsDTO.getFooterMessageSettingsList().get(i), errors);
			errors.popNestedPath();
		}

	}
}
