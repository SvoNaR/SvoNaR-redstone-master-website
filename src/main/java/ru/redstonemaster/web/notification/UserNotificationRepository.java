package ru.redstonemaster.web.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

	List<UserNotification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

	List<UserNotification> findByUserIdAndReadTrueOrderByCreatedAtDesc(Long userId);

	long countByUserIdAndReadFalse(Long userId);

	Optional<UserNotification> findByIdAndUserId(Long id, Long userId);

	Optional<UserNotification> findByUserIdAndSourceKey(Long userId, String sourceKey);

	@Modifying
	@Query("delete from UserNotification n where n.sourceKey = :sourceKey")
	void deleteBySourceKey(@Param("sourceKey") String sourceKey);

	@Modifying
	@Query("delete from UserNotification n where n.user.emailVerified = false")
	void deleteForUnverifiedUsers();
}
