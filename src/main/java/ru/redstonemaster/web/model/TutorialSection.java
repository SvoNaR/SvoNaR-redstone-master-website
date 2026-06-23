package ru.redstonemaster.web.model;

import java.util.List;

public record TutorialSection(
		String id,
		String title,
		String summary,
		String sources,
		List<TutorialImage> images,
		List<TutorialLesson> lessons
) {
}
