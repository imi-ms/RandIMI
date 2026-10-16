package de.unimuenster.imi.randimi.repository.settings;

import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object used for the Settings class.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface SettingsRepository extends SettingsCrudRepository, SettingsCustomRepository {
}
