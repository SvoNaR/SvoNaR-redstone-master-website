package ru.redstonemaster.web.tutorial;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.redstonemaster.web.model.TutorialImage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TutorialImageParserTest {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void parsesObjectEntriesWithPathAndCaption() throws Exception {
		var node = objectMapper.readTree("""
				[
				  {
				    "path": "textures/tutorial/carry_signal_lever.png",
				    "caption": "Рычаг как источник редстоун-сигнала силы 15"
				  }
				]
				""");

		List<TutorialImage> images = TutorialImageParser.parse(node);

		assertEquals(1, images.size());
		assertEquals("/mod-assets/carry_signal_lever.png", images.getFirst().url());
		assertEquals("Рычаг как источник редстоун-сигнала силы 15", images.getFirst().displayCaption());
	}

	@Test
	void parsesLegacyStringPaths() throws Exception {
		var node = objectMapper.readTree("""
				["textures/tutorial/horizontal_wire.png"]
				""");

		List<TutorialImage> images = TutorialImageParser.parse(node);

		assertEquals(1, images.size());
		assertEquals("/mod-assets/horizontal_wire.png", images.getFirst().url());
		assertTrue(images.getFirst().displayCaption().contains("horizontal wire"));
	}
}
