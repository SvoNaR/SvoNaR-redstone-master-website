package ru.redstonemaster.web.tutorial;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.TutorialLesson;
import ru.redstonemaster.web.model.TutorialSection;
import ru.redstonemaster.web.service.TutorialContentService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TutorialProgressService {

	private final UserLessonCompletionRepository completionRepository;
	private final TutorialContentService tutorialContentService;

	public TutorialProgressService(
			UserLessonCompletionRepository completionRepository,
			TutorialContentService tutorialContentService
	) {
		this.completionRepository = completionRepository;
		this.tutorialContentService = tutorialContentService;
	}

	@Transactional(readOnly = true)
	public TutorialProgressStats getStats(Long userId) {
		List<String> keys = this.completionRepository.findByUserId(userId).stream()
				.map(UserLessonCompletion::lessonKey)
				.sorted()
				.toList();
		return new TutorialProgressStats(keys.size(), this.countTotalLessons(), keys);
	}

	@Transactional
	public TutorialProgressStats mergeCompletedLessons(Long userId, List<String> lessonKeys) {
		Set<String> validKeys = this.validLessonKeys();
		for (String key : lessonKeys) {
			if (!validKeys.contains(key)) {
				continue;
			}
			String[] parts = key.split(":", 2);
			if (parts.length != 2) {
				continue;
			}
			String sectionId = parts[0];
			String lessonId = parts[1];
			if (!this.completionRepository.existsByUserIdAndSectionIdAndLessonId(userId, sectionId, lessonId)) {
				this.completionRepository.save(new UserLessonCompletion(userId, sectionId, lessonId));
			}
		}
		return this.getStats(userId);
	}

	public int countTotalLessons() {
		return this.tutorialContentService.getSections(WebLocale.RU).stream()
				.mapToInt(section -> section.lessons().size())
				.sum();
	}

	private Set<String> validLessonKeys() {
		Set<String> keys = new HashSet<>();
		for (TutorialSection section : this.tutorialContentService.getSections(WebLocale.RU)) {
			for (TutorialLesson lesson : section.lessons()) {
				keys.add(section.id() + ":" + lesson.id());
			}
		}
		return keys;
	}

	public List<String> parseLessonKeys(List<String> rawKeys) {
		if (rawKeys == null) {
			return List.of();
		}
		List<String> parsed = new ArrayList<>();
		for (String key : rawKeys) {
			if (key != null && !key.isBlank()) {
				parsed.add(key.trim());
			}
		}
		return parsed;
	}
}
