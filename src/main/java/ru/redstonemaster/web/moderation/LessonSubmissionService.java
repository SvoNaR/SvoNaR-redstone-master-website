package ru.redstonemaster.web.moderation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.notification.NotificationService;
import ru.redstonemaster.web.notification.NotificationType;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;
import ru.redstonemaster.web.user.UserRole;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class LessonSubmissionService {

	private final LessonSubmissionRepository submissionRepository;
	private final UserRepository userRepository;
	private final ModLessonJarService jarService;
	private final PseudoVideoArchiveService archiveService;
	private final NotificationService notificationService;

	public LessonSubmissionService(
			LessonSubmissionRepository submissionRepository,
			UserRepository userRepository,
			ModLessonJarService jarService,
			PseudoVideoArchiveService archiveService,
			NotificationService notificationService
	) {
		this.submissionRepository = submissionRepository;
		this.userRepository = userRepository;
		this.jarService = jarService;
		this.archiveService = archiveService;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public List<LessonSubmission> getModeratorSubmissions(User moderator) {
		return this.submissionRepository.findByModeratorUserIdOrderByCreatedAtDesc(moderator.getId());
	}

	@Transactional(readOnly = true)
	public List<LessonSubmission> getPendingSubmissions() {
		return this.submissionRepository.findByStatusOrderBySubmittedAtDesc(LessonSubmissionStatus.PENDING_REVIEW);
	}

	@Transactional(readOnly = true)
	public LessonSubmission getOwnedSubmission(User moderator, Long submissionId) {
		LessonSubmission submission = this.submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("Submission not found"));
		if (!submission.getModeratorUserId().equals(moderator.getId())) {
			throw new IllegalArgumentException("Submission not found");
		}
		return submission;
	}

	@Transactional
	public LessonSubmission saveDraft(LessonSubmission submission) {
		return this.submissionRepository.save(submission);
	}

	@Transactional
	public Path buildVideoOnlyJar(User moderator, Long submissionId) throws IOException {
		LessonSubmission submission = this.getOwnedSubmission(moderator, submissionId);
		if (!this.jarService.workspaceHasVideoFrames(submission)) {
			throw new IllegalStateException("Convert a video to PNG frames before building the JAR");
		}
		return this.jarService.buildVideoOnlyJar(submission);
	}

	@Transactional(readOnly = true)
	public Path buildFramesZip(User moderator, Long submissionId) throws IOException {
		LessonSubmission submission = this.getOwnedSubmission(moderator, submissionId);
		return this.archiveService.buildFramesZip(submission);
	}

	@Transactional
	public LessonSubmission buildJar(User moderator, Long submissionId) throws IOException {
		LessonSubmission submission = this.getOwnedSubmission(moderator, submissionId);
		if (!this.jarService.workspaceHasVideoFrames(submission)) {
			throw new IllegalStateException("Convert a video to PNG frames before building the JAR");
		}
		Path jarPath = this.jarService.buildLessonJar(submission);
		submission.setJarPath(jarPath.toString());
		return this.submissionRepository.save(submission);
	}

	@Transactional
	public LessonSubmission submitForReview(User moderator, Long submissionId) throws IOException {
		LessonSubmission submission = this.getOwnedSubmission(moderator, submissionId);
		if (submission.getJarPath() == null || submission.getJarPath().isBlank()) {
			submission = this.buildJar(moderator, submissionId);
		}
		submission.markPendingReview();
		submission = this.submissionRepository.save(submission);
		this.notificationService.notifyLessonSubmissionPending(submission);
		return submission;
	}

	@Transactional
	public LessonSubmission review(Long submissionId, boolean approve, String comment) {
		LessonSubmission submission = this.submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("Submission not found"));
		if (submission.getStatus() != LessonSubmissionStatus.PENDING_REVIEW) {
			throw new IllegalStateException("Submission is not pending review");
		}
		if (approve) {
			submission.approve(comment);
		} else {
			submission.reject(comment);
		}
		submission = this.submissionRepository.save(submission);
		this.notificationService.notifyLessonSubmissionReviewed(submission, approve);
		return submission;
	}

	@Transactional(readOnly = true)
	public LessonSubmission getSubmission(Long submissionId) {
		return this.submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("Submission not found"));
	}

	@Transactional(readOnly = true)
	public User getModerator(LessonSubmission submission) {
		return this.userRepository.findById(submission.getModeratorUserId())
				.orElseThrow(() -> new IllegalArgumentException("Moderator not found"));
	}

	@Transactional(readOnly = true)
	public List<User> getAdmins() {
		return this.userRepository.findAll().stream()
				.filter(user -> user.getRole() == UserRole.ADMIN)
				.toList();
	}
}
