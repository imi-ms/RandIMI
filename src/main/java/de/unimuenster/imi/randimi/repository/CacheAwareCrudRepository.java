package de.unimuenster.imi.randimi.repository;

import de.unimuenster.imi.randimi.config.CacheConfig;
import de.unimuenster.imi.randimi.model.EntityBase;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CacheAwareCrudRepository<T_Entity extends EntityBase> extends CrudRepository<T_Entity, Long> {

	@Override
	@CacheEvict(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, key = "#entity.id")
	<S extends T_Entity> S save(S entity);

	@Override
	@CacheEvict(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, allEntries = true)
	<S extends T_Entity> Iterable<S> saveAll(Iterable<S> entities);

	@Override
	@CacheEvict(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, key = "#id")
	void deleteById(Long id);

	@Override
	@CacheEvict(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, key = "#entity.id")
	void delete(T_Entity entity);

	@Override
	@CacheEvict(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, allEntries = true)
	void deleteAllById(Iterable<? extends Long> ids);

	@Override
	@CacheEvict(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, allEntries = true)
	void deleteAll(Iterable<? extends T_Entity> entities);

	@Override
	@CacheEvict(cacheManager = "cacheManager", cacheNames = CacheConfig.ENTITY_CACHE, allEntries = true)
	void deleteAll();
}
