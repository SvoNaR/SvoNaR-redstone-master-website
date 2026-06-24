package ru.redstonemaster.web.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public final class NoopOutboundEmailSender implements OutboundEmailSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoopOutboundEmailSender.class);

	@Override
	public boolean isConfigured() {
		return false;
	}

	@Override
	public boolean send(String to, String subject, String plainTextBody) {
		LOGGER.warn("Outbound email is not configured; message to {} was not sent", to);
		return false;
	}
}
