package ru.redstonemaster.web.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailConfigTest {

	@Test
	void normalizeAppPasswordStripsSpacesFromGmailAppPassword() {
		assertEquals("abcdefghijklmnop", MailConfig.normalizeAppPassword("abcd efgh ijkl mnop"));
	}

	@Test
	void normalizeAppPasswordTrimsWhitespace() {
		assertEquals("secret", MailConfig.normalizeAppPassword("  secret  "));
	}

	@Test
	void normalizeAppPasswordHandlesNull() {
		assertTrue(MailConfig.normalizeAppPassword(null).isEmpty());
	}
}
