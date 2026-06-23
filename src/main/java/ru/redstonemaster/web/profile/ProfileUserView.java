package ru.redstonemaster.web.profile;

import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRole;

import java.time.Instant;

public record ProfileUserView(
		long id,
		String username,
		String email,
		UserRole role,
		boolean emailVerified,
		Instant createdAt,
		String initials,
		String avatarUrl,
		boolean customAvatar,
		boolean profileIntroSeen
) {
	public static ProfileUserView from(User user, AvatarService avatarService) {
		return new ProfileUserView(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole(),
				user.isEmailVerified(),
				user.getCreatedAt(),
				user.getInitials(),
				avatarService.getAvatarUrl(user),
				user.isCustomAvatar(),
				user.isProfileIntroSeen()
		);
	}
}
