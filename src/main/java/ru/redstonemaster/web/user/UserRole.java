package ru.redstonemaster.web.user;

public enum UserRole {
	USER,
	MODERATOR,
	ADMIN;

	public String getAuthority() {
		return "ROLE_" + this.name();
	}
}
