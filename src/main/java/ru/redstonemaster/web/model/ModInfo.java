package ru.redstonemaster.web.model;

public record ModInfo(
		String name,
		String version,
		String minecraftVersion,
		String description,
		String repositoryUrl,
		String publicBaseUrl
) {
}
