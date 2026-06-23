package ru.redstonemaster.web.profile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterFormConstraintsTest {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void rejectsUsernameStartingWithUnderscore() {
		RegisterForm form = validForm();
		form.setUsername("_player");

		Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

		assertTrue(violations.stream().anyMatch(v -> "username".equals(v.getPropertyPath().toString())));
	}

	@Test
	void rejectsWeakPasswordWithoutDigit() {
		RegisterForm form = validForm();
		form.setPassword("Password");
		form.setConfirmPassword("Password");

		Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

		assertTrue(violations.stream().anyMatch(v -> "password".equals(v.getPropertyPath().toString())));
	}

	@Test
	void rejectsPasswordWithSpaces() {
		RegisterForm form = validForm();
		form.setPassword("Pass word1");
		form.setConfirmPassword("Pass word1");

		Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

		assertTrue(violations.stream().anyMatch(v -> "password".equals(v.getPropertyPath().toString())));
	}

	@Test
	void rejectsInvalidEmail() {
		RegisterForm form = validForm();
		form.setEmail("not-an-email");

		Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

		assertTrue(violations.stream().anyMatch(v -> "email".equals(v.getPropertyPath().toString())));
	}

	@Test
	void acceptsValidForm() {
		RegisterForm form = validForm();

		Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

		assertTrue(violations.isEmpty());
	}

	@Test
	void trimsUsernameOnSet() {
		RegisterForm form = validForm();
		form.setUsername("  player1  ");

		assertFalse(form.getUsername().contains(" "));
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

