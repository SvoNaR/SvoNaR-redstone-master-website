package ru.redstonemaster.web.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "lesson_comments")
public class LessonComment {

	public static final int MAX_BODY_LENGTH = 100;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "section_id", nullable = false, length = 64)
	private String sectionId;

	@Column(name = "lesson_id", nullable = false, length = 64)
	private String lessonId;

	@Column(name = "author_id", nullable = false)
	private Long authorId;

	@Column(nullable = false, length = 100)
	private String body;

	@Column(name = "parent_comment_id")
	private Long parentCommentId;

	@Column(name = "reply_to_user_id")
	private Long replyToUserId;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	@Column(nullable = false)
	private boolean deleted = false;

	protected LessonComment() {
	}

	public LessonComment(
			String sectionId,
			String lessonId,
			Long authorId,
			String body,
			Long parentCommentId,
			Long replyToUserId
	) {
		this.sectionId = sectionId;
		this.lessonId = lessonId;
		this.authorId = authorId;
		this.body = body;
		this.parentCommentId = parentCommentId;
		this.replyToUserId = replyToUserId;
	}

	public Long getId() {
		return this.id;
	}

	public String getSectionId() {
		return this.sectionId;
	}

	public String getLessonId() {
		return this.lessonId;
	}

	public Long getAuthorId() {
		return this.authorId;
	}

	public String getBody() {
		return this.body;
	}

	public Long getParentCommentId() {
		return this.parentCommentId;
	}

	public Long getReplyToUserId() {
		return this.replyToUserId;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public void markDeleted() {
		this.deleted = true;
	}
}
