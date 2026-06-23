package ru.redstonemaster.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redstonemaster.web.comment.LessonCommentApiDto;
import ru.redstonemaster.web.comment.LessonCommentService;
import ru.redstonemaster.web.comment.LessonCommentView;

import java.util.List;

@RestController
@RequestMapping("/api/tutorial")
public class TutorialCommentRestController {

	private final LessonCommentService commentService;

	public TutorialCommentRestController(LessonCommentService commentService) {
		this.commentService = commentService;
	}

	@GetMapping("/{sectionId}/{lessonId}/comments")
	public List<LessonCommentApiDto> list(
			@PathVariable String sectionId,
			@PathVariable String lessonId
	) {
		return this.commentService.listForLesson(sectionId, lessonId, null).stream()
				.map(LessonCommentApiDto::from)
				.toList();
	}
}
