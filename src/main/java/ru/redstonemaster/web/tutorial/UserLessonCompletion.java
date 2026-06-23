package ru.redstonemaster.web.tutorial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
		name = "user_lesson_completions",
		uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "section_id", "lesson_id"})
)
public class UserLessonCompletion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "section_id", nullable = false, length = 64)
	private String sectionId;

	@Column(name = "lesson_id", nullable = false, length = 64)
	private String lessonId;

	@Column(nullable = false)
	private Instant completedAt = Instant.now();

	protected UserLessonCompletion() {
	}

	public UserLessonCompletion(Long userId, String sectionId, String lessonId) {
		this.userId = userId;
		this.sectionId = sectionId;
		this.lessonId = lessonId;
	}

	public Long getUserId() {
		return this.userId;
	}

	public String sectionId() {
		return this.sectionId;
	}

	public String lessonId() {
		return this.lessonId;
	}

	public String lessonKey() {
		return this.sectionId + ":" + this.lessonId;
	}
}
