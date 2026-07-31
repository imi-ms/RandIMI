package de.unimuenster.imi.randimi.config;

import de.unimuenster.imi.randimi.model.enumeration.SupportedLanguage;
import de.unimuenster.imi.randimi.repository.settings.SettingsRepository;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.ServletContext;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * RandIMI main application config class.
 *
 * @author Tobias Brix
 * @author Tobias Hardt
 */
@Configuration
@EnableScheduling
@EnableCaching
@Import({MultiHttpSecurityConfig.class, AclContext.class, WebMvcConfig.class})
public class AppConfig {

	private final SettingsRepository settingsRepository;

	@Bean
	public OpenApiCustomizer customOpenAPI2() {
		return o -> o.getPaths().values().stream()
		                    .flatMap(pathItem -> pathItem.readOperations().stream())
		                    .forEach(operation -> operation.addParametersItem(
				                    new HeaderParameter().name("Accept-Language")
				                                         .description("Language ISO code for error messages. Supported are en-US and de-DE.")
				                                         .required(false)));
	}

	@Bean
	public OpenAPI customOpenAPI(@Value("${app.version}") final String randimiVersion) {
		return new OpenAPI()
				.components(new Components().addSecuritySchemes("Basic Authentication", new SecurityScheme().type(
						SecurityScheme.Type.HTTP).scheme("basic")))
				.addSecurityItem(new SecurityRequirement().addList("Basic Authentication"))
				.info(new Info().title("RandIMI API")
				                .version(randimiVersion)
				                .description("RandIMI is randomization service for clinical studies"));
	}

	@Autowired
	public AppConfig(final SettingsRepository settingsRepository) {
		this.settingsRepository = settingsRepository;
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
		return jackson2ObjectMapperBuilder -> {
			jackson2ObjectMapperBuilder.timeZone(TimeZone.getDefault());
			jackson2ObjectMapperBuilder.dateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		};
	}

	@Bean("messageSource")
	@Profile("!dev")
	public MessageSource messageSource() {
		return createMessageSource();
	}

	@Bean("messageSource")
	@Profile("dev")
	public MessageSource messageSourceDev() {
		final var messageSource = createMessageSource();
		messageSource.setCacheSeconds(10);
		return messageSource;
	}

	@Bean
	public CookieLocaleResolver localeResolver(ServletContext servletContext) {
		CookieLocaleResolver resolver = new CookieLocaleResolver("randimi.locale");
		resolver.setDefaultLocaleFunction(request -> {
			var accept = request.getHeader("Accept-Language");

			if (accept != null) {
				accept = accept.replaceAll("_", "-");

				List<Locale.LanguageRange> list = Locale.LanguageRange.parse(accept);
				var locale = Locale.lookup(list, SupportedLanguage.getSupportedLocals());
				if (locale != null) {
					return locale;
				}
			}

			return settingsRepository.getDefaultLanguage().toLocale();
		});
		resolver.setCookiePath(servletContext.getContextPath());
		return resolver;
	}

	@Bean
	public WebMvcConfigurer configurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				LocaleChangeInterceptor l = new LocaleChangeInterceptor();
				l.setParamName("lang");
				registry.addInterceptor(l);
			}
		};
	}

	private ReloadableResourceBundleMessageSource createMessageSource() {
		ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		source.setBasenames("classpath:messages/enum/enumMessages",
		                    "classpath:messages/exception/exceptionMessages",
		                    "classpath:messages/fragments/footerMessages",
		                    "classpath:messages/fragments/sidebar",
		                    "classpath:messages/mail/mail",
		                    "classpath:messages/messages",
		                    "classpath:messages/settings/settingsMessages",
		                    "classpath:messages/study/study_statistics",
		                    "classpath:messages/subject/subjectListMessages",
		                    "classpath:messages/title/titleMessages",
		                    "classpath:messages/validation/validationMessages");
		source.setDefaultEncoding("UTF-8");
		return source;
	}
}
