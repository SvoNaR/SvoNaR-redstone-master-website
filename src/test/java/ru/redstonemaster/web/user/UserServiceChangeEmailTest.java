package ru.redstonemaster.web.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceChangeEmailTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void changeEmailKeepsCurrentEmailUntilPendingIsConfirmed() {
		User user = this.userRepository.save(new User("email_change", "old@example.com", "hash"));
		user.setEmailVerified(true);
		this.userRepository.save(user);

		this.userService.changeEmail(user, "new@example.com");

		User updated = this.userRepository.findById(user.getId()).orElseThrow();
		assertEquals("old@example.com", updated.getEmail());
		assertEquals("new@example.com", updated.getPendingEmail());
		assertTrue(updated.getPendingEmailVerificationToken() != null);
	}

	@Test
	void verifyEmailAppliesPendingEmailChange() {
		User user = this.userRepository.save(new User("email_verify", "old@example.com", "hash"));
		user.setEmailVerified(true);
		this.userRepository.save(user);
		this.userService.changeEmail(user, "new@example.com");
		User pending = this.userRepository.findById(user.getId()).orElseThrow();

		this.userService.verifyEmail(pending.getPendingEmailVerificationToken());

		User updated = this.userRepository.findById(user.getId()).orElseThrow();
		assertEquals("new@example.com", updated.getEmail());
		assertNull(updated.getPendingEmail());
	}
}
