package de.unimuenster.imi.randimi.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@EnableCaching
public class CacheConfig {

	/**
	 * Create application wide cache manager.
	 * Using request scope still triggers many queries because of static files.
	 * Session scope works, but cannot be invalidated from other sessions.
	 * If another user disables a user or changes the settings, the changes do not affect other active sessions.
	 *
	 * @return The cache manager.
	 */
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.INTERFACES)
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}

	public static final String ENTITY_CACHE = "entityCache";
}
