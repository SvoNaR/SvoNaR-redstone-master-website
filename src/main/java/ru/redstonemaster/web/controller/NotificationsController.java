package ru.redstonemaster.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.notification.NotificationService;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.profile.ProfileUserView;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserService;

@Controller
public class NotificationsController {

	private final UserService userService;
	private final AvatarService avatarService;
	private final NotificationService notificationService;

	public NotificationsController(
			UserService userService,
			AvatarService avatarService,
			NotificationService notificationService
	) {
		this.userService = userService;
		this.avatarService = avatarService;
		this.notificationService = notificationService;
	}

	@GetMapping("/notifications")
	public String notifications(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "tab", defaultValue = "active") String tab,
			Authentication authentication,
			Model model
	) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return "redirect:/profile?lang=" + langCode;
		}
		WebLocale locale = WebLocale.fromCode(langCode);
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		this.notificationService.syncForUser(user);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Notifications" : "Уведомления");
		model.addAttribute("profileUser", ProfileUserView.from(user, this.avatarService));
		model.addAttribute("activeTab", "read".equals(tab) ? "read" : "active");
		model.addAttribute("hasActiveNotifications", this.notificationService.getNotificationCount(user) > 0);
		if ("read".equals(tab)) {
			model.addAttribute("notifications", this.notificationService.getReadNotifications(user, langCode));
		} else {
			model.addAttribute("notifications", this.notificationService.getActiveNotifications(user, langCode));
		}
		return "notifications/index";
	}

	@PostMapping("/notifications/{id}/read")
	public String markAsRead(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "tab", defaultValue = "active") String tab,
			Authentication authentication
	) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/profile?lang=" + langCode;
		}
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		this.notificationService.markAsRead(user, id);
		return "redirect:/notifications?lang=" + langCode + "&tab=" + tab;
	}

	@PostMapping("/notifications/read-all")
	public String markAllAsRead(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Authentication authentication
	) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/profile?lang=" + langCode;
		}
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		this.notificationService.markAllAsRead(user);
		return "redirect:/notifications?lang=" + langCode + "&tab=active";
	}

	@PostMapping("/notifications/{id}/delete")
	public String deleteNotification(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "tab", defaultValue = "active") String tab,
			Authentication authentication
	) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/profile?lang=" + langCode;
		}
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		this.notificationService.deleteNotification(user, id);
		return "redirect:/notifications?lang=" + langCode + "&tab=" + tab;
	}

	@GetMapping("/notifications/{id}/open")
	public String openNewsNotification(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Authentication authentication
	) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/profile?lang=" + langCode;
		}
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		String target = this.notificationService.openNewsNotification(user, id, langCode);
		return "redirect:" + target;
	}
}
