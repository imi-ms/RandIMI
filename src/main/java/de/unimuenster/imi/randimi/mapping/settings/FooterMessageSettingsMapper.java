package de.unimuenster.imi.randimi.mapping.settings;

import de.unimuenster.imi.randimi.dto.settings.FooterMessageSettingsDTO;
import de.unimuenster.imi.randimi.model.settings.FooterMessageSettings;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Tobias Brix <tobias.brix@uni-muenster.de>
 */
@Component
public class FooterMessageSettingsMapper {
    /**
     * Converts this {@link FooterMessageSettings} object to an {@link FooterMessageSettingsDTO} object.
     *
     * @return An {@link FooterMessageSettingsDTO} object based on this {@link FooterMessageSettings} object.
     */
    public FooterMessageSettingsDTO toFooterMessageSettingsDTO(FooterMessageSettings footerMessageSettings) {
        FooterMessageSettingsDTO footerMessageSettingsDTO = new FooterMessageSettingsDTO();

        footerMessageSettingsDTO.setId(footerMessageSettings.getId());
        footerMessageSettingsDTO.setCurrentLanguage(footerMessageSettings.getCurrentLanguage());
        footerMessageSettingsDTO.setImprintContent(footerMessageSettings.getImprintContent());
		footerMessageSettingsDTO.setDataPrivacyContent(footerMessageSettings.getDataPrivacyContent());
		footerMessageSettingsDTO.setSupportContent(footerMessageSettings.getSupportContent());

        return footerMessageSettingsDTO;
    }

	 /**
     * Converts this {@link FooterMessageSettingsDTO} object to an {@link FooterMessageSettings} object.
     *
     * @return An {@link FooterMessageSettings} object based on this {@link FooterMessageSettingsDTO} object.
     */
    public FooterMessageSettings toFooterMessageSettings(FooterMessageSettingsDTO dto) {
        FooterMessageSettings footerMessageSettings = new FooterMessageSettings();

        footerMessageSettings.setId(dto.getId());
        footerMessageSettings.setCurrentLanguage(dto.getCurrentLanguage());
        footerMessageSettings.setImprintContent(dto.getImprintContent());
		footerMessageSettings.setDataPrivacyContent(dto.getDataPrivacyContent());
		footerMessageSettings.setSupportContent(dto.getSupportContent());

        return footerMessageSettings;
    }
}
