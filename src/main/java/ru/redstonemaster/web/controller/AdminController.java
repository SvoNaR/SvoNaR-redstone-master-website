package ru.redstonemaster.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import ru.redstonemaster.web.admin.AdminPageView;
import ru.redstonemaster.web.admin.AdminStatsService;
import ru.redstonemaster.web.comment.LessonCommentService;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.profile.ProfileUserView;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRole;
import ru.redstonemaster.web.user.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final UserService userService;
	private final AvatarService avatarService;
	private final AdminStatsService adminStatsService;
	private final LessonCommentService commentService;

	public AdminController(
			UserService userService,
			AvatarService avatarService,
			AdminStatsService adminStatsService,
			LessonCommentService commentService
	) {
		this.userService = userService;
		this.avatarService = avatarService;
		this.adminStatsService = adminStatsService;
		this.commentService = commentService;
	}

	@GetMapping("/admin")
	public String admin(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Administration" : "Администрация");
		model.addAttribute("stats", this.adminStatsService.getStats());
		return "admin/index";
	}

	@GetMapping("/admin/moderators")
	public String moderators(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "userPage", defaultValue = "1") int userPage,
			@RequestParam(name = "userSearch", defaultValue = "") String userSearch,
			@RequestParam(name = "modPage", defaultValue = "1") int modPage,
			@RequestParam(name = "modSearch", defaultValue = "") String modSearch,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		Page<User> regularUsersPage = this.userService.findUsersByRole(UserRole.USER, userSearch, userPage);
		Page<User> moderatorsPage = this.userService.findUsersByRole(UserRole.MODERATOR, modSearch, modPage);

		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Moderators" : "Модераторы");
		model.addAttribute("regularUsers", this.toViews(regularUsersPage.getContent()));
		model.addAttribute("moderators", this.toViews(moderatorsPage.getContent()));
		model.addAttribute("regularUserPage", AdminPageView.from(regularUsersPage, userSearch));
		model.addAttribute("moderatorPage", AdminPageView.from(moderatorsPage, modSearch));
		return "admin/moderators";
	}

	@PostMapping("/admin/promote")
	public String promote(
			@RequestParam Long userId,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "userPage", defaultValue = "1") int userPage,
			@RequestParam(name = "userSearch", defaultValue = "") String userSearch,
			@RequestParam(name = "modPage", defaultValue = "1") int modPage,
			@RequestParam(name = "modSearch", defaultValue = "") String modSearch,
			RedirectAttributes redirectAttributes
	) {
		try {
			this.userService.promoteToModerator(userId);
			redirectAttributes.addFlashAttribute("adminMessage", "promoted");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
		}
		return "redirect:/admin/moderators?" + this.buildAdminQuery(langCode, userPage, userSearch, modPage, modSearch);
	}

	@PostMapping("/admin/demote")
	public String demote(
			@RequestParam Long userId,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "userPage", defaultValue = "1") int userPage,
			@RequestParam(name = "userSearch", defaultValue = "") String userSearch,
			@RequestParam(name = "modPage", defaultValue = "1") int modPage,
			@RequestParam(name = "modSearch", defaultValue = "") String modSearch,
			RedirectAttributes redirectAttributes
	) {
		try {
			this.userService.demoteModerator(userId);
			redirectAttributes.addFlashAttribute("adminMessage", "demoted");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
		}
		return "redirect:/admin/moderators?" + this.buildAdminQuery(langCode, userPage, userSearch, modPage, modSearch);
	}

	@GetMapping("/admin/unmute-moderator")
	public String unmuteModeratorForm(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Unmute moderator" : "Снять мьют с модератора");
		model.addAttribute("mutedModerators", this.commentService.listActiveMutesForRole(UserRole.MODERATOR));
		return "admin/unmute-moderator";
	}

	@PostMapping("/admin/unmute-moderator")
	public String unmuteModerator(
			Authentication authentication,
			@RequestParam Long userId,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			RedirectAttributes redirectAttributes
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		try {
			User actor = this.userService.findByUsername(authentication.getName()).orElseThrow();
			this.commentService.unmuteUser(actor, userId);
			redirectAttributes.addFlashAttribute(
					"successMessage",
					locale == WebLocale.EN ? "Moderator unmuted" : "Мьют с модератора снят"
			);
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
		}
		return "redirect:/admin/unmute-moderator?lang=" + langCode;
	}

	private String buildAdminQuery(String langCode, int userPage, String userSearch, int modPage, String modSearch) {
		return "lang=" + UriUtils.encodeQueryParam(langCode, StandardCharsets.UTF_8)
				+ "&userPage=" + Math.max(userPage, 1)
				+ "&userSearch=" + UriUtils.encodeQueryParam(userSearch == null ? "" : userSearch, StandardCharsets.UTF_8)
				+ "&modPage=" + Math.max(modPage, 1)
				+ "&modSearch=" + UriUtils.encodeQueryParam(modSearch == null ? "" : modSearch, StandardCharsets.UTF_8);
	}

	private List<ProfileUserView> toViews(List<User> users) {
		return users.stream().map(user -> ProfileUserView.from(user, this.avatarService)).toList();
	}
}
