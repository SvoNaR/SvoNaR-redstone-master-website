package ru.redstonemaster.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.redstonemaster.web.comment.LessonCommentService;
import ru.redstonemaster.web.comment.LessonCommentView;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserService;
import ru.redstonemaster.web.user.UserRole;
import ru.redstonemaster.web.model.TutorialLesson;
import ru.redstonemaster.web.model.TutorialSection;
import ru.redstonemaster.web.service.ModLangService;
import ru.redstonemaster.web.service.TutorialContentService;
import ru.redstonemaster.web.tutorial.TutorialLessonContentHelper;
import ru.redstonemaster.web.util.ModAssetUrls;

import java.util.List;

@Controller
@RequestMapping("/tutorial")
public class TutorialController {

	private final TutorialContentService tutorialContentService;
	private final ModLangService modLangService;
	private final LessonCommentService commentService;
	private final UserService userService;

	public TutorialController(
			TutorialContentService tutorialContentService,
			ModLangService modLangService,
			LessonCommentService commentService,
			UserService userService
	) {
		this.tutorialContentService = tutorialContentService;
		this.modLangService = modLangService;
		this.commentService = commentService;
		this.userService = userService;
	}

	@GetMapping
	public String list(
			@RequestParam(name = "lang", defaultValue = "ru") String lang,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(lang);
		model.addAttribute("sections", this.tutorialContentService.getSections(locale));
		model.addAttribute("disclaimer", this.modLangService.get(locale, "gui.redstone-master.tutorial.disclaimer"));
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Tutorial" : "Обучение");
		return "tutorial/index";
	}

	@GetMapping("/{sectionId}")
	public String section(
			@PathVariable String sectionId,
			@RequestParam(name = "lang", defaultValue = "ru") String lang,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(lang);
		TutorialSection section = this.tutorialContentService.findSection(locale, sectionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("section", section);
		model.addAttribute("pageTitle", section.title());
		return "tutorial/section";
	}

	@GetMapping("/{sectionId}/{lessonId}")
	public String lesson(
			@PathVariable String sectionId,
			@PathVariable String lessonId,
			@RequestParam(name = "lang", defaultValue = "ru") String lang,
			Authentication authentication,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(lang);
		TutorialSection section = this.tutorialContentService.findSection(locale, sectionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		TutorialLesson lesson = this.tutorialContentService.findLesson(locale, sectionId, lessonId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("section", section);
		model.addAttribute("lesson", lesson);
		model.addAttribute("pageTitle", lesson.title());

		if (!lesson.videoIds().isEmpty()) {
			TutorialLessonContentHelper.LessonBodyParts bodyParts =
					TutorialLessonContentHelper.splitForVideo(lesson.body(), locale, this.modLangService);
			model.addAttribute("goalHeading", this.modLangService.get(locale, "gui.redstone-master.tutorial.video.goal_heading"));
			model.addAttribute("lessonGoal", bodyParts.goalParagraph());
			model.addAttribute("lessonBody", bodyParts.remainingBody());
			model.addAttribute("primaryVideoId", lesson.videoIds().getFirst());
			model.addAttribute("videoBaseUrl", ModAssetUrls.videoBaseUrl(lesson.videoIds().getFirst()));
		} else {
			model.addAttribute("lessonBody", lesson.body());
		}

		User viewer = this.resolveViewer(authentication);
		List<LessonCommentView> comments = this.commentService.listForLesson(sectionId, lessonId, viewer);
		model.addAttribute("comments", comments);
		model.addAttribute("maxCommentLength", ru.redstonemaster.web.comment.LessonComment.MAX_BODY_LENGTH);
		model.addAttribute("commentAuthenticated", viewer != null);
		model.addAttribute(
				"commentMuted",
				viewer != null && this.commentService.isMuted(viewer)
		);
		boolean canModerate = viewer != null
				&& (viewer.getRole() == UserRole.MODERATOR || viewer.getRole() == UserRole.ADMIN);
		model.addAttribute("canModerateComments", canModerate);

		return "tutorial/lesson";
	}

	private User resolveViewer(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		return this.userService.findByUsername(authentication.getName()).orElse(null);
	}
}
