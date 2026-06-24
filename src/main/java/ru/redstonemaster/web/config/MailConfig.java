package ru.redstonemaster.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.mail.password:}')")
public class MailConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailConfig.class);

	@Bean
	public JavaMailSender javaMailSender(MailProperties mailProperties) {
		String password = normalizeAppPassword(mailProperties.getPassword());
		if (!StringUtils.hasText(password)) {
			throw new IllegalStateException("spring.mail.password is required when app.mail.enabled=true");
		}

		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
		sender.setHost(mailProperties.getHost());
		sender.setPort(mailProperties.getPort());
		sender.setUsername(mailProperties.getUsername());
		sender.setPassword(password);

		Properties javaMailProps = new Properties();
		if (mailProperties.getProperties() != null) {
			javaMailProps.putAll(mailProperties.getProperties());
		}

		javaMailProps.put("mail.transport.protocol", "smtp");
		javaMailProps.put("mail.smtp.auth", "true");
		javaMailProps.put("mail.smtp.connectiontimeout", "15000");
		javaMailProps.put("mail.smtp.timeout", "15000");
		javaMailProps.put("mail.smtp.writetimeout", "15000");

		String host = mailProperties.getHost() == null ? "smtp.gmail.com" : mailProperties.getHost();
		javaMailProps.put("mail.smtp.ssl.trust", host);

		if (mailProperties.getPort() == 465) {
			javaMailProps.put("mail.smtp.ssl.enable", "true");
			javaMailProps.put("mail.smtp.starttls.enable", "false");
			javaMailProps.put("mail.smtp.starttls.required", "false");
			javaMailProps.put("mail.smtp.socketFactory.port", "465");
			javaMailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			javaMailProps.put("mail.smtp.socketFactory.fallback", "false");
		} else {
			javaMailProps.put("mail.smtp.ssl.enable", "false");
			javaMailProps.put("mail.smtp.starttls.enable", "true");
			javaMailProps.put("mail.smtp.starttls.required", "true");
		}

		sender.setJavaMailProperties(javaMailProps);
		LOGGER.info(
				"JavaMailSender configured for {}:{} as {}",
				sender.getHost(),
				sender.getPort(),
				sender.getUsername()
		);
		return sender;
	}

	public static String normalizeAppPassword(String raw) {
		if (raw == null) {
			return "";
		}
		return raw.replace(" ", "").trim();
	}
}
