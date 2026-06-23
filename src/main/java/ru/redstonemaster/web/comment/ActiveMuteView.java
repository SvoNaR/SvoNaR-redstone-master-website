package ru.redstonemaster.web.comment;

import java.time.Instant;

public record ActiveMuteView(
		long userId,
		String username,
		String email,
		Instant mutedUntil,
		String reason,
		String mutedByUsername
) {
}
