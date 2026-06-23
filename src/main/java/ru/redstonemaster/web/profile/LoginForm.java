package ru.redstonemaster.web.profile;

import jakarta.validation.constraints.NotBlank;

public class LoginForm {

	@NotBlank(message = "{profile.validation.login.required}")
	private String login;

	@NotBlank(message = "{profile.validation.password.required}")
	private String password;

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
