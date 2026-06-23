package ru.redstonemaster.web.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_mutes")
public class UserMute {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "muted_by_id", nullable = false)
	private Long mutedById;

	@Column(name = "muted_until", nullable = false)
	private Instant mutedUntil;

	@Column(nullable = false, length = 500)
	private String reason;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	protected UserMute() {
	}

	public UserMute(Long userId, Long mutedById, Instant mutedUntil, String reason) {
		this.userId = userId;
		this.mutedById = mutedById;
		this.mutedUntil = mutedUntil;
		this.reason = reason;
	}

	public Long getUserId() {
		return this.userId;
	}

	public Long getMutedById() {
		return this.mutedById;
	}

	public Instant getMutedUntil() {
		return this.mutedUntil;
	}

	public void setMutedUntil(Instant mutedUntil) {
		this.mutedUntil = mutedUntil;
	}

	public String getReason() {
		return this.reason;
	}
}
