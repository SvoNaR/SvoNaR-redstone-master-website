package ru.redstonemaster.web.tutorial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLessonCompletionRepository extends JpaRepository<UserLessonCompletion, Long> {
	List<UserLessonCompletion> findByUserId(Long userId);

	long countByUserId(Long userId);

	boolean existsByUserIdAndSectionIdAndLessonId(Long userId, String sectionId, String lessonId);
}
