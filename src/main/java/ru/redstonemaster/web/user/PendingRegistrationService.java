package ru.redstonemaster.web.user;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.profile.RegisterForm;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class PendingRegistrationService {

	public static final int VERIFICATION_TTL_HOURS = 24;

	private static final SecureRandom RANDOM = new SecureRandom();

	private final PendingRegistrationRepository pendingRegistrationRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AvatarService avatarService;

	public PendingRegistrationService(
			PendingRegistrationRepository pendingRegistrationRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AvatarService avatarService
	) {
		this.pendingRegistrationRepository = pendingRegistrationRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.avatarService = avatarService;
	}

	@Transactional
	public PendingRegistration startRegistration(RegisterForm form) {
		this.purgeExpired();
		String username = form.getUsername().trim();
		String email = form.getEmail().trim().toLowerCase();

		this.pendingRegistrationRepository.findByUsernameIgnoreCase(username)
				.ifPresent(this.pendingRegistrationRepository::delete);
		this.pendingRegistrationRepository.findByEmailIgnoreCase(email)
				.ifPresent(this.pendingRegistrationRepository::delete);

		PendingRegistration pending = new PendingRegistration(
				username,
				email,
				this.passwordEncoder.encode(form.getPassword())
		);
		this.issueVerificationToken(pending);
		return this.pendingRegistrationRepository.save(pending);
	}

	@Transactional
	public Optional<User> completeRegistration(String token) {
		Optional<PendingRegistration> pendingOptional = this.pendingRegistrationRepository
				.findByVerificationToken(token);
		if (pendingOptional.isEmpty()) {
			return Optional.empty();
		}
		PendingRegistration pending = pendingOptional.get();
		if (this.isExpired(pending.getVerificationExpiresAt())) {
			this.pendingRegistrationRepository.delete(pending);
			return Optional.empty();
		}
		if (this.userRepository.existsByUsernameIgnoreCase(pending.getUsername())
				|| this.userRepository.existsByEmailIgnoreCase(pending.getEmail())) {
			this.pendingRegistrationRepository.delete(pending);
			return Optional.empty();
		}

		User user = new User(
				pending.getUsername(),
				pending.getEmail(),
				pending.getPasswordHash()
		);
		user.setEmailVerified(true);
		this.avatarService.assignRandomDefaultAvatar(user);
		User saved = this.userRepository.save(user);
		this.pendingRegistrationRepository.delete(pending);
		return Optional.of(saved);
	}

	@Transactional
	public void changeEmail(PendingRegistration pending, String newEmail) {
		pending.setEmail(newEmail.trim().toLowerCase());
		this.issueVerificationToken(pending);
	}

	@Transactional(readOnly = true)
	public Optional<PendingRegistration> findById(Long id) {
		return this.pendingRegistrationRepository.findById(id);
	}

	@Transactional
	public PendingRegistrationLookup lookupById(Long id) {
		this.purgeExpired();
		if (id == null) {
			return PendingRegistrationLookup.notFound();
		}
		Optional<PendingRegistration> pendingOptional = this.pendingRegistrationRepository.findById(id);
		if (pendingOptional.isEmpty()) {
			return PendingRegistrationLookup.notFound();
		}
		PendingRegistration pending = pendingOptional.get();
		if (this.isExpired(pending.getVerificationExpiresAt())) {
			this.pendingRegistrationRepository.delete(pending);
			return PendingRegistrationLookup.expired();
		}
		return PendingRegistrationLookup.found(pending);
	}

	@Transactional
	public void issueVerificationToken(PendingRegistration pending) {
		pending.setVerificationToken(this.newToken());
		pending.setVerificationExpiresAt(Instant.now().plus(VERIFICATION_TTL_HOURS, ChronoUnit.HOURS));
	}

	@Transactional
	public void purgeExpired() {
		this.pendingRegistrationRepository.deleteExpired(Instant.now());
	}

	@Scheduled(fixedRate = 3_600_000)
	@Transactional
	public void purgeExpiredScheduled() {
		this.purgeExpired();
	}

	public boolean isUsernameTaken(String username) {
		return this.userRepository.existsByUsernameIgnoreCase(username)
				|| this.pendingRegistrationRepository.existsByUsernameIgnoreCase(username);
	}

	public boolean isEmailTaken(String email) {
		String normalized = email.trim().toLowerCase();
		return this.userRepository.existsByEmailIgnoreCase(normalized)
				|| this.pendingRegistrationRepository.existsByEmailIgnoreCase(normalized);
	}

	public boolean isEmailTakenByAnother(String email, PendingRegistration current) {
		String normalized = email.trim().toLowerCase();
		if (normalized.equalsIgnoreCase(current.getEmail())) {
			return false;
		}
		return this.userRepository.existsByEmailIgnoreCase(normalized)
				|| this.pendingRegistrationRepository.findByEmailIgnoreCase(normalized)
				.filter(pending -> !pending.getId().equals(current.getId()))
				.isPresent();
	}

	private boolean isExpired(Instant expiresAt) {
		return expiresAt == null || expiresAt.isBefore(Instant.now());
	}

	private String newToken() {
		byte[] bytes = new byte[24];
		RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}
}
