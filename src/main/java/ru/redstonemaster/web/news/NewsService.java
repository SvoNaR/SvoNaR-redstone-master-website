package ru.redstonemaster.web.news;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.notification.NotificationService;
import ru.redstonemaster.web.user.User;

import java.util.List;
import java.util.Optional;

@Service
public class NewsService {

	private final NewsPostRepository newsPostRepository;
	private final NotificationService notificationService;

	public NewsService(NewsPostRepository newsPostRepository, NotificationService notificationService) {
		this.newsPostRepository = newsPostRepository;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public List<NewsPostView> getAll(String langCode) {
		return this.newsPostRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(post -> NewsPostView.from(post, langCode))
				.toList();
	}

	@Transactional(readOnly = true)
	public Optional<NewsPostView> findById(Long id, String langCode) {
		return this.newsPostRepository.findById(id)
				.map(post -> NewsPostView.from(post, langCode));
	}

	@Transactional(readOnly = true)
	public Optional<NewsPost> findPostById(Long id) {
		return this.newsPostRepository.findById(id);
	}

	@Transactional
	public NewsPost publish(PublishNewsForm form, User author) {
		NewsPost post = this.newsPostRepository.save(new NewsPost(
				form.getTitleRu(),
				form.getTitleEn(),
				form.getBodyRu(),
				form.getBodyEn(),
				author
		));
		this.notificationService.notifyNewsPublished(post);
		return post;
	}

	@Transactional
	public Optional<NewsPost> update(Long id, PublishNewsForm form) {
		return this.newsPostRepository.findById(id).map(post -> {
			post.updateContent(
					form.getTitleRu(),
					form.getTitleEn(),
					form.getBodyRu(),
					form.getBodyEn()
			);
			return this.newsPostRepository.save(post);
		});
	}

	@Transactional
	public boolean delete(Long id) {
		if (!this.newsPostRepository.existsById(id)) {
			return false;
		}
		this.notificationService.deleteNewsNotifications(id);
		this.newsPostRepository.deleteById(id);
		return true;
	}
}
