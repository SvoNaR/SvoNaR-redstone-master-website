package ru.redstonemaster.web.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.news.NewsPost;
import ru.redstonemaster.web.news.NewsService;
import ru.redstonemaster.web.news.PublishNewsForm;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class NotificationServiceTest {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NewsService newsService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void syncCreatesAvatarNotificationForNewUser() {
		User user = this.userRepository.save(new User("notify_test", "notify@test.com", "hash"));
		user.setEmailVerified(true);
		user.setProfileIntroSeen(false);
		this.userRepository.save(user);

		this.notificationService.syncForUser(user);

		assertEquals(1, this.notificationService.getNotificationCount(user));
		assertEquals(1, this.notificationService.getActiveNotifications(user, "ru").size());
	}

	@Test
	void markAsReadMovesNotificationOutOfActiveList() {
		User user = this.userRepository.save(new User("notify_read", "read@test.com", "hash"));
		user.setEmailVerified(true);
		user.setProfileIntroSeen(false);
		this.userRepository.save(user);
		this.notificationService.syncForUser(user);
		long notificationId = this.notificationService.getActiveNotifications(user, "ru").getFirst().id();

		this.notificationService.markAsRead(user, notificationId);

		assertEquals(0, this.notificationService.getNotificationCount(user));
		assertEquals(1, this.notificationService.getReadNotifications(user, "ru").size());
	}

	@Test
	void openNewsNotificationMarksItAsRead() {
		User author = this.userRepository.save(new User("news_notify_author", "news-author@test.com", "hash"));
		author.setEmailVerified(true);
		this.userRepository.save(author);

		User reader = this.userRepository.save(new User("news_notify_reader", "news-reader@test.com", "hash"));
		reader.setEmailVerified(true);
		this.userRepository.save(reader);

		PublishNewsForm form = new PublishNewsForm();
		form.setTitleRu("Заголовок");
		form.setTitleEn("Title");
		form.setBodyRu("Текст новости");
		form.setBodyEn("News body");

		NewsPost post = this.newsService.publish(form, author);
		long notificationId = this.notificationService.getActiveNotifications(reader, "ru").getFirst().id();

		String target = this.notificationService.openNewsNotification(reader, notificationId, "ru");

		assertEquals("/news/" + post.getId() + "?lang=ru", target);
		assertEquals(0, this.notificationService.getNotificationCount(reader));
		assertEquals(1, this.notificationService.getReadNotifications(reader, "ru").size());
		assertTrue(this.notificationService.getReadNotifications(reader, "ru").getFirst().read());
	}

	@Test
	void markAllAsReadMovesAllActiveNotificationsToRead() {
		User author = this.userRepository.save(new User("notify_all_author", "all-author@test.com", "hash"));
		author.setEmailVerified(true);
		this.userRepository.save(author);

		User user = this.userRepository.save(new User("notify_all_reader", "all-reader@test.com", "hash"));
		user.setEmailVerified(true);
		user.setProfileIntroSeen(false);
		this.userRepository.save(user);
		this.notificationService.syncForUser(user);

		PublishNewsForm form = new PublishNewsForm();
		form.setTitleRu("Заголовок");
		form.setTitleEn("Title");
		form.setBodyRu("Текст");
		form.setBodyEn("Body");
		this.newsService.publish(form, author);

		assertEquals(2, this.notificationService.getNotificationCount(user));

		this.notificationService.markAllAsRead(user);

		assertEquals(0, this.notificationService.getNotificationCount(user));
		assertEquals(2, this.notificationService.getReadNotifications(user, "ru").size());
	}

	@Test
	void deleteNotificationRemovesItForUser() {
		User author = this.userRepository.save(new User("notify_del_author", "del-author@test.com", "hash"));
		author.setEmailVerified(true);
		this.userRepository.save(author);

		User user = this.userRepository.save(new User("notify_del_reader", "del-reader@test.com", "hash"));
		user.setEmailVerified(true);
		this.userRepository.save(user);

		PublishNewsForm form = new PublishNewsForm();
		form.setTitleRu("Заголовок");
		form.setTitleEn("Title");
		form.setBodyRu("Текст");
		form.setBodyEn("Body");
		this.newsService.publish(form, author);

		long notificationId = this.notificationService.getActiveNotifications(user, "ru").getFirst().id();
		assertEquals(1, this.notificationService.getNotificationCount(user));

		this.notificationService.deleteNotification(user, notificationId);

		assertEquals(0, this.notificationService.getNotificationCount(user));
		assertEquals(0, this.notificationService.getActiveNotifications(user, "ru").size());
		assertEquals(0, this.notificationService.getReadNotifications(user, "ru").size());
	}

	@Test
	void formatBadgeCount() {
		assertEquals("9+", this.notificationService.formatBadgeCount(10));
		assertEquals("", this.notificationService.formatBadgeCount(0));
	}
}
