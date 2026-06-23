package ru.redstonemaster.web.profile;

public record HeaderUserView(
		String username,
		String avatarUrl,
		int notificationCount,
		String notificationBadge
) {
}
