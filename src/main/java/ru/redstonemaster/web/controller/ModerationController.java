package ru.redstonemaster.web.controller;



import org.springframework.core.io.FileSystemResource;

import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.redstonemaster.web.locale.WebLocale;

import ru.redstonemaster.web.comment.LessonCommentService;
import ru.redstonemaster.web.moderation.LessonSubmission;

import ru.redstonemaster.web.moderation.LessonSubmissionService;

import ru.redstonemaster.web.moderation.ModerationLessonBuilderService;

import ru.redstonemaster.web.moderation.ModerationProperties;

import ru.redstonemaster.web.moderation.PseudoVideoStatus;

import ru.redstonemaster.web.moderation.PseudoVideoWorkspaceService;

import ru.redstonemaster.web.moderation.VideoToPngFramesService;

import ru.redstonemaster.web.user.User;

import ru.redstonemaster.web.user.UserRole;

import ru.redstonemaster.web.user.UserService;



import java.io.IOException;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.List;

import java.util.Locale;



@Controller

@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")

public class ModerationController {



	private final LessonSubmissionService submissionService;

	private final ModerationLessonBuilderService lessonBuilderService;

	private final VideoToPngFramesService videoToPngFramesService;

	private final PseudoVideoWorkspaceService pseudoVideoWorkspaceService;

	private final ModerationProperties moderationProperties;

	private final UserService userService;

	private final LessonCommentService commentService;



	public ModerationController(

			LessonSubmissionService submissionService,

			ModerationLessonBuilderService lessonBuilderService,

			VideoToPngFramesService videoToPngFramesService,

			PseudoVideoWorkspaceService pseudoVideoWorkspaceService,

			ModerationProperties moderationProperties,

			UserService userService,

			LessonCommentService commentService

	) {

		this.submissionService = submissionService;

		this.lessonBuilderService = lessonBuilderService;

		this.videoToPngFramesService = videoToPngFramesService;

		this.pseudoVideoWorkspaceService = pseudoVideoWorkspaceService;

		this.moderationProperties = moderationProperties;

		this.userService = userService;

		this.commentService = commentService;

	}



	@GetMapping("/moderation")

	public String moderation(

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			Model model

	) {

		WebLocale locale = WebLocale.fromCode(langCode);

		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Moderation" : "Модерация");

		model.addAttribute("videoFps", this.moderationProperties.getVideoFps());

		model.addAttribute("videoWidth", this.moderationProperties.getVideoWidth());

		model.addAttribute("videoHeight", this.moderationProperties.getVideoHeight());

		return "moderation/index";

	}



	@GetMapping("/moderation/pseudo-video")

	public String pseudoVideoForm(

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			@RequestParam(name = "submissionId", required = false) Long submissionId,

			Authentication authentication,

			Model model

	) {

		WebLocale locale = WebLocale.fromCode(langCode);

		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Create pseudo-video" : "Создать псевдо-видео");

		model.addAttribute("videoFps", this.moderationProperties.getVideoFps());

		model.addAttribute("videoWidth", this.moderationProperties.getVideoWidth());

		model.addAttribute("videoHeight", this.moderationProperties.getVideoHeight());

		if (submissionId != null) {

			LessonSubmission submission = this.submissionService.getOwnedSubmission(

					this.requireUser(authentication),

					submissionId

			);

			model.addAttribute("submission", submission);

			model.addAttribute("videoStatus", this.pseudoVideoWorkspaceService.readStatus(submission));

		}

		return "moderation/pseudo-video";

	}



	@PostMapping("/moderation/pseudo-video")

	public String createPseudoVideoDraft(

			Authentication authentication,

			@RequestParam String videoId,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			RedirectAttributes redirectAttributes

	) {

		try {

			LessonSubmission submission = this.lessonBuilderService.createPseudoVideoDraft(

					this.requireUser(authentication),

					videoId.trim()

			);

			submission = this.submissionService.saveDraft(submission);

			return "redirect:/moderation/pseudo-video?submissionId=" + submission.getId() + "&lang=" + langCode;

		} catch (Exception exception) {

			redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

			return "redirect:/moderation/pseudo-video?lang=" + langCode;

		}

	}



