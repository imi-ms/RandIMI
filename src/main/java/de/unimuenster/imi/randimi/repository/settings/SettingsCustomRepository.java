package de.unimuenster.imi.randimi.repository.settings;

import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.settings.Settings;

/**
 * @author Daniel Preciado-Marquez
 */
public interface SettingsCustomRepository {

	Settings getCurrentSettings();

	SupportedLanguage getDefaultLanguage();

	String getMailHost();

	int getMailPort();

	Boolean getMailTLS();

	Boolean getMailSMTPAuth();

	String getMailUsername();

	String getMailPassword();

	String getMailSender();

	String getSupportMail();

	String getSupportPhone();

}
