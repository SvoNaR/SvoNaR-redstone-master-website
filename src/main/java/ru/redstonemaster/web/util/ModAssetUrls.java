package ru.redstonemaster.web.util;

import java.util.List;

public final class ModAssetUrls {
	private static final String PREFIX = "/mod-assets/";

	private ModAssetUrls() {
	}

	public static List<String> toWebUrls(List<String> modPaths) {
		if (modPaths == null) {
			return List.of();
		}
		return modPaths.stream()
				.map(ModAssetUrls::toWebUrl)
				.filter(path -> !path.isBlank())
				.toList();
	}

	public static String toWebUrl(String modPath) {
		if (modPath == null || modPath.isBlank()) {
			return "";
		}
		String normalized = modPath.startsWith("textures/tutorial/")
				? modPath.substring("textures/tutorial/".length())
				: modPath;
		return PREFIX + normalized;
	}

	public static String videoBaseUrl(String videoId) {
		if (videoId == null || videoId.isBlank()) {
			return "";
		}
		return PREFIX + "tutorials/videos/" + videoId + "/";
	}
}
