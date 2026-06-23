package ru.redstonemaster.web.profile;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.redstonemaster.web.user.PendingRegistrationService;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

@Component
public class ChangeEmailFormValidator implements Validator {

	private final UserRepository userRepository;
	private final PendingRegistrationService pendingRegistrationService;

	public ChangeEmailFormValidator(
			UserRepository userRepository,
			PendingRegistrationService pendingRegistrationService
	) {
		this.userRepository = userRepository;
		this.pendingRegistrationService = pendingRegistrationService;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return ChangeEmailForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		throw new UnsupportedOperationException("Use validate(form, errors, currentUser)");
	}

	public void validate(ChangeEmailForm form, Errors errors, User currentUser) {
		String email = form.getEmail() == null ? null : form.getEmail().toLowerCase();

		if (currentUser != null && email != null && email.equalsIgnoreCase(currentUser.getEmail())) {
			errors.rejectValue("email", "profile.validation.email.unchanged");
		}
		if (email != null && this.pendingRegistrationService.isEmailTaken(email)) {
			errors.rejectValue("email", "profile.validation.email.taken");
		}
		if (email != null && this.userRepository.findByEmailIgnoreCase(email)
				.filter(user -> !user.getId().equals(currentUser.getId()))
				.isPresent()) {
			errors.rejectValue("email", "profile.validation.email.taken");
		}
		if (email != null && this.userRepository.existsByPendingEmailIgnoreCaseAndIdNot(email, currentUser.getId())) {
			errors.rejectValue("email", "profile.validation.email.taken");
		}
	}
}
