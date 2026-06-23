package ru.redstonemaster.web.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.notification.UserNotificationRepository;
import ru.redstonemaster.web.profile.AvatarService;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	public static final int ADMIN_PAGE_SIZE = 10;

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final UserNotificationRepository notificationRepository;
	private final PendingRegistrationService pendingRegistrationService;
	private final PasswordEncoder passwordEncoder;
	private final AvatarService avatarService;
	private final String adminUsername;
	private final String adminEmail;
	private final String adminPassword;

	public UserService(
			UserRepository userRepository,
			UserNotificationRepository notificationRepository,
			PendingRegistrationService pendingRegistrationService,
			PasswordEncoder passwordEncoder,
			AvatarService avatarService,
			@Value("${app.admin.username}") String adminUsername,
			@Value("${app.admin.email}") String adminEmail,
			@Value("${app.admin.password}") String adminPassword
	) {
		this.userRepository = userRepository;
		this.notificationRepository = notificationRepository;
		this.pendingRegistrationService = pendingRegistrationService;
		this.passwordEncoder = passwordEncoder;
		this.avatarService = avatarService;
		this.adminUsername = adminUsername;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
	}

	@Transactional
	public void changeEmail(User user, String newEmail) {
		String normalized = newEmail.trim().toLowerCase();
		if (normalized.equalsIgnoreCase(user.getEmail())) {
			throw new IllegalArgumentException("Email unchanged");
		}
		user.setPendingEmail(normalized);
		this.issuePendingEmailToken(user);
		this.userRepository.save(user);
	}

	@Transactional
	public void issuePendingEmailToken(User user) {
		user.setPendingEmailVerificationToken(this.newToken());
		user.setPendingEmailVerificationExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
	}

	@Transactional
	public Optional<String> verifyEmail(String token) {
		Optional<User> completedRegistration = this.pendingRegistrationService.completeRegistration(token);
		if (completedRegistration.isPresent()) {
			return Optional.of(completedRegistration.get().getUsername());
		}

		Optional<User> userOptional = this.userRepository.findByPendingEmailVerificationToken(token);
		if (userOptional.isEmpty()) {
			return Optional.empty();
		}
		User user = userOptional.get();
		if (user.getPendingEmailVerificationExpiresAt() == null
				|| user.getPendingEmailVerificationExpiresAt().isBefore(Instant.now())
				|| user.getPendingEmail() == null || user.getPendingEmail().isBlank()) {
			return Optional.empty();
		}
		user.setEmail(user.getPendingEmail());
		user.clearPendingEmailChange();
		this.userRepository.save(user);
		return Optional.of(user.getUsername());
	}

	public Optional<User> findByLogin(String login) {
		return this.userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(login.trim(), login.trim().toLowerCase());
	}

	public Optional<User> findByUsername(String username) {
		return this.userRepository.findByUsernameIgnoreCase(username);
	}

	public List<User> findUsersByRole(UserRole role) {
		return this.userRepository.findByRoleOrderByUsernameAsc(role);
	}

	@Transactional(readOnly = true)
	public Page<User> findUsersByRole(UserRole role, String search, int page) {
		String normalizedSearch = search == null ? "" : search.trim();
		int safePage = Math.max(page, 1);
		Page<User> result = this.userRepository.findByRoleAndSearch(
				role,
				normalizedSearch,
				PageRequest.of(safePage - 1, ADMIN_PAGE_SIZE)
		);
		if (result.getTotalPages() > 0 && safePage > result.getTotalPages()) {
			return this.userRepository.findByRoleAndSearch(
					role,
					normalizedSearch,
					PageRequest.of(result.getTotalPages() - 1, ADMIN_PAGE_SIZE)
			);
		}
		return result;
	}

	@Transactional
	public void promoteToModerator(Long userId) {
		User user = this.userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (user.getRole() != UserRole.USER) {
			throw new IllegalStateException("Only regular users can be promoted");
		}
		user.setRole(UserRole.MODERATOR);
	}

	@Transactional
	public void demoteModerator(Long userId) {
		User user = this.userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (user.getRole() != UserRole.MODERATOR) {
			throw new IllegalStateException("Only moderators can be demoted");
		}
		user.setRole(UserRole.USER);
	}

	@Transactional
	public void ensureAdminExists() {
		this.notificationRepository.deleteForUnverifiedUsers();
		this.userRepository.deleteByEmailVerifiedFalse();
		this.pendingRegistrationService.purgeExpired();

		var existing = this.userRepository.findByUsernameIgnoreCase(this.adminUsername);
		if (existing.isPresent()) {
			User admin = existing.get();
			if (admin.getAvatarPath() == null || admin.getAvatarPath().isBlank()) {
				this.avatarService.assignRandomDefaultAvatar(admin);
			}
			return;
		}
		User admin = new User(
				this.adminUsername,
				this.adminEmail.toLowerCase(),
				this.passwordEncoder.encode(this.adminPassword)
		);
		admin.setRole(UserRole.ADMIN);
		admin.setEmailVerified(true);
		this.avatarService.assignRandomDefaultAvatar(admin);
		this.userRepository.save(admin);
	}

	private String newToken() {
		byte[] bytes = new byte[24];
		RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}
}
