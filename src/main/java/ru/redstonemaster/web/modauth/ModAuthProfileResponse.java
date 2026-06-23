package ru.redstonemaster.web.modauth;

import java.util.List;

public record ModAuthProfileResponse(
		String username,
		String avatarUrl,
		String syncToken,
		String email,
		String role,
		String createdAt,
		int completedLessons,
		int totalLessons,
		List<String> completedLessonKeys
) {
}
