package de.unimuenster.imi.randimi.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
@EnableAutoConfiguration(exclude = {ThymeleafAutoConfiguration.class})
public class ThymeleafConfig implements ApplicationContextAware, WebMvcConfigurer {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(@NonNull final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Bean("viewResolver")
	@Description("Thymeleaf Template Resolver")
	@Profile("!dev")
	public ViewResolver viewResolver() {
		return createViewResolver();
	}

	@Bean("viewResolver")
	@Description("Thymeleaf Template Resolver")
	@Profile("dev")
	public ViewResolver viewResolverDev() {
		final ThymeleafViewResolver viewResolver = createViewResolver();
		viewResolver.setCache(false);
		return viewResolver;
	}

	@Bean("templateEngine")
	@Profile("!dev")
	public TemplateEngine templateEngine() {
		final SpringTemplateEngine engine = creteTemplateEngine();
		engine.setTemplateResolver(templateResolver());
		return engine;
	}

	@Bean("templateEngine")
	@Profile("dev")
	public TemplateEngine templateEngineDev() {
		final SpringTemplateEngine engine = creteTemplateEngine();
		final SpringResourceTemplateResolver resolver = templateResolver();
		resolver.setCacheable(false);
		engine.setTemplateResolver(templateResolver());
		return engine;
	}

	@Bean
	public SpringResourceTemplateResolver templateResolver() {
		final SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setApplicationContext(applicationContext);
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		return resolver;
	}

	private ThymeleafViewResolver createViewResolver() {
		final ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
		viewResolver.setTemplateEngine((ISpringTemplateEngine) templateEngine());
		viewResolver.setCharacterEncoding("UTF-8");
		return viewResolver;
	}

	private SpringTemplateEngine creteTemplateEngine() {
		final SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setEnableSpringELCompiler(true);
		engine.addDialect(new SpringSecurityDialect());
		return engine;
	}


}
