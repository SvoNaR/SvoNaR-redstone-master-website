package ru.redstonemaster.web.config;



import org.springframework.context.MessageSource;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;



@Configuration

public class ValidationConfig {



	@Bean

	MessageSource messageSource() {

		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

		messageSource.setBasename("classpath:ValidationMessages");

		messageSource.setDefaultEncoding("UTF-8");

		return messageSource;

	}



	@Bean

	LocalValidatorFactoryBean validator(MessageSource messageSource) {

		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

		validator.setValidationMessageSource(messageSource);

		return validator;

	}

}


