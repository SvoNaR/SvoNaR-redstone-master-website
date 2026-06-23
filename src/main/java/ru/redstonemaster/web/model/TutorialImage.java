package ru.redstonemaster.web.model;

import ru.redstonemaster.web.util.ModAssetUrls;

public record TutorialImage(String url, String caption) {
	public TutorialImage {
		url = url != null ? url : "";
		caption = caption != null ? caption : "";
	}

	public static TutorialImage fromModPath(String modPath, String caption) {
		return new TutorialImage(ModAssetUrls.toWebUrl(modPath), caption);
	}

	public static TutorialImage ofModPath(String modPath) {
		return fromModPath(modPath, "");
	}

	public String displayCaption() {
		if (!this.caption.isBlank()) {
			return this.caption.trim();
		}
		return defaultCaptionFromUrl(this.url);
	}

	private static String defaultCaptionFromUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			return "";
		}
		String normalized = imageUrl.replace('\\', '/');
		int slash = normalized.lastIndexOf('/');
		String fileName = slash >= 0 ? normalized.substring(slash + 1) : normalized;
		int dot = fileName.lastIndexOf('.');
		if (dot > 0) {
			fileName = fileName.substring(0, dot);
		}
		return fileName.replace('_', ' ');
	}
}
