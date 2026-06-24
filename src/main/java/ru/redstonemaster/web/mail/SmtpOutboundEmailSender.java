package ru.redstonemaster.web.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

public class SmtpOutboundEmailSender implements OutboundEmailSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmtpOutboundEmailSender.class);

	private final JavaMailSender mailSender;
	private final String fromEmail;
	private final String fromName;

	public SmtpOutboundEmailSender(JavaMailSender mailSender, String fromEmail, String fromName) {
		this.mailSender = mailSender;
		this.fromEmail = fromEmail;
		this.fromName = fromName;
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public boolean send(String to, String subject, String plainTextBody) {
		try {
			MimeMessage mimeMessage = this.mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
			helper.setFrom(this.fromEmail, this.fromName);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(plainTextBody, false);
			this.mailSender.send(mimeMessage);
			LOGGER.info("SMTP email sent to {}", to);
			return true;
		} catch (Exception ex) {
			LOGGER.error("Failed to send SMTP email to {}", to, ex);
			return false;
		}
	}
}
