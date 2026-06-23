package ru.redstonemaster.web.news;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.notification.NotificationService;
import ru.redstonemaster.web.notification.UserNotificationRepository;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class NewsServiceTest {

	@Autowired
	private NewsService newsService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private UserNotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void deleteRemovesNewsAndRelatedNotifications() {
		User author = this.userRepository.save(new User("news_author", "author@test.com", "hash"));
		author.setEmailVerified(true);
		this.userRepository.save(author);

		User reader = this.userRepository.save(new User("news_reader", "reader@test.com", "hash"));
		reader.setEmailVerified(true);
		this.userRepository.save(reader);

		PublishNewsForm form = new PublishNewsForm();
		form.setTitleRu("Заголовок");
		form.setTitleEn("Title");
		form.setBodyRu("Текст новости");
		form.setBodyEn("News body");

		NewsPost post = this.newsService.publish(form, author);
		assertEquals(1, this.notificationService.getNotificationCount(reader));

		assertTrue(this.newsService.delete(post.getId()));
		assertFalse(this.newsService.findPostById(post.getId()).isPresent());
		assertEquals(0, this.notificationService.getNotificationCount(reader));
		assertEquals(0, this.notificationRepository.findAll().stream()
				.filter(notification -> NotificationService.newsSourceKey(post.getId()).equals(notification.getSourceKey()))
				.count());
	}

	@Test
	void updateChangesNewsContent() {
		User author = this.userRepository.save(new User("news_editor", "editor@test.com", "hash"));
		author.setEmailVerified(true);
		this.userRepository.save(author);

		PublishNewsForm publishForm = new PublishNewsForm();
		publishForm.setTitleRu("Старое");
		publishForm.setTitleEn("Old");
		publishForm.setBodyRu("Старый текст");
		publishForm.setBodyEn("Old text");

		NewsPost post = this.newsService.publish(publishForm, author);

		PublishNewsForm editForm = new PublishNewsForm();
		editForm.setTitleRu("Новое");
		editForm.setTitleEn("New");
		editForm.setBodyRu("Новый текст");
		editForm.setBodyEn("New text");

		this.newsService.update(post.getId(), editForm);

		NewsPost updated = this.newsService.findPostById(post.getId()).orElseThrow();
		assertEquals("Новое", updated.getTitleRu());
		assertEquals("New", updated.getTitleEn());
		assertEquals("Новый текст", updated.getBodyRu());
		assertEquals("New text", updated.getBodyEn());
	}
}
