package ru.redstonemaster.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.redstonemaster.web.notification.NotificationService;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.profile.HeaderUserView;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserService;

@ControllerAdvice
public class HeaderModelAdvice {

	private final UserService userService;
	private final AvatarService avatarService;
	private final NotificationService notificationService;

	public HeaderModelAdvice(
			UserService userService,
			AvatarService avatarService,
			NotificationService notificationService
	) {
		this.userService = userService;
		this.avatarService = avatarService;
		this.notificationService = notificationService;
	}

	@ModelAttribute("headerUser")
	public HeaderUserView headerUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return null;
		}
		User user = this.userService.findByUsername(authentication.getName()).orElse(null);
		if (user == null) {
			return null;
		}
		this.notificationService.syncForUser(user);
		int count = this.notificationService.getNotificationCount(user);
		return new HeaderUserView(
				user.getUsername(),
				this.avatarService.getAvatarUrl(user),
				count,
				this.notificationService.formatBadgeCount(count)
		);
	}
}
