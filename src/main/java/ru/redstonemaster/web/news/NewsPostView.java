package ru.redstonemaster.web.news;

import java.time.Instant;

public record NewsPostView(
		Long id,
		String title,
		String body,
		String authorName,
		Instant createdAt
) {
	public static NewsPostView from(NewsPost post, String langCode) {
		boolean english = "en".equals(langCode);
		return new NewsPostView(
				post.getId(),
				english ? post.getTitleEn() : post.getTitleRu(),
				english ? post.getBodyEn() : post.getBodyRu(),
				post.getAuthor().getUsername(),
				post.getCreatedAt()
		);
	}
}
