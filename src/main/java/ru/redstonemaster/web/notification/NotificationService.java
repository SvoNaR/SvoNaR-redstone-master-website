package ru.redstonemaster.web.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.moderation.LessonSubmission;
import ru.redstonemaster.web.news.NewsPost;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;
import ru.redstonemaster.web.user.UserRole;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

	private static final String SOURCE_AVATAR = "system:avatar";

	private final UserNotificationRepository notificationRepository;
	private final UserRepository userRepository;

	public NotificationService(
			UserNotificationRepository notificationRepository,
			UserRepository userRepository
	) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public void syncForUser(User user) {
		if (!user.isProfileIntroSeen()) {
			this.ensureNotification(
					user,
					SOURCE_AVATAR,
					NotificationType.AVATAR_SETUP,
					"Настройте аватарку",
					"Set up your avatar",
					"Загрузите скин Minecraft 64×64 или готовую аватарку 8×8 для профиля.",
					"Upload a Minecraft skin (64×64) or a ready-made 8×8 avatar for your profile.",
					"/profile/avatar",
					"Сменить аватарку",
					"Change avatar"
			);
		} else {
			this.autoResolve(user, SOURCE_AVATAR);
		}
	}

	@Transactional
	public void notifyNewsPublished(NewsPost news) {
		String sourceKey = newsSourceKey(news.getId());
		String titleRu = "Последняя новость: " + news.getTitleRu();
		String titleEn = "Latest post: " + news.getTitleEn();
		String messageRu = this.truncate(news.getBodyRu(), 220);
		String messageEn = this.truncate(news.getBodyEn(), 220);
		String actionPath = "/news/" + news.getId();

		for (User user : this.userRepository.findAll()) {
			if (this.notificationRepository.findByUserIdAndSourceKey(user.getId(), sourceKey).isPresent()) {
				continue;
			}
			this.notificationRepository.save(new UserNotification(
					user,
					NotificationType.NEWS,
					sourceKey,
					titleRu,
					titleEn,
					messageRu,
					messageEn,
					actionPath,
					"Читать",
					"Read"
			));
		}
	}

	@Transactional
	public void deleteNewsNotifications(Long newsId) {
		this.notificationRepository.deleteBySourceKey(newsSourceKey(newsId));
	}

	public static String newsSourceKey(Long newsId) {
		return "news:" + newsId;
	}

	public static String lessonSubmissionSourceKey(Long submissionId) {
		return "lesson-submission:" + submissionId;
	}

	@Transactional
	public void notifyLessonSubmissionPending(LessonSubmission submission) {
		String sourceKey = lessonSubmissionSourceKey(submission.getId());
		String titleRu = "Новый урок на проверке";
		String titleEn = "New lesson pending review";
		String messageRu = "Модератор отправил урок «" + submission.getTitleRu() + "» ("
				+ submission.getSectionId() + "/" + submission.getLessonId() + ").";
		String messageEn = "Moderator submitted lesson \"" + submission.getTitleEn() + "\" ("
				+ submission.getSectionId() + "/" + submission.getLessonId() + ").";
		String actionPath = "/admin/lesson-submissions";

		for (User user : this.userRepository.findAll()) {
			if (user.getRole() != UserRole.ADMIN) {
				continue;
			}
			if (this.notificationRepository.findByUserIdAndSourceKey(user.getId(), sourceKey).isPresent()) {
				continue;
			}
			this.notificationRepository.save(new UserNotification(
					user,
					NotificationType.LESSON_MODERATION,
					sourceKey,
					titleRu,
					titleEn,
					messageRu,
					messageEn,
					actionPath,
					"Проверить",
					"Review"
			));
		}
	}

	@Transactional
	public void notifyCommentEvent(
			User recipient,
			String sourceKey,
			NotificationType type,
			String titleRu,
			String titleEn,
			String messageRu,
			String messageEn,
			String actionPath
	) {
		this.ensureNotification(
				recipient,
				sourceKey,
				type,
				titleRu,
				titleEn,
				messageRu,
				messageEn,
				actionPath,
				"Открыть",
				"Open"
		);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void notifyUserMuted(Long targetUserId, String moderatorUsername, Instant mutedUntil, String reason) {
		User target = this.userRepository.findById(targetUserId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		String untilLabel = MUTE_UNTIL_FORMATTER.format(mutedUntil);
		String sourceKey = "user-mute:" + targetUserId + ":" + System.nanoTime();
		String titleRu = "Ограничение комментариев";
		String titleEn = "Comment restriction";
		String messageRu = this.truncate(
				"Модератор " + moderatorUsername + " ограничил вам комментарии до "
						+ untilLabel + " (UTC). Причина: " + reason,
				1024
		);
		String messageEn = this.truncate(
				"Moderator " + moderatorUsername + " restricted your comments until "
						+ untilLabel + " (UTC). Reason: " + reason,
				1024
		);
		this.notificationRepository.save(new UserNotification(
				target,
				NotificationType.USER_MUTE,
				sourceKey,
				titleRu,
				titleEn,
				messageRu,
				messageEn,
				"/notifications",
				"Открыть",
				"Open"
		));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void notifyUserUnmuted(Long targetUserId, String actorUsername) {
		User target = this.userRepository.findById(targetUserId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		String sourceKey = "user-unmute:" + targetUserId + ":" + System.nanoTime();
		String titleRu = "Ограничение комментариев снято";
		String titleEn = "Comment restriction lifted";
		String messageRu = "Модератор " + actorUsername + " снял ограничение на комментарии к урокам.";
		String messageEn = "Moderator " + actorUsername + " lifted your lesson comment restriction.";
		this.notificationRepository.save(new UserNotification(
				target,
				NotificationType.USER_UNMUTE,
				sourceKey,
				titleRu,
				titleEn,
				messageRu,
				messageEn,
				"/notifications",
				"Открыть",
				"Open"
		));
	}

	private static final DateTimeFormatter MUTE_UNTIL_FORMATTER =
			DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.of("UTC"));

	@Transactional
	public void notifyLessonSubmissionReviewed(LessonSubmission submission, boolean approved) {
		User moderator = this.userRepository.findById(submission.getModeratorUserId())
				.orElse(null);
		if (moderator == null) {
			return;
		}
		String sourceKey = lessonSubmissionSourceKey(submission.getId()) + ":review";
		String titleRu = approved ? "Урок одобрен" : "Урок отклонён";
		String titleEn = approved ? "Lesson approved" : "Lesson rejected";
		String messageRu = "Администратор проверил урок «" + submission.getTitleRu() + "».";
		String messageEn = "Administrator reviewed lesson \"" + submission.getTitleEn() + "\".";
		if (submission.getReviewComment() != null && !submission.getReviewComment().isBlank()) {
			messageRu += " Комментарий: " + submission.getReviewComment();
			messageEn += " Comment: " + submission.getReviewComment();
		}
		this.ensureNotification(
				moderator,
				sourceKey,
				NotificationType.LESSON_MODERATION,
				titleRu,
				titleEn,
				messageRu,
				messageEn,
				"/moderation/submissions",
				"Открыть",
				"Open"
		);
	}

	@Transactional(readOnly = true)
	public List<NotificationView> getActiveNotifications(User user, String langCode) {
		return this.notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId()).stream()
				.map(notification -> NotificationView.from(notification, langCode))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<NotificationView> getReadNotifications(User user, String langCode) {
		return this.notificationRepository.findByUserIdAndReadTrueOrderByCreatedAtDesc(user.getId()).stream()
				.map(notification -> NotificationView.from(notification, langCode))
				.toList();
	}

	@Transactional(readOnly = true)
	public int getNotificationCount(User user) {
		return (int) this.notificationRepository.countByUserIdAndReadFalse(user.getId());
	}

	public String formatBadgeCount(int count) {
		if (count <= 0) {
			return "";
		}
		if (count > 9) {
			return "9+";
		}
		return Integer.toString(count);
	}

	@Transactional
	public void markAsRead(User user, Long notificationId) {
		UserNotification notification = this.findOwnedNotification(user, notificationId);
		if (!notification.isRead()) {
			notification.markRead();
		}
	}

	@Transactional
	public void markAllAsRead(User user) {
		for (UserNotification notification : this.notificationRepository
				.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId())) {
			notification.markRead();
		}
	}

	@Transactional
	public String openNewsNotification(User user, Long notificationId, String langCode) {
		UserNotification notification = this.findOwnedNotification(user, notificationId);
		if (notification.getType() != NotificationType.NEWS) {
			throw new IllegalArgumentException("Open action is only supported for news notifications");
		}
		if (!notification.isRead()) {
			notification.markRead();
		}
		return notification.getActionPath() + "?lang=" + langCode;
	}

	@Transactional
	public void deleteNotification(User user, Long notificationId) {
		this.notificationRepository.delete(this.findOwnedNotification(user, notificationId));
	}

	private UserNotification findOwnedNotification(User user, Long notificationId) {
		return this.notificationRepository.findByIdAndUserId(notificationId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("Notification not found"));
	}

	private void ensureNotification(
			User user,
			String sourceKey,
			NotificationType type,
			String titleRu,
			String titleEn,
			String messageRu,
			String messageEn,
			String actionPath,
			String actionLabelRu,
			String actionLabelEn
	) {
		if (this.notificationRepository.findByUserIdAndSourceKey(user.getId(), sourceKey).isPresent()) {
			return;
		}
		this.notificationRepository.save(new UserNotification(
				user,
				type,
				sourceKey,
				titleRu,
				titleEn,
				messageRu,
				messageEn,
				actionPath,
				actionLabelRu,
				actionLabelEn
		));
	}

	private void autoResolve(User user, String sourceKey) {
		this.notificationRepository.findByUserIdAndSourceKey(user.getId(), sourceKey)
				.filter(notification -> !notification.isRead())
				.ifPresent(notification -> notification.markRead());
	}

	private String truncate(String text, int maxLength) {
		if (text == null) {
			return "";
		}
		String normalized = text.trim().replaceAll("\\s+", " ");
		if (normalized.length() <= maxLength) {
			return normalized;
		}
		return normalized.substring(0, maxLength - 1) + "…";
	}
}
