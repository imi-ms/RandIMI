package de.unimuenster.imi.randimi.repository.settings;

import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.settings.Settings;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Component
public class SettingsCustomRepositoryImpl implements SettingsCustomRepository {

	private final SettingsCrudRepository settingsCrudRepository;

	public SettingsCustomRepositoryImpl(final SettingsCrudRepository settingsCrudRepository) {
		this.settingsCrudRepository = settingsCrudRepository;
	}

	@Override
	public Settings getCurrentSettings() {
		return settingsCrudRepository.findFirstByOrderByIdAsc().orElseGet(Settings::new);
	}

	@Override
	public SupportedLanguage getDefaultLanguage() {
		return getCurrentSettings().getDefaultLanguage();
	}

	@Override
	public String getMailHost() {
		return getCurrentSettings().getMailHost();
	}

	@Override
	public int getMailPort() {
		return getCurrentSettings().getMailPort();
	}

	@Override
	public Boolean getMailTLS() {
		return getCurrentSettings().isMailTLS();
	}

	@Override
	public Boolean getMailSMTPAuth() {
		return getCurrentSettings().isMailSMTPAuth();
	}

	@Override
	public String getMailUsername() {
		return getCurrentSettings().getMailUsername();
	}

	@Override
	public String getMailPassword() {
		return getCurrentSettings().getMailPassword();
	}

	@Override
	public String getMailSender() {
		return getCurrentSettings().getMailSender();
	}

	@Override
	public String getSupportMail() {
		return getCurrentSettings().getSupportMail();
	}

	@Override
	public String getSupportPhone() {
		return getCurrentSettings().getSupportPhone();
	}

}
