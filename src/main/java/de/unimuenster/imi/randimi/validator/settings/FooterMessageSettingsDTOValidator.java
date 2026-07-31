package de.unimuenster.imi.randimi.validator.settings;

import de.unimuenster.imi.randimi.dto.settings.FooterMessageSettingsDTO;
import de.unimuenster.imi.randimi.service.MessageService;
import de.unimuenster.imi.randimi.validator.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Component
public class FooterMessageSettingsDTOValidator extends AbstractValidator {

	@Autowired
	public FooterMessageSettingsDTOValidator(MessageService messageService) {
		super(messageService);
	}

	@Override
	public boolean supports(Class<?> type) {
		return FooterMessageSettingsDTO.class.isAssignableFrom(type);
	}

	@Override
	public void validate(Object o, Errors errors) {
		FooterMessageSettingsDTO footerMessageSettingsDTO = (FooterMessageSettingsDTO) o;

		if (footerMessageSettingsDTO.getCurrentLanguage() == null) {
			errors.rejectValue("currentLanguage", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		} 

		if (footerMessageSettingsDTO.getImprintContent() == null || footerMessageSettingsDTO.getImprintContent().trim().isEmpty()) {
			footerMessageSettingsDTO.setImprintContent("");
			errors.rejectValue("imprintContent", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}
		
		if (footerMessageSettingsDTO.getDataPrivacyContent() == null || footerMessageSettingsDTO.getDataPrivacyContent().trim().isEmpty()) {
			footerMessageSettingsDTO.setDataPrivacyContent("");
			errors.rejectValue("dataPrivacyContent", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
		}
		
		if (footerMessageSettingsDTO.getSupportContent() == null || footerMessageSettingsDTO.getSupportContent().trim().isEmpty()) {
				footerMessageSettingsDTO.setSupportContent("");
				errors.rejectValue("supportContent", "errormessage", getMsg("validator.general.mustNotBeEmpty"));
			}

		//content can be empty
	}
}
