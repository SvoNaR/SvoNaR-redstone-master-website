package ru.redstonemaster.web.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.notification.NotificationService;
import ru.redstonemaster.web.notification.NotificationType;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.service.TutorialContentService;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;
import ru.redstonemaster.web.user.UserRole;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LessonCommentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LessonCommentService.class);

	private final LessonCommentRepository commentRepository;
	private final UserMuteRepository muteRepository;
	private final UserRepository userRepository;
	private final AvatarService avatarService;
	private final TutorialContentService tutorialContentService;
	private final NotificationService notificationService;

	public LessonCommentService(
			LessonCommentRepository commentRepository,
			UserMuteRepository muteRepository,
			UserRepository userRepository,
			AvatarService avatarService,
			TutorialContentService tutorialContentService,
			NotificationService notificationService
	) {
		this.commentRepository = commentRepository;
		this.muteRepository = muteRepository;
		this.userRepository = userRepository;
		this.avatarService = avatarService;
		this.tutorialContentService = tutorialContentService;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public List<LessonCommentView> listForLesson(String sectionId, String lessonId, User viewer) {
		this.requireLessonExists(sectionId, lessonId);
		List<LessonComment> comments = this.commentRepository
				.findBySectionIdAndLessonIdAndDeletedFalseOrderByCreatedAtAsc(sectionId, lessonId);
		Map<Long, User> usersById = this.loadAuthors(comments);
		Map<Long, String> usernamesById = new HashMap<>();
		usersById.forEach((id, user) -> usernamesById.put(id, user.getUsername()));
		boolean canModerate = viewer != null && this.canModerate(viewer);
		Long viewerId = viewer != null ? viewer.getId() : null;

		List<LessonCommentView> views = new ArrayList<>();
		for (LessonComment comment : comments) {
			User author = usersById.get(comment.getAuthorId());
			if (author == null) {
				continue;
			}
			String replyToUsername = null;
			if (comment.getReplyToUserId() != null) {
				replyToUsername = usernamesById.get(comment.getReplyToUserId());
			}
			views.add(new LessonCommentView(
					comment.getId(),
					author.getUsername(),
					this.avatarService.getAvatarUrl(author),
					comment.getBody(),
					comment.getCreatedAt(),
					comment.getParentCommentId(),
					replyToUsername,
					this.canDeleteComment(viewer, author),
					canModerate && this.canMuteTarget(viewer, author),
					viewerId != null && viewerId.equals(comment.getAuthorId())
			));
		}
		return views;
	}

	@Transactional
	public void postComment(User author, String sectionId, String lessonId, String body, Long parentCommentId) {
		this.requireLessonExists(sectionId, lessonId);
		this.ensureNotMuted(author);
		String normalizedBody = this.normalizeBody(body);
		Long replyToUserId = null;
		if (parentCommentId != null) {
			LessonComment parent = this.commentRepository.findById(parentCommentId)
					.orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
			if (parent.isDeleted()) {
				throw new IllegalArgumentException("Parent comment not found");
			}
			if (!parent.getSectionId().equals(sectionId) || !parent.getLessonId().equals(lessonId)) {
				throw new IllegalArgumentException("Parent comment belongs to another lesson");
			}
			replyToUserId = parent.getAuthorId();
		}
		LessonComment saved = this.commentRepository.save(new LessonComment(
				sectionId,
				lessonId,
				author.getId(),
				normalizedBody,
				parentCommentId,
				replyToUserId
		));
		if (replyToUserId != null && !replyToUserId.equals(author.getId())) {
			this.notifyCommentReply(author, replyToUserId, saved, sectionId, lessonId);
		}
	}

	@Transactional
	public void deleteComment(User actor, long commentId) {
		LessonComment comment = this.commentRepository.findById(commentId)
				.orElseThrow(() -> new IllegalArgumentException("Comment not found"));
		User author = this.userRepository.findById(comment.getAuthorId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (!this.canDeleteComment(actor, author)) {
			throw new IllegalArgumentException("Cannot delete this comment");
		}
		if (!comment.isDeleted()) {
			comment.markDeleted();
		}
	}

	@Transactional
	public void muteAuthorFromComment(User moderator, long commentId, int minutes, String reason) {
		this.requireModerator(moderator);
		LessonComment comment = this.commentRepository.findById(commentId)
				.orElseThrow(() -> new IllegalArgumentException("Comment not found"));
		User target = this.userRepository.findById(comment.getAuthorId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		this.applyMute(moderator, target, minutes, reason);
	}

	@Transactional
	public void muteUser(User moderator, long targetUserId, int minutes, String reason) {
		this.requireModerator(moderator);
		User target = this.userRepository.findById(targetUserId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		this.applyMute(moderator, target, minutes, reason);
	}

	@Transactional
	public void unmuteUser(User actor, long targetUserId) {
		this.requireModerator(actor);
		User target = this.userRepository.findById(targetUserId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		this.applyUnmute(actor, target);
	}

	@Transactional(readOnly = true)
	public List<ActiveMuteView> listActiveMutesForRole(UserRole role) {
		Instant now = Instant.now();
		Map<Long, UserMute> latestByUser = new LinkedHashMap<>();
		for (UserMute mute : this.muteRepository.findAllActiveMutes(now)) {
			latestByUser.merge(
					mute.getUserId(),
					mute,
					(existing, candidate) -> existing.getMutedUntil().isAfter(candidate.getMutedUntil())
							? existing
							: candidate
			);
		}

		List<ActiveMuteView> views = new ArrayList<>();
		for (UserMute mute : latestByUser.values()) {
			User user = this.userRepository.findById(mute.getUserId()).orElse(null);
			if (user == null || user.getRole() != role) {
				continue;
			}
			String mutedByUsername = this.userRepository.findById(mute.getMutedById())
					.map(User::getUsername)
					.orElse("—");
			views.add(new ActiveMuteView(
					user.getId(),
					user.getUsername(),
					user.getEmail(),
					mute.getMutedUntil(),
					mute.getReason(),
					mutedByUsername
			));
		}
		views.sort(Comparator.comparing(ActiveMuteView::mutedUntil));
		return views;
	}

	@Transactional
	public boolean isMuted(User user) {
		return this.findActiveMute(user.getId()).isPresent();
	}

	@Transactional(readOnly = true)
	public Optional<Instant> getMutedUntil(User user) {
		return this.findActiveMute(user.getId()).map(UserMute::getMutedUntil);
	}

	@Transactional(readOnly = true)
	public long countActiveComments() {
		return this.commentRepository.countByDeletedFalse();
	}

	private void applyMute(User moderator, User target, int minutes, String reason) {
		if (minutes <= 0) {
			throw new IllegalArgumentException("Mute duration must be positive");
		}
		if (target.getId().equals(moderator.getId())) {
			throw new IllegalArgumentException("Cannot mute yourself");
		}
		if (target.getRole() == UserRole.ADMIN) {
			throw new IllegalArgumentException("Cannot mute administrator");
		}
		if (target.getRole() == UserRole.MODERATOR && moderator.getRole() != UserRole.ADMIN) {
			throw new IllegalArgumentException("Only administrator can mute moderators");
		}
		String normalizedReason = this.normalizeReason(reason);
		Instant until = Instant.now().plusSeconds((long) minutes * 60L);
		this.muteRepository.save(new UserMute(target.getId(), moderator.getId(), until, normalizedReason));
		this.muteRepository.flush();
		try {
			this.notificationService.notifyUserMuted(target.getId(), moderator.getUsername(), until, normalizedReason);
		} catch (RuntimeException exception) {
			LOGGER.error(
					"Mute saved for user {}, but notification failed: {}",
					target.getId(),
					exception.getMessage(),
					exception
			);
		}
	}

	private void applyUnmute(User actor, User target) {
		if (target.getId().equals(actor.getId())) {
			throw new IllegalArgumentException("Cannot unmute yourself");
		}
		if (target.getRole() == UserRole.ADMIN) {
			throw new IllegalArgumentException("Cannot unmute administrator");
		}
		if (target.getRole() == UserRole.MODERATOR && actor.getRole() != UserRole.ADMIN) {
			throw new IllegalArgumentException("Only administrator can unmute moderators");
		}
		Instant now = Instant.now();
		List<UserMute> activeMutes = this.muteRepository.findActiveMutesForUser(target.getId(), now);
		if (activeMutes.isEmpty()) {
			throw new IllegalArgumentException("User is not muted");
		}
		Instant expiredAt = now.minusSeconds(1);
		for (UserMute mute : activeMutes) {
			mute.setMutedUntil(expiredAt);
		}
		this.muteRepository.flush();
		try {
			this.notificationService.notifyUserUnmuted(target.getId(), actor.getUsername());
		} catch (RuntimeException exception) {
			LOGGER.error(
					"Unmute saved for user {}, but notification failed: {}",
					target.getId(),
					exception.getMessage(),
					exception
			);
		}
	}

	private Optional<UserMute> findActiveMute(Long userId) {
		if (userId == null) {
			return Optional.empty();
		}
		return this.muteRepository.findActiveMute(userId, Instant.now());
	}

	private void notifyCommentReply(User author, Long replyToUserId, LessonComment comment, String sectionId, String lessonId) {
		User recipient = this.userRepository.findById(replyToUserId).orElse(null);
		if (recipient == null) {
			return;
		}
		String actionPath = "/tutorial/" + sectionId + "/" + lessonId;
		String sourceKey = "comment-reply:" + comment.getId();
		String titleRu = "Ответ на ваш комментарий";
		String titleEn = "Reply to your comment";
		String messageRu = author.getUsername() + ": " + comment.getBody();
		String messageEn = author.getUsername() + ": " + comment.getBody();
		this.notificationService.notifyCommentEvent(
				recipient,
				sourceKey,
				NotificationType.COMMENT_REPLY,
				titleRu,
				titleEn,
				messageRu,
				messageEn,
				actionPath
		);
	}

	private void ensureNotMuted(User user) {
		Optional<UserMute> activeMute = this.findActiveMute(user.getId());
		if (activeMute.isPresent()) {
			throw new IllegalStateException(this.formatMuteRestrictionMessage(activeMute.get().getMutedUntil()));
		}
	}

	private String formatMuteRestrictionMessage(Instant mutedUntil) {
		return "Вы не можете оставлять комментарии до " + mutedUntil + " (UTC)";
	}

	private String normalizeBody(String body) {
		if (body == null) {
			throw new IllegalArgumentException("Comment is required");
		}
		String normalized = body.trim().replaceAll("\\s+", " ");
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("Comment is required");
		}
		if (normalized.length() > LessonComment.MAX_BODY_LENGTH) {
			throw new IllegalArgumentException("Comment must be at most " + LessonComment.MAX_BODY_LENGTH + " characters");
		}
		return normalized;
	}

	private String normalizeReason(String reason) {
		if (reason == null || reason.trim().isEmpty()) {
			throw new IllegalArgumentException("Mute reason is required");
		}
		String normalized = reason.trim().replaceAll("\\s+", " ");
		if (normalized.length() > 500) {
			throw new IllegalArgumentException("Reason must be at most 500 characters");
		}
		return normalized;
	}

	private void requireLessonExists(String sectionId, String lessonId) {
		if (this.tutorialContentService.findLesson(ru.redstonemaster.web.locale.WebLocale.RU, sectionId, lessonId).isEmpty()) {
			throw new IllegalArgumentException("Lesson not found");
		}
	}

	private void requireModerator(User user) {
		if (!this.canModerate(user)) {
			throw new IllegalArgumentException("Moderator access required");
		}
	}

	private boolean canModerate(User user) {
		return user.getRole() == UserRole.MODERATOR || user.getRole() == UserRole.ADMIN;
	}

	private boolean canMuteTarget(User moderator, User target) {
		if (moderator == null) {
			return false;
		}
		if (target.getRole() == UserRole.ADMIN) {
			return false;
		}
		return target.getRole() != UserRole.MODERATOR || moderator.getRole() == UserRole.ADMIN;
	}

	private boolean canDeleteComment(User actor, User author) {
		if (actor == null) {
			return false;
		}
		if (actor.getId().equals(author.getId())) {
			return true;
		}
		if (!this.canModerate(actor)) {
			return false;
		}
		if (actor.getRole() == UserRole.ADMIN) {
			return true;
		}
		return author.getRole() == UserRole.USER;
	}

	private Map<Long, User> loadAuthors(List<LessonComment> comments) {
		Map<Long, User> users = new HashMap<>();
		for (LessonComment comment : comments) {
			if (!users.containsKey(comment.getAuthorId())) {
				this.userRepository.findById(comment.getAuthorId()).ifPresent(user -> users.put(user.getId(), user));
			}
			if (comment.getReplyToUserId() != null && !users.containsKey(comment.getReplyToUserId())) {
				this.userRepository.findById(comment.getReplyToUserId())
						.ifPresent(user -> users.put(user.getId(), user));
			}
		}
		return users;
	}
}
