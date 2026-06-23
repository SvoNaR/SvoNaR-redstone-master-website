package ru.redstonemaster.web.modauth;

import java.util.List;

public record ModSyncProgressRequest(
		List<String> completedLessons
) {
}
