package ru.redstonemaster.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.TutorialLesson;
import ru.redstonemaster.web.model.TutorialSection;
import ru.redstonemaster.web.tutorial.TutorialImageParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TutorialContentService {
	private final ObjectMapper objectMapper;
	private final ConcurrentMap<WebLocale, List<TutorialSection>> cache = new ConcurrentHashMap<>();

	public TutorialContentService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public List<TutorialSection> getSections(WebLocale locale) {
		return this.cache.computeIfAbsent(locale, this::load);
	}

	public Optional<TutorialSection> findSection(WebLocale locale, String sectionId) {
		return this.getSections(locale).stream()
				.filter(section -> section.id().equals(sectionId))
				.findFirst();
	}

	public Optional<TutorialLesson> findLesson(WebLocale locale, String sectionId, String lessonId) {
		return this.findSection(locale, sectionId)
				.flatMap(section -> section.lessons().stream()
						.filter(lesson -> lesson.id().equals(lessonId))
						.findFirst());
	}

	private List<TutorialSection> load(WebLocale locale) {
		String path = "mod-data/tutorial/" + locale.getResourceCode() + ".json";
		try (InputStream input = new ClassPathResource(path).getInputStream()) {
			JsonNode root = this.objectMapper.readTree(input);
			JsonNode sectionsNode = root.get("sections");
			if (sectionsNode == null || !sectionsNode.isArray()) {
				return List.of();
			}
			List<TutorialSection> sections = new ArrayList<>();
			for (JsonNode sectionNode : sectionsNode) {
				sections.add(this.parseSection(sectionNode));
			}
			return List.copyOf(sections);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to load tutorial file: " + path, exception);
		}
	}

	private TutorialSection parseSection(JsonNode sectionNode) {
		List<TutorialLesson> lessons = new ArrayList<>();
		JsonNode lessonsNode = sectionNode.get("lessons");
		if (lessonsNode != null && lessonsNode.isArray()) {
			for (JsonNode lessonNode : lessonsNode) {
				lessons.add(this.parseLesson(lessonNode));
			}
		}
		return new TutorialSection(
				sectionNode.path("id").asText(),
				sectionNode.path("title").asText(),
				sectionNode.path("summary").asText(""),
				sectionNode.path("sources").asText(""),
				TutorialImageParser.parse(sectionNode.get("images")),
				List.copyOf(lessons)
		);
	}

	private TutorialLesson parseLesson(JsonNode lessonNode) {
		return new TutorialLesson(
				lessonNode.path("id").asText(),
				lessonNode.path("title").asText(),
				lessonNode.path("body").asText(""),
				TutorialImageParser.parse(lessonNode.get("images")),
				this.readStringList(lessonNode.get("videos"))
		);
	}

	private List<String> readStringList(JsonNode node) {
		if (node == null || !node.isArray()) {
			return List.of();
		}
		List<String> values = new ArrayList<>();
		for (JsonNode item : node) {
			values.add(item.asText());
		}
		return values;
	}
}
