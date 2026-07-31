package de.unimuenster.imi.randimi.mapping.settings;

import de.unimuenster.imi.randimi.dto.settings.FooterMessageSettingsDTO;
import de.unimuenster.imi.randimi.dto.settings.PseudonymRegexDTO;
import de.unimuenster.imi.randimi.dto.settings.SettingsDTO;
import de.unimuenster.imi.randimi.model.settings.FooterMessageSettings;
import de.unimuenster.imi.randimi.model.settings.PseudonymRegex;
import de.unimuenster.imi.randimi.model.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SettingsMapper {

    @Autowired
    PseudonymRegexMapper pseudonymRegexMapper;
    @Autowired
    FooterMessageSettingsMapper footerMessageSettingsMapper;

    /**
     * Converts this {@link Settings} object to an {@link SettingsDTO} object.
     *
     * @return An {@link SettingsDTO} object based on this {@link Settings} object.
     */
    public SettingsDTO toSettingsDTO(Settings settings) {
        SettingsDTO settingsDTO = new SettingsDTO();

        settingsDTO.setId(settings.getId());
        settingsDTO.setDefaultLanguage(settings.getDefaultLanguage());

        settingsDTO.setMailHost(settings.getMailHost());
        settingsDTO.setMailPort(settings.getMailPort());
        settingsDTO.setMailTLS(settings.isMailTLS());
        settingsDTO.setMailSMTPAuth(settings.isMailSMTPAuth());
        settingsDTO.setMailUsername(settings.getMailUsername());
        settingsDTO.setMailPassword(settings.getMailPassword());
        settingsDTO.setMailSender(settings.getMailSender());

        settingsDTO.setSupportMail(settings.getSupportMail());
        settingsDTO.setSupportPhone(settings.getSupportPhone());

        List<PseudonymRegexDTO> pseudonymRegexDTOs = new ArrayList<>();
        for (PseudonymRegex pseudonymRegex : settings.getPseudonymRegexList()) {
            pseudonymRegexDTOs.add(pseudonymRegexMapper.toPseudonymRegexDTO(pseudonymRegex));
        }
        settingsDTO.setPseudonymRegexList(pseudonymRegexDTOs);

        //color settings
        settingsDTO.setMainColor(settings.getMainColor());
        settingsDTO.setHighlightColor(settings.getHighlightColor());
		settingsDTO.setBackgroundColor(settings.getBackgroundColor());
        settingsDTO.setMainTextColor(settings.getMainTextColor());
		settingsDTO.setHighlightTextColor(settings.getHighlightTextColor());

		//gravatar settings
		settingsDTO.setGravatarEnabled(settings.isGravatarEnabled());
		settingsDTO.setGravatarOption(settings.getGravatarOption());

		//footer message settings
		List<FooterMessageSettingsDTO> footerMessageSettingsDTOs = new ArrayList<>();
        for (FooterMessageSettings footerMessageSettings : settings.getFooterMessageSettingsList()) {
            footerMessageSettingsDTOs.add(footerMessageSettingsMapper.toFooterMessageSettingsDTO(footerMessageSettings));
        }
        settingsDTO.setFooterMessageSettingsList(footerMessageSettingsDTOs);
		
        return settingsDTO;
    }

    public Settings toSettings(SettingsDTO dto, Settings settings) {
        settings.setId(dto.getId());
        settings.setDefaultLanguage(dto.getDefaultLanguage());

        settings.setMailHost(dto.getMailHost());
        settings.setMailPort(dto.getMailPort());
        settings.setMailTLS(dto.getMailTLS());
        settings.setMailSMTPAuth(dto.getMailSMTPAuth());
        settings.setMailUsername(dto.getMailUsername());
        settings.setMailPassword(dto.getMailPassword());
        settings.setMailSender(dto.getMailSender());

        settings.setSupportMail(dto.getSupportMail());
        settings.setSupportPhone(dto.getSupportPhone());

        // Set pseudonym regex list
        settings.getPseudonymRegexList().clear();
        for (PseudonymRegexDTO pseudonymRegexDTO : dto.getPseudonymRegexList()) {
            settings.addPseudonymRegex(pseudonymRegexMapper.toPseudonymRegex(pseudonymRegexDTO));
        }

		//color settings
		settings.setMainColor(dto.getMainColor());
		settings.setHighlightColor(dto.getHighlightColor());
		settings.setBackgroundColor(dto.getBackgroundColor());
		settings.setMainTextColor(dto.getMainTextColor());
		settings.setHighlightTextColor(dto.getHighlightTextColor());

		//gravatar settings
		settings.setGravatarEnabled(dto.isGravatarEnabled());
		settings.setGravatarOption(dto.getGravatarOption());

		// Set footer message settings list
        settings.getFooterMessageSettingsList().clear();
        for (FooterMessageSettingsDTO footerMessageSettingsDTO : dto.getFooterMessageSettingsList()) {
            settings.addFooterMessageSettings(footerMessageSettingsMapper.toFooterMessageSettings(footerMessageSettingsDTO));
        }
		
        return settings;
    }
}
