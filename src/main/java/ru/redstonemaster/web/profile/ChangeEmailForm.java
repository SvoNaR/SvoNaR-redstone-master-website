package ru.redstonemaster.web.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeEmailForm {

	@NotBlank(message = "{profile.validation.email.required}")
	@Email(regexp = ".+@.+\\..+", message = "{profile.validation.email.invalid}")
	@Size(max = 255, message = "{profile.validation.email.size}")
	private String email;

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email == null ? null : email.trim();
	}
}
