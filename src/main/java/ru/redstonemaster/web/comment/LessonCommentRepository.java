package ru.redstonemaster.web.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonCommentRepository extends JpaRepository<LessonComment, Long> {
	List<LessonComment> findBySectionIdAndLessonIdAndDeletedFalseOrderByCreatedAtAsc(String sectionId, String lessonId);

	long countByDeletedFalse();
}
