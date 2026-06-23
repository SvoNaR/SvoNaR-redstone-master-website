package ru.redstonemaster.web.tutorial;

import com.fasterxml.jackson.databind.JsonNode;
import ru.redstonemaster.web.model.TutorialImage;

import java.util.ArrayList;
import java.util.List;

public final class TutorialImageParser {
	private TutorialImageParser() {
	}

	public static List<TutorialImage> parse(JsonNode node) {
		if (node == null || !node.isArray()) {
			return List.of();
		}
		List<TutorialImage> images = new ArrayList<>();
		for (JsonNode item : node) {
			if (item.isTextual()) {
				String path = item.asText();
				if (!path.isBlank()) {
					images.add(TutorialImage.ofModPath(path));
				}
				continue;
			}
			if (item.isObject()) {
				String path = item.path("path").asText("");
				String caption = item.path("caption").asText("");
				if (!path.isBlank()) {
					images.add(TutorialImage.fromModPath(path, caption));
				}
			}
		}
		return List.copyOf(images);
	}
}
