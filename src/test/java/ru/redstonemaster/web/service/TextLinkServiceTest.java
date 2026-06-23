package ru.redstonemaster.web.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TextLinkServiceTest {

	private final TextLinkService service = new TextLinkService();

	@Test
	void linkifiesHttpUrl() {
		String html = this.service.toHtml("See https://ru.minecraft.wiki/w/Test for details.", "ru");
		assertTrue(html.contains("<a href=\"https://ru.minecraft.wiki/w/Test\""));
	}

	@Test
	void linkifiesGithubPath() {
		String html = this.service.toHtml("Repo: github.com/SvoNaR/redstone-master", "ru");
		assertTrue(html.contains("<a href=\"https://github.com/SvoNaR/redstone-master\""));
	}

	@Test
	void linkifiesInternalTutorialTabRu() {
		String html = this.service.toHtml("Откройте «Обучение» для уроков.", "ru");
		assertTrue(html.contains("<a href=\"/tutorial?lang=ru\">&laquo;Обучение&raquo;</a>"));
	}

	@Test
	void linkifiesInternalSettingsTabEn() {
		String html = this.service.toHtml("• Settings — window size.", "en");
		assertTrue(html.contains("<a href=\"/settings?lang=en\">Settings &mdash;</a>"));
	}

	@Test
	void linkifiesWikiDomainWithoutScheme() {
		String html = this.service.toHtml("See ru.minecraft.wiki and minecraft.wiki.", "ru");
		assertTrue(html.contains("<a href=\"https://ru.minecraft.wiki\""));
		assertTrue(html.contains("<a href=\"https://minecraft.wiki\""));
	}
}
