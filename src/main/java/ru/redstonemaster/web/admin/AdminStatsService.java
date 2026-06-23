package ru.redstonemaster.web.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.comment.LessonCommentRepository;
import ru.redstonemaster.web.news.NewsPostRepository;
import ru.redstonemaster.web.tutorial.UserLessonCompletionRepository;
import ru.redstonemaster.web.user.UserRepository;
import ru.redstonemaster.web.user.UserRole;

@Service
public class AdminStatsService {

	private final UserRepository userRepository;
	private final LessonCommentRepository commentRepository;
	private final UserLessonCompletionRepository completionRepository;
	private final NewsPostRepository newsPostRepository;

	public AdminStatsService(
			UserRepository userRepository,
			LessonCommentRepository commentRepository,
			UserLessonCompletionRepository completionRepository,
			NewsPostRepository newsPostRepository
	) {
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
		this.completionRepository = completionRepository;
		this.newsPostRepository = newsPostRepository;
	}

	@Transactional(readOnly = true)
	public AdminStatsView getStats() {
		long users = this.userRepository.findByRoleOrderByUsernameAsc(UserRole.USER).size();
		long moderators = this.userRepository.findByRoleOrderByUsernameAsc(UserRole.MODERATOR).size();
		long admins = this.userRepository.findByRoleOrderByUsernameAsc(UserRole.ADMIN).size();
		return new AdminStatsView(
				users,
				moderators,
				admins,
				this.commentRepository.countByDeletedFalse(),
				this.completionRepository.count(),
				this.newsPostRepository.count()
		);
	}
}
