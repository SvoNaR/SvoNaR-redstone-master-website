package ru.redstonemaster.web.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.moderation.LessonSubmission;
import ru.redstonemaster.web.moderation.LessonSubmissionService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminLessonSubmissionController {

	private final LessonSubmissionService submissionService;

	public AdminLessonSubmissionController(LessonSubmissionService submissionService) {
		this.submissionService = submissionService;
	}

	@GetMapping("/admin/lesson-submissions")
	public String list(@RequestParam(name = "lang", defaultValue = "ru") String langCode, Model model) {
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute(
				"pageTitle",
				locale == WebLocale.EN ? "Lesson submissions" : "Проверка уроков"
		);
		model.addAttribute("submissions", this.submissionService.getPendingSubmissions());
		return "admin/lesson-submissions";
	}

	@PostMapping("/admin/lesson-submissions/{id}/approve")
	public String approve(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "comment", defaultValue = "") String comment,
			RedirectAttributes redirectAttributes
	) {
		this.submissionService.review(id, true, comment);
		redirectAttributes.addFlashAttribute("adminMessage", "lesson-approved");
		return "redirect:/admin/lesson-submissions?lang=" + langCode;
	}

	@PostMapping("/admin/lesson-submissions/{id}/reject")
	public String reject(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "comment", defaultValue = "") String comment,
			RedirectAttributes redirectAttributes
	) {
		this.submissionService.review(id, false, comment);
		redirectAttributes.addFlashAttribute("adminMessage", "lesson-rejected");
		return "redirect:/admin/lesson-submissions?lang=" + langCode;
	}

	@GetMapping("/admin/lesson-submissions/{id}/download")
	public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
		LessonSubmission submission = this.submissionService.getSubmission(id);
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
}
