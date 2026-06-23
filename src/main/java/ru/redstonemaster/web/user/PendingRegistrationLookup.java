package ru.redstonemaster.web.user;

import java.util.Optional;

public record PendingRegistrationLookup(Optional<PendingRegistration> pending, boolean wasExpired) {

	public static PendingRegistrationLookup notFound() {
		return new PendingRegistrationLookup(Optional.empty(), false);
	}

	public static PendingRegistrationLookup expired() {
		return new PendingRegistrationLookup(Optional.empty(), true);
	}

	public static PendingRegistrationLookup found(PendingRegistration pending) {
		return new PendingRegistrationLookup(Optional.of(pending), false);
	}
}
