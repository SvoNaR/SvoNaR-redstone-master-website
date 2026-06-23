package ru.redstonemaster.web.model;

public record SettingInfo(
		String section,
		String name,
		String defaultValue,
		String tooltip
) {
}
