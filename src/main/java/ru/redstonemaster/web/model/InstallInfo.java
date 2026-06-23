package ru.redstonemaster.web.model;

public record InstallInfo(
		InstallGuide playerGuide,
		InstallGuide developerGuide,
		String jarPath,
		String configPath
) {
}
