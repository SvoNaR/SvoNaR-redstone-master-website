package ru.redstonemaster.web.mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SendGridOutboundEmailSenderTest {

	@Test
	void buildPayloadIncludesRecipientSubjectAndPlainText() throws Exception {
		SendGridOutboundEmailSender sender = new SendGridOutboundEmailSender(
				"SG.test",
				"redstone.master.dev@gmail.com",
				"Redstone Master",
				HttpClientFactory.noop(),
				new ObjectMapper()
		);

		String json = sender.buildPayload(
				"user@example.com",
				"Подтверждение",
				"Привет\nhttps://example.com"
		);

		JsonNode root = new ObjectMapper().readTree(json);
		assertEquals("redstone.master.dev@gmail.com", root.path("from").path("email").asText());
		assertEquals("Redstone Master", root.path("from").path("name").asText());
		assertEquals("Подтверждение", root.path("subject").asText());
		assertEquals(
				"user@example.com",
				root.path("personalizations").get(0).path("to").get(0).path("email").asText()
		);
		assertEquals(
				"Привет\nhttps://example.com",
				root.path("content").get(0).path("value").asText()
		);
	}

	@Test
	void isConfiguredWhenApiKeyPresent() {
		SendGridOutboundEmailSender sender = new SendGridOutboundEmailSender(
				" SG.secret ",
				"from@example.com",
				"Redstone Master"
		);
		assertTrue(sender.isConfigured());
	}

	private static final class HttpClientFactory {
		private static java.net.http.HttpClient noop() {
			return java.net.http.HttpClient.newHttpClient();
		}
	}
}
