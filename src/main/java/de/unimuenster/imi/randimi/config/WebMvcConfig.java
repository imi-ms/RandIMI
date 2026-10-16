package de.unimuenster.imi.randimi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.LiteWebJarsResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("/webjars/")
				.resourceChain(false)
				.addResolver(new LiteWebJarsResourceResolver()); //needed to work with webjars
		registry.addResourceHandler("/css/**")
				.addResourceLocations("/WEB-INF/css/");
		registry.addResourceHandler("/pics/**")
				.addResourceLocations("/WEB-INF/pics/","/WEB-INF/resources/pics/");
		registry.addResourceHandler("/resources/**")
				.addResourceLocations("/WEB-INF/resources/");

	}
}
