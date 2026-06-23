package ru.redstonemaster.web.user;

import org.springframework.stereotype.Component;

@Component
public class PendingRegistrationSession {

	public static final String SESSION_KEY = "PENDING_REGISTRATION_ID";

	private PendingRegistrationSession() {
	}
}
