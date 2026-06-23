package ru.redstonemaster.web.comment;

import java.time.Instant;

public record LessonCommentView(
		long id,
		String username,
		String avatarUrl,
		String body,
		Instant createdAt,
		Long parentCommentId,
		String replyToUsername,
		boolean canModerate,
		boolean canMuteAuthor,
		boolean ownComment
) {
}