	@PostMapping("/moderation/video/convert")

	public String convertVideo(

			Authentication authentication,

			@RequestParam Long submissionId,

			@RequestParam("video") MultipartFile video,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			@RequestParam(name = "returnTo", defaultValue = "lesson") String returnTo,

			RedirectAttributes redirectAttributes

	) {

		User moderator = this.requireUser(authentication);

		try {

			if (video.isEmpty()) {

				throw new IllegalArgumentException("Video file is required");

			}

			LessonSubmission submission = this.submissionService.getOwnedSubmission(moderator, submissionId);

			Path videoDir = this.lessonBuilderService.videoDirectory(submission);

			Files.createDirectories(videoDir);

			Path input = videoDir.resolve("source" + this.extension(video.getOriginalFilename()));

			video.transferTo(input);

			List<Path> frames = this.videoToPngFramesService.convertToPngFrames(input, videoDir);

			this.videoToPngFramesService.writeVideoMeta(videoDir, frames.size());

			redirectAttributes.addFlashAttribute(

					"successMessage",

					WebLocale.fromCode(langCode) == WebLocale.EN

							? "Converted " + frames.size() + " PNG frames ("

									+ this.moderationProperties.getVideoFps() + " fps, "

									+ this.moderationProperties.getVideoWidth() + "×"

									+ this.moderationProperties.getVideoHeight() + ")"

							: "Создано кадров PNG: " + frames.size() + " ("

									+ this.moderationProperties.getVideoFps() + " fps, "

									+ this.moderationProperties.getVideoWidth() + "×"

									+ this.moderationProperties.getVideoHeight() + ")"

			);

		} catch (Exception exception) {

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					exception.getMessage() != null ? exception.getMessage() : "Conversion failed"

			);

		}

