package ru.redstonemaster.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.redstonemaster.web.comment.LessonCommentService;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserService;

@Controller
@RequestMapping("/tutorial")
@PreAuthorize("isAuthenticated()")
public class LessonCommentController {

	private final LessonCommentService commentService;
	private final UserService userService;

	public LessonCommentController(LessonCommentService commentService, UserService userService) {
		this.commentService = commentService;
		this.userService = userService;
	}

	@PostMapping("/{sectionId}/{lessonId}/comments")
	public String postComment(
			Authentication authentication,
			@PathVariable String sectionId,
			@PathVariable String lessonId,
			@RequestParam String body,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			RedirectAttributes redirectAttributes
	) {
		return this.handlePost(
				authentication,
				sectionId,
				lessonId,
				langCode,
				redirectAttributes,
				() -> this.commentService.postComment(this.requireUser(authentication), sectionId, lessonId, body, null)
		);
	}

	@PostMapping("/{sectionId}/{lessonId}/comments/{parentId}/reply")
	public String reply(
			Authentication authentication,
			@PathVariable String sectionId,
			@PathVariable String lessonId,
			@PathVariable Long parentId,
			@RequestParam String body,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			RedirectAttributes redirectAttributes
	) {
		return this.handlePost(
				authentication,
				sectionId,
				lessonId,
				langCode,
				redirectAttributes,
				() -> this.commentService.postComment(
						this.requireUser(authentication),
						sectionId,
						lessonId,
						body,
						parentId
				)
		);
	}

	@PostMapping("/comments/{commentId}/delete")
	public String deleteComment(
			Authentication authentication,
			@PathVariable Long commentId,
			@RequestParam String sectionId,
			@RequestParam String lessonId,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			RedirectAttributes redirectAttributes
	) {
		try {
			this.commentService.deleteComment(this.requireUser(authentication), commentId);
			redirectAttributes.addFlashAttribute(
					"commentMessage",
					WebLocale.fromCode(langCode) == WebLocale.EN ? "Comment deleted" : "Комментарий удалён"
			);
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("commentError", exception.getMessage());
		}
		return "redirect:/tutorial/" + sectionId + "/" + lessonId + "?lang=" + langCode;
	}

	@PostMapping("/comments/{commentId}/mute")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	public String muteFromComment(
			Authentication authentication,
			@PathVariable Long commentId,
			@RequestParam String sectionId,
			@RequestParam String lessonId,
			@RequestParam int minutes,
			@RequestParam String reason,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			RedirectAttributes redirectAttributes
	) {
		try {
			this.commentService.muteAuthorFromComment(
					this.requireUser(authentication),
					commentId,
					minutes,
					reason
			);
			redirectAttributes.addFlashAttribute(
					"commentMessage",
					WebLocale.fromCode(langCode) == WebLocale.EN ? "User muted" : "Пользователь замьючен"
			);
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("commentError", exception.getMessage());
		}
		return "redirect:/tutorial/" + sectionId + "/" + lessonId + "?lang=" + langCode;
	}

	private String handlePost(
			Authentication authentication,
			String sectionId,
			String lessonId,
			String langCode,
			RedirectAttributes redirectAttributes,
			Runnable action
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		try {
			action.run();
			redirectAttributes.addFlashAttribute(
					"commentMessage",
					locale == WebLocale.EN ? "Comment posted" : "Комментарий опубликован"
			);
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("commentError", exception.getMessage());
		}
		return "redirect:/tutorial/" + sectionId + "/" + lessonId + "?lang=" + langCode;
	}

	private User requireUser(Authentication authentication) {
		return this.userService.findByUsername(authentication.getName()).orElseThrow();
	}
}
