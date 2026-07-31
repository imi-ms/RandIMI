package de.unimuenster.imi.randimi.repository.settings;

import de.unimuenster.imi.randimi.config.CacheConfig;
import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.model.settings.Settings;

import de.unimuenster.imi.randimi.repository.CacheAwareCrudRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Data access object used for the Settings class.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface SettingsRepository extends CacheAwareCrudRepository<Settings> {

	@Override
	@Cacheable(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, key = "#id")
	Optional<Settings> findById(Long id);

	@Query("SELECT e FROM Settings e")
	Optional<SettingsId> findFirst();

	public Settings getCurrentSettings();

	public SupportedLanguage getDefaultLanguage();

	public String getMailHost();

	public int getMailPort();

	public Boolean getMailTLS();

	public Boolean getMailSMTPAuth();

	public String getMailUsername();

	public String getMailPassword();

	public String getMailSender();

	public String getSupportMail();

	public String getSupportPhone();

	interface SettingsId {
		Long getId();
	}
}
