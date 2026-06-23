package ru.redstonemaster.web.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import ru.redstonemaster.web.locale.WebLocale;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("textLinkService")
public class TextLinkService {
	private static final Pattern HTTP_URL = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
	private static final Pattern GITHUB_PATH = Pattern.compile(
			"(?<![\\w./])(github\\.com/[\\w\\-./]+)(?![\\w./])"
	);
	private static final Pattern WIKI_DOMAIN = Pattern.compile(
			"(?<![\\w./])((?:ru\\.)?minecraft\\.wiki)(?![\\w])"
	);

	private static final Map<String, String> INTERNAL_LINKS_RU = linkedMap(
			"«Обучение»", "/tutorial?lang=ru",
			"«Настройки»", "/settings?lang=ru",
			"«Профиль»", "/?lang=ru",
			"«Главное меню»", "/?lang=ru",
			"«Изучить»", "/tutorial?lang=ru",
			"docs/TUTORIAL_SOURCES.md", "https://github.com/SvoNaR/redstone-master/blob/master/docs/TUTORIAL_SOURCES.md"
	);

	private static final Map<String, String> INTERNAL_LINKS_EN = linkedMap(
			"Tutorial —", "/tutorial?lang=en",
			"Settings —", "/settings?lang=en",
			"Profile —", "/?lang=en",
			"«Study»", "/tutorial?lang=en",
			"docs/TUTORIAL_SOURCES.md", "https://github.com/SvoNaR/redstone-master/blob/master/docs/TUTORIAL_SOURCES.md"
	);

	public String toHtml(String text, String langCode) {
		if (text == null || text.isBlank()) {
			return "";
		}
		String escaped = HtmlUtils.htmlEscape(text);
		escaped = this.linkPattern(escaped, HTTP_URL, true);
		escaped = this.linkPattern(escaped, GITHUB_PATH, true);
		escaped = this.linkPattern(escaped, WIKI_DOMAIN, true);
		escaped = this.applyInternalLinks(escaped, WebLocale.fromCode(langCode));
		return escaped;
	}

	private String linkPattern(String text, Pattern pattern, boolean external) {
		Matcher matcher = pattern.matcher(text);
		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String url = matcher.group(0);
			String href = external && !url.startsWith("http") ? "https://" + url : url;
			String anchor = "<a href=\"" + href + "\"" + (external ? " target=\"_blank\" rel=\"noopener noreferrer\"" : "")
					+ ">" + url + "</a>";
			matcher.appendReplacement(result, Matcher.quoteReplacement(anchor));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private String applyInternalLinks(String text, WebLocale locale) {
		Map<String, String> links = locale == WebLocale.EN ? INTERNAL_LINKS_EN : INTERNAL_LINKS_RU;
		String result = text;
		for (Map.Entry<String, String> entry : links.entrySet()) {
			String phrase = HtmlUtils.htmlEscape(entry.getKey());
			String href = entry.getValue();
			boolean external = href.startsWith("http");
			String anchor = "<a href=\"" + href + "\""
					+ (external ? " target=\"_blank\" rel=\"noopener noreferrer\"" : "")
					+ ">" + phrase + "</a>";
			result = result.replace(phrase, anchor);
		}
		return result;
	}

	private static Map<String, String> linkedMap(String... keysAndValues) {
		Map<String, String> map = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			map.put(keysAndValues[i], keysAndValues[i + 1]);
		}
		return map;
	}
}
