package ru.redstonemaster.web.tutorial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class TutorialProgressServiceTest {

	@Autowired
	private TutorialProgressService tutorialProgressService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void mergeCompletedLessonsAddsValidKeysOnly() {
		User user = this.userRepository.save(new User("progress_user", "progress@test.com", "hash"));
		var stats = this.tutorialProgressService.mergeCompletedLessons(
				user.getId(),
				List.of("redstone_signal:carry_signal", "invalid-key", "redstone_signal:carry_signal")
		);
		assertEquals(1, stats.completedLessons());
		assertEquals(1, stats.completedLessonKeys().size());
		assertEquals("redstone_signal:carry_signal", stats.completedLessonKeys().getFirst());
	}
}
