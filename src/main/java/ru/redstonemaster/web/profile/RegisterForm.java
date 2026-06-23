package ru.redstonemaster.web.profile;



import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Pattern;

import jakarta.validation.constraints.Size;



public class RegisterForm {



	public static final String USERNAME_PATTERN = "[A-Za-z0-9][A-Za-z0-9_]{2,19}";

	public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[\\S]{8,72}$";



	@NotBlank(message = "{profile.validation.username.required}")

	@Size(min = 3, max = 20, message = "{profile.validation.username.size}")

	@Pattern(regexp = USERNAME_PATTERN, message = "{profile.validation.username.pattern}")

	private String username;



	@NotBlank(message = "{profile.validation.email.required}")

	@Email(regexp = ".+@.+\\..+", message = "{profile.validation.email.invalid}")

	@Size(max = 255, message = "{profile.validation.email.size}")

	private String email;



	@NotBlank(message = "{profile.validation.password.required}")

	@Size(min = 8, max = 72, message = "{profile.validation.password.size}")

	@Pattern(regexp = PASSWORD_PATTERN, message = "{profile.validation.password.pattern}")

	private String password;



	@NotBlank(message = "{profile.validation.confirmPassword.required}")

	private String confirmPassword;



	public String getUsername() {

		return this.username;

	}



	public void setUsername(String username) {

		this.username = username == null ? null : username.trim();

	}



	public String getEmail() {

		return this.email;

	}



	public void setEmail(String email) {

		this.email = email == null ? null : email.trim();

	}



	public String getPassword() {

		return this.password;

	}



	public void setPassword(String password) {

		this.password = password;

	}



	public String getConfirmPassword() {

		return this.confirmPassword;

	}



	public void setConfirmPassword(String confirmPassword) {

		this.confirmPassword = confirmPassword;

	}



	public boolean isPasswordMatching() {

		return this.password != null && this.password.equals(this.confirmPassword);

	}

}


