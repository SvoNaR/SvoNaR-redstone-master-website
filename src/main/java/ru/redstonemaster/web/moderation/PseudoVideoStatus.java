package ru.redstonemaster.web.moderation;

public record PseudoVideoStatus(
		boolean ready,
		int frameCount,
		int fps,
		int width,
		int height,
		String sourceFilename
) {
	public static PseudoVideoStatus empty() {
		return new PseudoVideoStatus(false, 0, 0, 0, 0, null);
	}
}
