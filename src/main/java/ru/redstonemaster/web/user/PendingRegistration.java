package ru.redstonemaster.web.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "pending_registrations")
public class PendingRegistration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 32)
	private String username;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false, length = 64)
	private String verificationToken;

	@Column(nullable = false)
	private Instant verificationExpiresAt;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	protected PendingRegistration() {
	}

	public PendingRegistration(String username, String email, String passwordHash) {
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

	public String getPasswordHash() {
		return this.passwordHash;
	}

	public String getVerificationToken() {
		return this.verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public Instant getVerificationExpiresAt() {
		return this.verificationExpiresAt;
	}

	public void setVerificationExpiresAt(Instant verificationExpiresAt) {
		this.verificationExpiresAt = verificationExpiresAt;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}
}
