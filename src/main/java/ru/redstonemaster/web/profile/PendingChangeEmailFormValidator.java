package ru.redstonemaster.web.profile;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.redstonemaster.web.user.PendingRegistration;
import ru.redstonemaster.web.user.PendingRegistrationService;

@Component
public class PendingChangeEmailFormValidator {

	private final PendingRegistrationService pendingRegistrationService;

	public PendingChangeEmailFormValidator(PendingRegistrationService pendingRegistrationService) {
		this.pendingRegistrationService = pendingRegistrationService;
	}

	public void validate(ChangeEmailForm form, Errors errors, PendingRegistration pending) {
		String email = form.getEmail() == null ? null : form.getEmail().toLowerCase();

		if (email != null && email.equalsIgnoreCase(pending.getEmail())) {
			errors.rejectValue("email", "profile.validation.email.unchanged");
		}
		if (email != null && this.pendingRegistrationService.isEmailTakenByAnother(email, pending)) {
			errors.rejectValue("email", "profile.validation.email.taken");
		}
	}
}
