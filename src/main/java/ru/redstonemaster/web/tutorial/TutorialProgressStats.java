package ru.redstonemaster.web.tutorial;

import java.util.List;

public record TutorialProgressStats(
		int completedLessons,
		int totalLessons,
		List<String> completedLessonKeys
) {
}
