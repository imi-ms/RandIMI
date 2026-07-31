package de.unimuenster.imi.randimi.config;

import de.unimuenster.imi.randimi.service.auth.CustomPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

import javax.sql.DataSource;

/**
 * Configuration class responsible for ACL related config.
 *
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class AclContext {

	private final ApplicationContext applicationContext;

	private final CacheManager cacheManager;

	private final CustomPermissionEvaluator customPermissionEvaluator;

	@Autowired @Lazy
	public AclContext(final ApplicationContext applicationContext,
	                  final CacheManager cacheManager,
	                  final CustomPermissionEvaluator customPermissionEvaluator) {
		this.applicationContext = applicationContext;
		this.cacheManager = cacheManager;
		this.customPermissionEvaluator = customPermissionEvaluator;
	}

	@Bean
	public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
		final var expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
		expressionHandler.setApplicationContext(applicationContext);
		return expressionHandler;
	}

	@Bean
	public DefaultWebSecurityExpressionHandler webExpressionHandler(CustomPermissionEvaluator customPermissionEvaluator) {
		return new DefaultWebSecurityExpressionHandler() {
			{
				setPermissionEvaluator(customPermissionEvaluator);
			}
		};
	}

	@Bean
	public JdbcMutableAclService aclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
		return new JdbcMutableAclService(dataSource, lookupStrategy, aclCache);
	}

	@Bean
	public AclAuthorizationStrategy aclAuthorizationStrategy() {
		return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN"));
	}

	@Bean
	public PermissionGrantingStrategy permissionGrantingStrategy() {
		return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
	}

	@Bean
	public AclCache aclCache(PermissionGrantingStrategy permissionGrantingStrategy,
	                         AclAuthorizationStrategy aclAuthorizationStrategy) {
		return new SpringCacheBasedAclCache(cacheManager.getCache("aclCache"), permissionGrantingStrategy,
		                                    aclAuthorizationStrategy);
	}

	@Bean
	public LookupStrategy lookupStrategy(DataSource dataSource, AclCache aclCache, AclAuthorizationStrategy aclAuthorizationStrategy) {
		return new BasicLookupStrategy(dataSource, aclCache, aclAuthorizationStrategy, new ConsoleAuditLogger());
	}
}
