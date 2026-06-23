package ru.redstonemaster.web.moderation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonSubmissionRepository extends JpaRepository<LessonSubmission, Long> {
	List<LessonSubmission> findByModeratorUserIdOrderByCreatedAtDesc(Long moderatorUserId);

	List<LessonSubmission> findByStatusOrderBySubmittedAtDesc(LessonSubmissionStatus status);
}
