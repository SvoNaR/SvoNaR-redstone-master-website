package ru.redstonemaster.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import ru.redstonemaster.web.mail.NoopOutboundEmailSender;
import ru.redstonemaster.web.mail.OutboundEmailSender;
import ru.redstonemaster.web.mail.SendGridOutboundEmailSender;
import ru.redstonemaster.web.mail.SmtpOutboundEmailSender;

@Configuration
public class OutboundEmailConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutboundEmailConfiguration.class);

	@Bean
	@Primary
	@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
	@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${app.mail.sendgrid.api-key:}')")
	public OutboundEmailSender sendGridOutboundEmailSender(
			@Value("${app.mail.sendgrid.api-key}") String apiKey,
			@Value("${app.mail.from}") String fromEmail,
			@Value("${app.mail.from-name:Redstone Master}") String fromName
	) {
		LOGGER.info("Outbound email provider: SendGrid API (HTTPS, Railway-compatible)");
		return new SendGridOutboundEmailSender(apiKey, fromEmail, fromName);
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
	@ConditionalOnMissingBean(OutboundEmailSender.class)
	@ConditionalOnBean(JavaMailSender.class)
	public OutboundEmailSender smtpOutboundEmailSender(
			JavaMailSender mailSender,
			@Value("${app.mail.from}") String fromEmail,
			@Value("${app.mail.from-name:Redstone Master}") String fromName
	) {
		LOGGER.info("Outbound email provider: SMTP (local/VPS)");
		return new SmtpOutboundEmailSender(mailSender, fromEmail, fromName);
	}

	@Bean
	@ConditionalOnMissingBean(OutboundEmailSender.class)
	public OutboundEmailSender noopOutboundEmailSender() {
		return new NoopOutboundEmailSender();
	}
}
