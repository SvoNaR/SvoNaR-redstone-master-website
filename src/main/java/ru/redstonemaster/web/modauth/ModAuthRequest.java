package ru.redstonemaster.web.modauth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "mod_auth_requests")
public class ModAuthRequest {

	@Id
	@Column(length = 64)
	private String state;

	@Column(nullable = false)
	private int callbackPort;

	@Column(nullable = false, length = 16)
	private String mode;

	@Column(length = 64)
	private String exchangeCode;

	private Long userId;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean consumed = false;

	protected ModAuthRequest() {
	}

	public ModAuthRequest(String state, int callbackPort, String mode, Instant expiresAt) {
		this.state = state;
		this.callbackPort = callbackPort;
		this.mode = mode;
		this.expiresAt = expiresAt;
	}

	public String getState() {
		return this.state;
	}

	public int getCallbackPort() {
		return this.callbackPort;
	}

	public String getMode() {
		return this.mode;
	}

	public String getExchangeCode() {
		return this.exchangeCode;
	}

	public Long getUserId() {
		return this.userId;
	}

	public Instant getExpiresAt() {
		return this.expiresAt;
	}

	public boolean isConsumed() {
		return this.consumed;
	}

	public void complete(Long userId, String exchangeCode) {
		this.userId = userId;
		this.exchangeCode = exchangeCode;
	}

	public void markConsumed() {
		this.consumed = true;
	}

	public boolean isExpired() {
		return Instant.now().isAfter(this.expiresAt);
	}
}
