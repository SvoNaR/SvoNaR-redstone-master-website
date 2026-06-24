package ru.redstonemaster.web.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SendGridOutboundEmailSender implements OutboundEmailSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendGridOutboundEmailSender.class);
	private static final URI SEND_ENDPOINT = URI.create("https://api.sendgrid.com/v3/mail/send");

	private final String apiKey;
	private final String fromEmail;
	private final String fromName;
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	public SendGridOutboundEmailSender(String apiKey, String fromEmail, String fromName) {
		this(apiKey, fromEmail, fromName, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build(), new ObjectMapper());
	}

	SendGridOutboundEmailSender(
			String apiKey,
			String fromEmail,
			String fromName,
			HttpClient httpClient,
			ObjectMapper objectMapper
	) {
		this.apiKey = apiKey == null ? "" : apiKey.trim();
		this.fromEmail = fromEmail;
		this.fromName = fromName;
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean isConfigured() {
		return StringUtils.hasText(this.apiKey);
	}

	@Override
	public boolean send(String to, String subject, String plainTextBody) {
		if (!this.isConfigured()) {
			return false;
		}
		try {
			String payload = this.buildPayload(to, subject, plainTextBody);
			HttpRequest request = HttpRequest.newBuilder(SEND_ENDPOINT)
					.timeout(Duration.ofSeconds(30))
					.header("Authorization", "Bearer " + this.apiKey)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
					.build();
			HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			int status = response.statusCode();
			if (status >= 200 && status < 300) {
				LOGGER.info("SendGrid email sent to {}", to);
				return true;
			}
			LOGGER.error("SendGrid rejected email to {}: HTTP {} {}", to, status, response.body());
			return false;
		} catch (Exception ex) {
			LOGGER.error("Failed to send SendGrid email to {}", to, ex);
			return false;
		}
	}

	String buildPayload(String to, String subject, String plainTextBody) throws Exception {
		ObjectNode root = this.objectMapper.createObjectNode();

		ArrayNode personalizations = root.putArray("personalizations");
		ObjectNode personalization = personalizations.addObject();
		ArrayNode recipients = personalization.putArray("to");
		recipients.addObject().put("email", to);

		ObjectNode from = root.putObject("from");
		from.put("email", this.fromEmail);
		from.put("name", this.fromName);

		root.put("subject", subject);

		ArrayNode content = root.putArray("content");
		content.addObject()
				.put("type", "text/plain; charset=utf-8")
				.put("value", plainTextBody);

		return this.objectMapper.writeValueAsString(root);
	}
}