		return this.redirectAfterVideoAction(submissionId, langCode, returnTo);

	}



	@PostMapping("/moderation/lesson/build-video-jar")

	public String buildVideoOnlyJar(

			Authentication authentication,

			@RequestParam Long submissionId,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			@RequestParam(name = "returnTo", defaultValue = "lesson") String returnTo,

			RedirectAttributes redirectAttributes

	) {

		try {

			Path jarPath = this.submissionService.buildVideoOnlyJar(this.requireUser(authentication), submissionId);

			redirectAttributes.addFlashAttribute(

					"successMessage",

					WebLocale.fromCode(langCode) == WebLocale.EN

							? "Pseudo-video JAR built: " + jarPath.getFileName()

							: "JAR псевдо-видео собран: " + jarPath.getFileName()

			);

			redirectAttributes.addFlashAttribute("videoJarReady", true);

		} catch (Exception exception) {

			redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

		}

		return this.redirectAfterVideoAction(submissionId, langCode, returnTo);

	}



	@GetMapping("/moderation/submissions/{id}/download-video-jar")

	public ResponseEntity<Resource> downloadVideoJar(

			Authentication authentication,

			@PathVariable Long id

	) throws IOException {

		LessonSubmission submission = this.submissionService.getOwnedSubmission(this.requireUser(authentication), id);

		Path jarPath = this.submissionService.buildVideoOnlyJar(this.requireUser(authentication), id);

		FileSystemResource resource = new FileSystemResource(jarPath);

		return ResponseEntity.ok()

				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + jarPath.getFileName() + "\"")

				.contentType(MediaType.APPLICATION_OCTET_STREAM)

				.body(resource);

	}



	@GetMapping("/moderation/submissions/{id}/download-frames-zip")

	public ResponseEntity<Resource> downloadFramesZip(

			Authentication authentication,

			@PathVariable Long id

	) throws IOException {

		this.submissionService.getOwnedSubmission(this.requireUser(authentication), id);

		Path zipPath = this.submissionService.buildFramesZip(this.requireUser(authentication), id);

		FileSystemResource resource = new FileSystemResource(zipPath);

		return ResponseEntity.ok()

				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipPath.getFileName() + "\"")

				.contentType(MediaType.APPLICATION_OCTET_STREAM)

				.body(resource);

	}



	@GetMapping("/moderation/lesson")

	public String lessonForm(

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			@RequestParam(name = "submissionId", required = false) Long submissionId,

			Authentication authentication,

			Model model

	) {

		WebLocale locale = WebLocale.fromCode(langCode);

		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Create lesson" : "Создать урок");

		model.addAttribute("videoFps", this.moderationProperties.getVideoFps());

		model.addAttribute("videoWidth", this.moderationProperties.getVideoWidth());

		model.addAttribute("videoHeight", this.moderationProperties.getVideoHeight());

		if (submissionId != null) {

			LessonSubmission submission = this.submissionService.getOwnedSubmission(

					this.requireUser(authentication),

					submissionId

			);

			model.addAttribute("submission", submission);

			model.addAttribute("videoStatus", this.pseudoVideoWorkspaceService.readStatus(submission));

		}

		return "moderation/lesson";

	}



	@PostMapping("/moderation/lesson")

	public String createLesson(

			Authentication authentication,

			@RequestParam String sectionId,

			@RequestParam String lessonId,

			@RequestParam String videoId,

			@RequestParam String titleRu,

			@RequestParam String titleEn,

			@RequestParam String bodyRu,

			@RequestParam String bodyEn,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			RedirectAttributes redirectAttributes

	) {

		try {

			LessonSubmission submission = this.lessonBuilderService.createDraft(

					this.requireUser(authentication),

					sectionId.trim(),

					lessonId.trim(),

					videoId.trim(),

					titleRu,

					titleEn,

					bodyRu,

					bodyEn,

					List.of()

			);

			submission = this.submissionService.saveDraft(submission);

			redirectAttributes.addFlashAttribute(

					"successMessage",

					WebLocale.fromCode(langCode) == WebLocale.EN ? "Lesson draft created" : "Черновик урока создан"

			);

			return "redirect:/moderation/lesson?submissionId=" + submission.getId() + "&lang=" + langCode;

		} catch (Exception exception) {

			redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

			return "redirect:/moderation/lesson?lang=" + langCode;

		}

	}



	@PostMapping("/moderation/lesson/build-jar")

	public String buildJar(

			Authentication authentication,

			@RequestParam Long submissionId,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			RedirectAttributes redirectAttributes

	) {

		try {

			LessonSubmission submission = this.submissionService.buildJar(

					this.requireUser(authentication),

					submissionId

			);

			redirectAttributes.addFlashAttribute(

					"successMessage",

					WebLocale.fromCode(langCode) == WebLocale.EN

							? "JAR built: " + submission.getJarPath()

							: "JAR собран: " + submission.getJarPath()

			);

		} catch (Exception exception) {

			redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

		}

		return "redirect:/moderation/lesson?submissionId=" + submissionId + "&lang=" + langCode;

	}



	@PostMapping("/moderation/lesson/submit")

	public String submitLesson(

			Authentication authentication,

			@RequestParam Long submissionId,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			RedirectAttributes redirectAttributes

	) {

		try {

			this.submissionService.submitForReview(this.requireUser(authentication), submissionId);

			redirectAttributes.addFlashAttribute(

					"successMessage",

					WebLocale.fromCode(langCode) == WebLocale.EN

							? "Lesson sent to administrator for review"

							: "Урок отправлен администратору на проверку"

			);

			return "redirect:/moderation/submissions?lang=" + langCode;

		} catch (Exception exception) {

			redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

			return "redirect:/moderation/lesson?submissionId=" + submissionId + "&lang=" + langCode;

		}

	}



	@GetMapping("/moderation/mute-user")

	public String muteUserForm(

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			Model model

	) {

		WebLocale locale = WebLocale.fromCode(langCode);

		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Mute user" : "Мьют пользователя");

		return "moderation/mute-user";

	}



	@PostMapping("/moderation/mute-user")

	public String muteUser(

			Authentication authentication,

			@RequestParam String username,

			@RequestParam int minutes,

			@RequestParam String reason,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			RedirectAttributes redirectAttributes

	) {

		WebLocale locale = WebLocale.fromCode(langCode);

		try {

			User target = this.userService.findByUsername(username.trim())

					.orElseThrow(() -> new IllegalArgumentException(

							locale == WebLocale.EN ? "User not found" : "Пользователь не найден"

					));

			this.commentService.muteUser(this.requireUser(authentication), target.getId(), minutes, reason);

			redirectAttributes.addFlashAttribute(

					"successMessage",

					locale == WebLocale.EN ? "User muted" : "Пользователь замьючен"

			);

		} catch (RuntimeException exception) {

			redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

		}

		return "redirect:/moderation/mute-user?lang=" + langCode;

	}



	@GetMapping("/moderation/unmute-user")

	public String unmuteUserForm(

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			Model model

	) {

		WebLocale locale = WebLocale.fromCode(langCode);

		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Unmute user" : "Снять мьют с пользователя");

		model.addAttribute("mutedUsers", this.commentService.listActiveMutesForRole(UserRole.USER));

		return "moderation/unmute-user";

	}



	@PostMapping("/moderation/unmute-user")

	public String unmuteUser(

			Authentication authentication,

			@RequestParam Long userId,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			RedirectAttributes redirectAttributes

	) {

		WebLocale locale = WebLocale.fromCode(langCode);

		try {

			this.commentService.unmuteUser(this.requireUser(authentication), userId);

			redirectAttributes.addFlashAttribute(

					"successMessage",

					locale == WebLocale.EN ? "User unmuted" : "Мьют с пользователя снят"

			);

		} catch (RuntimeException exception) {

			redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

		}

		return "redirect:/moderation/unmute-user?lang=" + langCode;

	}



	@GetMapping("/moderation/submissions")

	public String submissions(

			Authentication authentication,

			@RequestParam(name = "lang", defaultValue = "ru") String langCode,

			Model model

	) {

		WebLocale locale = WebLocale.fromCode(langCode);

		model.addAttribute("pageTitle", locale == WebLocale.EN ? "My lesson submissions" : "Мои уроки");

		model.addAttribute(

				"submissions",

				this.submissionService.getModeratorSubmissions(this.requireUser(authentication))

		);

		return "moderation/submissions";

	}



	@GetMapping("/moderation/submissions/{id}/download")

	public ResponseEntity<Resource> downloadJar(

			Authentication authentication,

			@PathVariable Long id

	) throws IOException {

		LessonSubmission submission = this.submissionService.getOwnedSubmission(this.requireUser(authentication), id);

		if (submission.getJarPath() == null || submission.getJarPath().isBlank()) {

			return ResponseEntity.notFound().build();

		}

		Path jarPath = Path.of(submission.getJarPath());

		if (!Files.exists(jarPath)) {

			return ResponseEntity.notFound().build();

		}

		FileSystemResource resource = new FileSystemResource(jarPath);

		return ResponseEntity.ok()

				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + jarPath.getFileName() + "\"")

				.contentType(MediaType.APPLICATION_OCTET_STREAM)

				.body(resource);

	}



	private String redirectAfterVideoAction(Long submissionId, String langCode, String returnTo) {

		if ("pseudo-video".equals(returnTo)) {

			return "redirect:/moderation/pseudo-video?submissionId=" + submissionId + "&lang=" + langCode;

		}

		return "redirect:/moderation/lesson?submissionId=" + submissionId + "&lang=" + langCode;

	}



	private User requireUser(Authentication authentication) {

		return this.userService.findByUsername(authentication.getName()).orElseThrow();

	}



	private String extension(String filename) {

		if (filename == null || !filename.contains(".")) {

			return ".mp4";

		}

		return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);

	}

}


