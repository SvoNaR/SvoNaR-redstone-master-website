package ru.redstonemaster.web.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailVerificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationService.class);

	private final String baseUrl;
	private final String fromAddress;
	private final boolean mailEnabled;
	private final String mailPassword;
	private final Optional<JavaMailSender> mailSender;

	public EmailVerificationService(
			@Value("${app.base-url}") String baseUrl,
			@Value("${app.mail.from}") String fromAddress,
			@Value("${app.mail.enabled}") boolean mailEnabled,
			@Value("${spring.mail.password:}") String mailPassword,
			Optional<JavaMailSender> mailSender
	) {
		this.baseUrl = baseUrl;
		this.fromAddress = fromAddress;
		this.mailEnabled = mailEnabled;
		this.mailPassword = mailPassword == null ? "" : mailPassword;
		this.mailSender = mailSender;
	}

	public boolean isMailConfigured() {
		return this.mailEnabled && !this.mailPassword.isBlank() && this.mailSender.isPresent();
	}

	public String buildRegistrationVerificationUrl(PendingRegistration pending, String langCode) {
		return this.baseUrl + "/profile/verify?token=" + pending.getVerificationToken() + "&lang=" + langCode;
	}

	public String buildPendingEmailChangeUrl(User user, String langCode) {
		return this.baseUrl + "/profile/verify?token=" + user.getPendingEmailVerificationToken() + "&lang=" + langCode;
	}

	public boolean sendRegistrationVerificationEmail(PendingRegistration pending, String langCode) {
		return this.sendEmail(
				pending.getEmail(),
				pending.getUsername(),
				this.buildRegistrationVerificationUrl(pending, langCode),
				langCode,
				true
		);
	}

	public boolean sendPendingEmailChangeEmail(User user, String langCode) {
		return this.sendEmail(
				user.getPendingEmail(),
				user.getUsername(),
				this.buildPendingEmailChangeUrl(user, langCode),
				langCode,
				false
		);
	}

	private boolean sendEmail(String to, String username, String url, String langCode, boolean registration) {
		boolean russian = "ru".equals(langCode);
		String subject = russian
				? "Подтверждение аккаунта — Redstone Master"
				: "Confirm your account — Redstone Master";
		String body = russian
				? (registration
				? """
				Здравствуйте, %s!

				Спасибо за регистрацию на сайте Redstone Master.
				Чтобы подтвердить почту и создать аккаунт, перейдите по ссылке:

				%s

				Ссылка действует 24 часа.
				Если вы не подтвердите почту в течение этого срока, данные регистрации будут удалены.
				Если это не вы, просто проигнорируйте данное письмо.
				""".formatted(username, url)
				: """
				Здравствуйте, %s!

				Вы запросили смену адреса почты на сайте Redstone Master.
				Чтобы подтвердить новый адрес, перейдите по ссылке:

				%s

				Ссылка действует 24 часа.
				Если это не вы, просто проигнорируйте данное письмо.
				""".formatted(username, url))
				: (registration
				? """
				Hello, %s!

				Thank you for signing up at Redstone Master.
				To verify your email and create your account, open this link:

				%s

				The link is valid for 24 hours.
				If you do not confirm within this time, your registration data will be deleted.
				If this was not you, please ignore this email.
				""".formatted(username, url)
				: """
				Hello, %s!

				You requested an email change at Redstone Master.
				To confirm your new address, open this link:

				%s

				The link is valid for 24 hours.
				If this was not you, please ignore this email.
				""".formatted(username, url));

		if (this.isMailConfigured()) {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(this.fromAddress);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			try {
				this.mailSender.get().send(message);
				LOGGER.info("Verification email sent to {}", to);
				return true;
			} catch (MailException ex) {
				LOGGER.error("Failed to send verification email to {}", to, ex);
				return false;
			}
		}

		LOGGER.warn("Verification email was not sent to {} (mail not configured). Link: {}", to, url);
		return false;
	}
}
