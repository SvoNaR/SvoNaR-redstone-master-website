package ru.redstonemaster.web.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import ru.redstonemaster.web.user.PendingRegistrationService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterFormValidatorTest {

	@Mock
	private PendingRegistrationService pendingRegistrationService;

	private RegisterFormValidator validator;

	@BeforeEach
	void setUp() {
		this.validator = new RegisterFormValidator(this.pendingRegistrationService);
	}

	@Test
	void rejectsReservedUsername() {
		RegisterForm form = validForm();
		form.setUsername("admin");
		Errors errors = new BeanPropertyBindingResult(form, "registerForm");

		this.validator.validate(form, errors);

		assertTrue(errors.hasFieldErrors("username"));
	}

	@Test
	void rejectsTakenUsername() {
		when(this.pendingRegistrationService.isUsernameTaken("player1")).thenReturn(true);
		RegisterForm form = validForm();
		form.setUsername("player1");
		Errors errors = new BeanPropertyBindingResult(form, "registerForm");

		this.validator.validate(form, errors);

		assertTrue(errors.hasFieldErrors("username"));
	}

	@Test
	void rejectsTakenEmail() {
		when(this.pendingRegistrationService.isEmailTaken("taken@example.com")).thenReturn(true);
		RegisterForm form = validForm();
		form.setEmail("taken@example.com");
		Errors errors = new BeanPropertyBindingResult(form, "registerForm");

		this.validator.validate(form, errors);

		assertTrue(errors.hasFieldErrors("email"));
	}

	@Test
	void rejectsMismatchedPasswords() {
		RegisterForm form = validForm();
		form.setConfirmPassword("Password2");
		Errors errors = new BeanPropertyBindingResult(form, "registerForm");

		this.validator.validate(form, errors);

		assertTrue(errors.hasFieldErrors("confirmPassword"));
	}

	@Test
	void acceptsValidForm() {
		when(this.pendingRegistrationService.isUsernameTaken(anyString())).thenReturn(false);
		when(this.pendingRegistrationService.isEmailTaken(anyString())).thenReturn(false);
		RegisterForm form = validForm();
		Errors errors = new BeanPropertyBindingResult(form, "registerForm");

		this.validator.validate(form, errors);

		assertFalse(errors.hasErrors());
	}

	private RegisterForm validForm() {
		RegisterForm form = new RegisterForm();
		form.setUsername("player1");
		form.setEmail("player1@example.com");
		form.setPassword("Password1");
		form.setConfirmPassword("Password1");
		return form;
	}
}
