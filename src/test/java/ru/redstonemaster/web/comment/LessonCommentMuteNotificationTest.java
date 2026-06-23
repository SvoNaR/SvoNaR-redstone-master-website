package ru.redstonemaster.web.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.redstonemaster.web.notification.NotificationType;
import ru.redstonemaster.web.notification.UserNotificationRepository;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;
import ru.redstonemaster.web.user.UserRole;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class LessonCommentMuteNotificationTest {

	@Autowired
	private LessonCommentService commentService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserNotificationRepository notificationRepository;

	@Test
	void muteCreatesNotificationForTarget() {
		User author = this.userRepository.save(new User("notify_muted", "notify-muted@test.com", "hash"));
		User moderator = this.userRepository.save(new User("notify_mod", "notify-mod@test.com", "hash"));
		moderator.setRole(UserRole.MODERATOR);
		this.userRepository.save(moderator);

		this.commentService.muteUser(moderator, author.getId(), 30, "rules");

		long notifications = this.notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(author.getId())
				.stream()
				.filter(notification -> notification.getType() == NotificationType.USER_MUTE)
				.count();
		assertEquals(1, notifications);
	}
}
