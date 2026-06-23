package ru.redstonemaster.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final LocalValidatorFactoryBean validator;

	public WebConfig(LocalValidatorFactoryBean validator) {
		this.validator = validator;
	}

	@Override
	public Validator getValidator() {
		return this.validator;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path modVideosDir = Path.of(System.getProperty("user.dir"))
				.resolve("../../src/main/resources/assets/redstone-master/tutorials/videos")
				.normalize();
		if (Files.isDirectory(modVideosDir)) {
			registry.addResourceHandler("/mod-assets/tutorials/videos/**")
					.addResourceLocations(modVideosDir.toUri().toString());
		}
	}

	@Bean
	LocaleResolver localeResolver() {
		AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
		resolver.setDefaultLocale(Locale.forLanguageTag("ru"));
		return resolver;
	}
}
