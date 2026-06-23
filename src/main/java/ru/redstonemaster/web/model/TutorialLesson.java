package ru.redstonemaster.web.model;

import java.util.List;

public record TutorialLesson(
		String id,
		String title,
		String body,
		List<TutorialImage> images,
		List<String> videoIds
) {
}
