package de.unimuenster.imi.randimi.repository.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.settings.Settings;
import de.unimuenster.imi.randimi.repository.RepositoryTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Daniel Preciado-Marquez
 */
public class SettingsRepositoryTest extends RepositoryTestBase {

	@Autowired
	SettingsRepository settingsRepository;

	@Test
	public void getCurrentSettingsTest() {
		Settings settings = settingsRepository.getCurrentSettings();
		assertNotNull(settings, "Settings not found!");
		Assertions.assertEquals(6L, settings.getId(), "Found wrong settings!");

		// Delete settings
		settingsRepository.delete(settings);
		Long count = settingsRepository.count();
		assertEquals(0L, count);

		settings = settingsRepository.getCurrentSettings();
		assertNotNull(settings, "Settings not found!");
	}

	@Test
	public void getDefaultLanguageTest() {
		assertEquals(SupportedLanguage.GERMAN, settingsRepository.getDefaultLanguage());
	}

	@Test
	public void getMailHostTest() {
		assertEquals("secmail.uni-muenster.de", settingsRepository.getMailHost());
	}

	@Test
	public void getMailPortTest() {
		assertEquals(587, settingsRepository.getMailPort());
	}

	@Test
	public void getMailTLSTest() {
		assertEquals(true, settingsRepository.getMailTLS());
	}

	@Test
	public void getMailSMTPAuthTest() {
		assertEquals(true, settingsRepository.getMailSMTPAuth());
	}

	@Test
	public void getMailUsernameTest() {
		assertEquals("randimi", settingsRepository.getMailUsername());
	}

	@Test
	public void getMailPasswordTest() {
		assertEquals("dcb1a2e23bf956609b6194476853b52f", settingsRepository.getMailPassword());
	}

	@Test
	public void getMailSenderTest() {
		assertEquals("randimi@uni-muenster.de", settingsRepository.getMailSender());
	}

	@Test
	public void getSupportMailTest() {
		assertEquals("randimi@uni-muenster.de", settingsRepository.getSupportMail());
	}

	@Test
	public void getSupportPhoneTest() {
		assertEquals("+49 (0)251 83 52526", settingsRepository.getSupportPhone());
	}

}
