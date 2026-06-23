package ru.redstonemaster.web.tutorial;

import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.service.ModLangService;

public final class TutorialLessonContentHelper {

	public record LessonBodyParts(String goalParagraph, String remainingBody) {
	}

	private TutorialLessonContentHelper() {
	}

	public static LessonBodyParts splitForVideo(String body, WebLocale locale, ModLangService modLangService) {
		String goalHeading = modLangService.get(locale, "gui.redstone-master.tutorial.video.goal_heading");
		if (body == null || !body.startsWith(goalHeading)) {
			return new LessonBodyParts("", body != null ? body : "");
		}
		String rest = body.substring(goalHeading.length());
		if (rest.startsWith("\r\n")) {
			rest = rest.substring(2);
		} else if (rest.startsWith("\n")) {
			rest = rest.substring(1);
		}
		int sectionBreak = rest.indexOf("\n\n");
		if (sectionBreak >= 0) {
			return new LessonBodyParts(rest.substring(0, sectionBreak).trim(), rest.substring(sectionBreak + 2));
		}
		return new LessonBodyParts(rest.trim(), "");
	}
}
