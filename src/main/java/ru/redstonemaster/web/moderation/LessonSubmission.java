package ru.redstonemaster.web.moderation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ru.redstonemaster.web.user.User;

import java.time.Instant;

@Entity
@Table(name = "lesson_submissions")
public class LessonSubmission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long moderatorUserId;

	@Column(nullable = false, length = 64)
	private String sectionId;

	@Column(nullable = false, length = 64)
	private String lessonId;

	@Column(nullable = false, length = 64)
	private String videoId;

	@Column(nullable = false, length = 256)
	private String titleRu;

	@Column(nullable = false, length = 256)
	private String titleEn;

	@Column(nullable = false, length = 16000)
	private String bodyRu;

	@Column(nullable = false, length = 16000)
	private String bodyEn;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private LessonSubmissionStatus status = LessonSubmissionStatus.DRAFT;

	@Column(nullable = false, length = 512)
	private String workspacePath;

	@Column(length = 512)
	private String jarPath;

	@Column(length = 1024)
	private String reviewComment;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	private Instant submittedAt;
	private Instant reviewedAt;

	protected LessonSubmission() {
	}

	public LessonSubmission(
			User moderator,
			String sectionId,
			String lessonId,
			String videoId,
			String titleRu,
			String titleEn,
			String bodyRu,
			String bodyEn,
			String workspacePath
	) {
		this.moderatorUserId = moderator.getId();
		this.sectionId = sectionId;
		this.lessonId = lessonId;
		this.videoId = videoId;
		this.titleRu = titleRu;
		this.titleEn = titleEn;
		this.bodyRu = bodyRu;
		this.bodyEn = bodyEn;
		this.workspacePath = workspacePath;
	}

	public Long getId() {
		return this.id;
	}

	public Long getModeratorUserId() {
		return this.moderatorUserId;
	}

	public String getSectionId() {
		return this.sectionId;
	}

	public String getLessonId() {
		return this.lessonId;
	}

	public String getVideoId() {
		return this.videoId;
	}

	public String getTitleRu() {
		return this.titleRu;
	}

	public String getTitleEn() {
		return this.titleEn;
	}

	public String getBodyRu() {
		return this.bodyRu;
	}

	public String getBodyEn() {
		return this.bodyEn;
	}

	public LessonSubmissionStatus getStatus() {
		return this.status;
	}

	public String getWorkspacePath() {
		return this.workspacePath;
	}

	public String getJarPath() {
		return this.jarPath;
	}

	public String getReviewComment() {
		return this.reviewComment;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public Instant getSubmittedAt() {
		return this.submittedAt;
	}

	public Instant getReviewedAt() {
		return this.reviewedAt;
	}

	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}

	public void markPendingReview() {
		this.status = LessonSubmissionStatus.PENDING_REVIEW;
		this.submittedAt = Instant.now();
	}

	public void approve(String comment) {
		this.status = LessonSubmissionStatus.APPROVED;
		this.reviewComment = comment;
		this.reviewedAt = Instant.now();
	}

	public void reject(String comment) {
		this.status = LessonSubmissionStatus.REJECTED;
		this.reviewComment = comment;
		this.reviewedAt = Instant.now();
	}
}
