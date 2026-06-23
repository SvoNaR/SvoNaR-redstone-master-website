package ru.redstonemaster.web.locale;

import java.util.Arrays;

public enum WebLocale {
	RU("ru", "ru_ru", "Русский"),
	EN("en", "en_us", "English");

	private final String code;
	private final String resourceCode;
	private final String label;

	WebLocale(String code, String resourceCode, String label) {
		this.code = code;
		this.resourceCode = resourceCode;
		this.label = label;
	}

	public String getCode() {
		return this.code;
	}

	public String getResourceCode() {
		return this.resourceCode;
	}

	public String getLabel() {
		return this.label;
	}

	public static WebLocale fromCode(String code) {
		if (code == null || code.isBlank()) {
			return RU;
		}
		return Arrays.stream(values())
				.filter(locale -> locale.code.equalsIgnoreCase(code))
				.findFirst()
				.orElse(RU);
	}
}
