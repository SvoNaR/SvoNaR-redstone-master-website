package ru.redstonemaster.web.service;

import org.springframework.stereotype.Service;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.AboutSection;

import java.util.ArrayList;
import java.util.List;

@Service
public class AboutContentService {
	private static final String BODY_KEY = "gui.redstone-master.main_menu.body";

	private final ModLangService modLangService;

	public AboutContentService(ModLangService modLangService) {
		this.modLangService = modLangService;
	}

	public List<AboutSection> getSections(WebLocale locale) {
		String body = this.modLangService.get(locale, BODY_KEY);
		String[] blocks = body.split("\n\n");
		List<AboutSection> sections = new ArrayList<>();

		for (String block : blocks) {
			String trimmed = block.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			int newline = trimmed.indexOf('\n');
			if (newline < 0) {
				sections.add(new AboutSection(trimmed, ""));
				continue;
			}
			String title = trimmed.substring(0, newline).trim();
			String content = trimmed.substring(newline + 1).trim();
			sections.add(new AboutSection(title, content));
		}
		return sections;
	}
}
