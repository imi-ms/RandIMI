package de.unimuenster.imi.randimi.repository.settings;

import de.unimuenster.imi.randimi.config.CacheConfig;
import de.unimuenster.imi.randimi.model.settings.Settings;
import de.unimuenster.imi.randimi.repository.CacheAwareCrudRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Daniel Preciado-Marquez
 */
@Transactional(readOnly = true)
public interface SettingsCrudRepository extends CacheAwareCrudRepository<Settings> {

	@Override
	@Cacheable(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, key = "#id")
	Optional<Settings> findById(Long id);

	Optional<Settings> findFirstByOrderByIdAsc();

}
