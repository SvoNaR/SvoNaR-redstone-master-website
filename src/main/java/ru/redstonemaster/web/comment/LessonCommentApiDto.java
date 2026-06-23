package ru.redstonemaster.web.comment;

public record LessonCommentApiDto(
		long id,
		String username,
		String avatarUrl,
		String body,
		String createdAt,
		Long parentCommentId,
		String replyToUsername
) {
	public static LessonCommentApiDto from(LessonCommentView view) {
		return new LessonCommentApiDto(
				view.id(),
				view.username(),
				view.avatarUrl(),
				view.body(),
				view.createdAt().toString(),
				view.parentCommentId(),
				view.replyToUsername()
		);
	}
}
