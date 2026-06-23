package ru.redstonemaster.web.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;
import ru.redstonemaster.web.user.UserRole;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class LessonCommentMuteTest {

	@Autowired
	private LessonCommentService commentService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void mutedUserCannotPostComments() {
		User author = this.userRepository.save(new User("muted_user", "muted@test.com", "hash"));
		User moderator = this.userRepository.save(new User("mod_user", "mod@test.com", "hash"));
		moderator.setRole(UserRole.MODERATOR);
		this.userRepository.save(moderator);

		this.commentService.muteUser(moderator, author.getId(), 60, "spam");

		assertTrue(this.commentService.isMuted(author));
		assertThrows(IllegalStateException.class, () -> this.commentService.postComment(
				author,
				"redstone_signal",
				"carry_signal",
				"test",
				null
		));
	}

	@Test
	void moderatorCannotMuteAnotherModerator() {
		User target = this.userRepository.save(new User("mod_target", "mod-target@test.com", "hash"));
		target.setRole(UserRole.MODERATOR);
		this.userRepository.save(target);
		User moderator = this.userRepository.save(new User("mod_actor", "mod-actor@test.com", "hash"));
		moderator.setRole(UserRole.MODERATOR);
		this.userRepository.save(moderator);

		assertThrows(IllegalArgumentException.class, () -> this.commentService.muteUser(
				moderator,
				target.getId(),
				10,
				"test"
		));
		assertFalse(this.commentService.isMuted(target));
	}

	@Test
	void adminCanMuteModerator() {
		User target = this.userRepository.save(new User("mod_for_admin", "mod-admin@test.com", "hash"));
		target.setRole(UserRole.MODERATOR);
		this.userRepository.save(target);
		User admin = this.userRepository.save(new User("admin_actor", "admin-actor@test.com", "hash"));
		admin.setRole(UserRole.ADMIN);
		this.userRepository.save(admin);

		this.commentService.muteUser(admin, target.getId(), 15, "policy");
		assertTrue(this.commentService.isMuted(target));
	}

	@Test
	void moderatorCanUnmuteRegularUser() {
		User author = this.userRepository.save(new User("unmute_user", "unmute@test.com", "hash"));
		User moderator = this.userRepository.save(new User("unmute_mod", "unmute-mod@test.com", "hash"));
		moderator.setRole(UserRole.MODERATOR);
		this.userRepository.save(moderator);

		this.commentService.muteUser(moderator, author.getId(), 60, "spam");
		assertTrue(this.commentService.isMuted(author));

		this.commentService.unmuteUser(moderator, author.getId());
		assertFalse(this.commentService.isMuted(author));
	}

	@Test
	void moderatorCannotUnmuteAnotherModerator() {
		User target = this.userRepository.save(new User("mod_unmute_target", "mod-unmute-target@test.com", "hash"));
		target.setRole(UserRole.MODERATOR);
		this.userRepository.save(target);
		User moderator = this.userRepository.save(new User("mod_unmute_actor", "mod-unmute-actor@test.com", "hash"));
		moderator.setRole(UserRole.MODERATOR);
		this.userRepository.save(moderator);
		User admin = this.userRepository.save(new User("mod_unmute_admin", "mod-unmute-admin@test.com", "hash"));
		admin.setRole(UserRole.ADMIN);
		this.userRepository.save(admin);

		this.commentService.muteUser(admin, target.getId(), 30, "policy");
		assertTrue(this.commentService.isMuted(target));

		assertThrows(IllegalArgumentException.class, () -> this.commentService.unmuteUser(moderator, target.getId()));
		assertTrue(this.commentService.isMuted(target));
	}

	@Test
	void adminCanUnmuteModerator() {
		User target = this.userRepository.save(new User("admin_unmute_target", "admin-unmute-target@test.com", "hash"));
		target.setRole(UserRole.MODERATOR);
		this.userRepository.save(target);
		User admin = this.userRepository.save(new User("admin_unmute_actor", "admin-unmute-actor@test.com", "hash"));
		admin.setRole(UserRole.ADMIN);
		this.userRepository.save(admin);

		this.commentService.muteUser(admin, target.getId(), 45, "policy");
		assertTrue(this.commentService.isMuted(target));

		this.commentService.unmuteUser(admin, target.getId());
		assertFalse(this.commentService.isMuted(target));
	}
}
