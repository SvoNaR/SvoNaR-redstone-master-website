package ru.redstonemaster.web.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 32)
	private String username;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private UserRole role = UserRole.USER;

	@Column(nullable = false)
	private boolean emailVerified = false;

	@Column(length = 64)
	private String emailVerificationToken;

	private Instant emailVerificationExpiresAt;

	@Column(length = 255)
	private String pendingEmail;

	@Column(length = 64)
	private String pendingEmailVerificationToken;

	private Instant pendingEmailVerificationExpiresAt;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	@Column(length = 128)
	private String avatarPath;

	@Column
	private boolean customAvatar = false;

	@Column
	private boolean profileIntroSeen = false;

	@Column(length = 64, unique = true)
	private String modSyncToken;

	protected User() {
	}

	public User(String username, String email, String passwordHash) {
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
	}

	public Long getId() {
		return this.id;
	}

	public String getUsername() {
		return this.username;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return this.passwordHash;
	}

	public UserRole getRole() {
		return this.role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public boolean isEmailVerified() {
		return this.emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getEmailVerificationToken() {
		return this.emailVerificationToken;
	}

	public void setEmailVerificationToken(String emailVerificationToken) {
		this.emailVerificationToken = emailVerificationToken;
	}

	public Instant getEmailVerificationExpiresAt() {
		return this.emailVerificationExpiresAt;
	}

	public void setEmailVerificationExpiresAt(Instant emailVerificationExpiresAt) {
		this.emailVerificationExpiresAt = emailVerificationExpiresAt;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public String getInitials() {
		if (this.username == null || this.username.isBlank()) {
			return "?";
		}
		return this.username.substring(0, 1).toUpperCase();
	}

	public String getAvatarPath() {
		return this.avatarPath;
	}

	public void setAvatarPath(String avatarPath) {
		this.avatarPath = avatarPath;
	}

	public boolean isCustomAvatar() {
		return this.customAvatar;
	}

	public void setCustomAvatar(boolean customAvatar) {
		this.customAvatar = customAvatar;
	}

	public boolean isProfileIntroSeen() {
		return this.profileIntroSeen;
	}

	public void setProfileIntroSeen(boolean profileIntroSeen) {
		this.profileIntroSeen = profileIntroSeen;
	}

	public String getPendingEmail() {
		return this.pendingEmail;
	}

	public void setPendingEmail(String pendingEmail) {
		this.pendingEmail = pendingEmail;
	}

	public String getPendingEmailVerificationToken() {
		return this.pendingEmailVerificationToken;
	}

	public void setPendingEmailVerificationToken(String pendingEmailVerificationToken) {
		this.pendingEmailVerificationToken = pendingEmailVerificationToken;
	}

	public Instant getPendingEmailVerificationExpiresAt() {
		return this.pendingEmailVerificationExpiresAt;
	}

	public void setPendingEmailVerificationExpiresAt(Instant pendingEmailVerificationExpiresAt) {
		this.pendingEmailVerificationExpiresAt = pendingEmailVerificationExpiresAt;
	}

	public void clearPendingEmailChange() {
		this.pendingEmail = null;
		this.pendingEmailVerificationToken = null;
		this.pendingEmailVerificationExpiresAt = null;
	}

	public String getModSyncToken() {
		return this.modSyncToken;
	}

	public void setModSyncToken(String modSyncToken) {
		this.modSyncToken = modSyncToken;
	}
}
