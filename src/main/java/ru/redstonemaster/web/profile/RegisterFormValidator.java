package ru.redstonemaster.web.profile;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.redstonemaster.web.user.PendingRegistrationService;

import java.util.Locale;
import java.util.Set;

@Component
public class RegisterFormValidator implements Validator {

	private static final Set<String> RESERVED_USERNAMES = Set.of(
			"admin",
			"moderator",
			"mod",
			"system",
			"root",
			"support",
			"anonymous",
			"null"
	);

	private final PendingRegistrationService pendingRegistrationService;

	public RegisterFormValidator(PendingRegistrationService pendingRegistrationService) {
		this.pendingRegistrationService = pendingRegistrationService;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return RegisterForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		RegisterForm form = (RegisterForm) target;

		String username = form.getUsername();
		String email = form.getEmail() == null ? null : form.getEmail().toLowerCase(Locale.ROOT);

		if (username != null && RESERVED_USERNAMES.contains(username.toLowerCase(Locale.ROOT))) {
			errors.rejectValue("username", "profile.validation.username.reserved");
		}
		if (username != null && this.pendingRegistrationService.isUsernameTaken(username)) {
			errors.rejectValue("username", "profile.validation.username.taken");
		}
		if (email != null && this.pendingRegistrationService.isEmailTaken(email)) {
			errors.rejectValue("email", "profile.validation.email.taken");
		}
		if (!form.isPasswordMatching()) {
			errors.rejectValue("confirmPassword", "profile.validation.confirmPassword.mismatch");
		}
	}
}
