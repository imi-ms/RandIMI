package de.unimuenster.imi.randimi.repository.settings;

import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class SettingsRepositoryImpl {

	SettingsRepository settingsRepository;

	@Autowired
	public void setSettingsRepository(@Lazy final SettingsRepository settingsRepository) {
		this.settingsRepository = settingsRepository;
	}

	public Settings getCurrentSettings() {
		Optional<SettingsRepository.SettingsId> settingsId = settingsRepository.findFirst();
		if (settingsId.isEmpty()) {
			return new Settings();
		}
		return settingsRepository.findById(settingsId.get().getId()).get();
	}

	public SupportedLanguage getDefaultLanguage() {
		return getCurrentSettings().getDefaultLanguage();
	}

	public String getMailHost() {
		return getCurrentSettings().getMailHost();
	}

	public int getMailPort() {
		return getCurrentSettings().getMailPort();
	}

	public Boolean getMailTLS() {
		return getCurrentSettings().isMailTLS();
	}

	public Boolean getMailSMTPAuth() {
		return getCurrentSettings().isMailSMTPAuth();
	}

	public String getMailUsername() {
		return getCurrentSettings().getMailUsername();
	}

	public String getMailPassword() {
		return getCurrentSettings().getMailPassword();
	}

	public String getMailSender() {
		return getCurrentSettings().getMailSender();
	}

	public String getSupportMail() {
		return getCurrentSettings().getSupportMail();
	}

	public String getSupportPhone() {
		return getCurrentSettings().getSupportPhone();
	}
}
