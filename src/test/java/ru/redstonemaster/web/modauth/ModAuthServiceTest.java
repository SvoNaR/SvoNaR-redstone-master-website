package ru.redstonemaster.web.modauth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ModAuthServiceTest {

	@Autowired
	private ModAuthService modAuthService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AvatarService avatarService;

	@Test
	void exchangeReturnsProfileAfterCompletion() {
		User user = this.userRepository.save(new User("mod_player", "mod@test.com", "hash"));
		user.setEmailVerified(true);
		this.avatarService.assignRandomDefaultAvatar(user);
		this.userRepository.save(user);

		String state = "abc123def4567890";
		this.modAuthService.begin(state, 49152, "login");
		this.modAuthService.complete(state, user);

		ModAuthProfileResponse response = this.modAuthService.exchange(state, this.requireExchangeCode(state));
		assertEquals("mod_player", response.username());
		assertTrue(response.avatarUrl().startsWith("/avatars/"));
		assertTrue(response.syncToken() != null && !response.syncToken().isBlank());
		assertEquals("mod@test.com", response.email());
		assertEquals("USER", response.role());
		assertEquals(0, response.completedLessons());
		assertTrue(response.totalLessons() > 0);
	}

	@Test
	void exchangeRejectsInvalidCode() {
		User user = this.userRepository.save(new User("mod_player2", "mod2@test.com", "hash"));
		user.setEmailVerified(true);
		this.userRepository.save(user);

		String state = "9876543210abcdef";
		this.modAuthService.begin(state, 50000, "login");
		this.modAuthService.complete(state, user);

		assertThrows(IllegalArgumentException.class, () -> this.modAuthService.exchange(state, "bad-code"));
	}

	private String requireExchangeCode(String state) {
		return this.modAuthService.requireCompleted(state).getExchangeCode();
	}
}
