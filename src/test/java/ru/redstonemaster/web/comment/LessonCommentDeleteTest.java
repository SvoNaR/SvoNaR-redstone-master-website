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
class LessonCommentDeleteTest {

	private static final String SECTION_ID = "redstone_signal";
	private static final String LESSON_ID = "carry_signal";

	@Autowired
	private LessonCommentService commentService;

	@Autowired
	private LessonCommentRepository commentRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void moderatorCanDeleteRegularUserComment() {
		User author = this.createUser("delete_user", UserRole.USER);
		User moderator = this.createUser("delete_mod", UserRole.MODERATOR);
		this.commentService.postComment(author, SECTION_ID, LESSON_ID, "spam", null);
		long commentId = this.commentRepository.findAll().getLast().getId();

		this.commentService.deleteComment(moderator, commentId);
		assertTrue(this.commentRepository.findById(commentId).orElseThrow().isDeleted());
	}

	@Test
	void moderatorCannotDeleteModeratorComment() {
		User author = this.createUser("delete_mod_author", UserRole.MODERATOR);
		User moderator = this.createUser("delete_mod_actor", UserRole.MODERATOR);
		this.commentService.postComment(author, SECTION_ID, LESSON_ID, "note", null);
		long commentId = this.commentRepository.findAll().getLast().getId();

		assertThrows(IllegalArgumentException.class, () -> this.commentService.deleteComment(moderator, commentId));
		assertFalse(this.commentRepository.findById(commentId).orElseThrow().isDeleted());
	}

	@Test
	void moderatorCannotDeleteAdminComment() {
		User author = this.createUser("delete_admin_author", UserRole.ADMIN);
		User moderator = this.createUser("delete_mod_vs_admin", UserRole.MODERATOR);
		this.commentService.postComment(author, SECTION_ID, LESSON_ID, "note", null);
		long commentId = this.commentRepository.findAll().getLast().getId();

		assertThrows(IllegalArgumentException.class, () -> this.commentService.deleteComment(moderator, commentId));
		assertFalse(this.commentRepository.findById(commentId).orElseThrow().isDeleted());
	}

	@Test
	void adminCanDeleteModeratorComment() {
		User author = this.createUser("delete_mod_for_admin", UserRole.MODERATOR);
		User admin = this.createUser("delete_admin_actor", UserRole.ADMIN);
		this.commentService.postComment(author, SECTION_ID, LESSON_ID, "note", null);
		long commentId = this.commentRepository.findAll().getLast().getId();

		this.commentService.deleteComment(admin, commentId);
		assertTrue(this.commentRepository.findById(commentId).orElseThrow().isDeleted());
	}

	@Test
	void authorCanDeleteOwnComment() {
		User author = this.createUser("delete_own_user", UserRole.USER);
		this.commentService.postComment(author, SECTION_ID, LESSON_ID, "my comment", null);
		long commentId = this.commentRepository.findAll().getLast().getId();

		this.commentService.deleteComment(author, commentId);
		assertTrue(this.commentRepository.findById(commentId).orElseThrow().isDeleted());
	}

	@Test
	void authorCannotDeleteOthersComment() {
		User author = this.createUser("delete_other_author", UserRole.USER);
		User other = this.createUser("delete_other_actor", UserRole.USER);
		this.commentService.postComment(author, SECTION_ID, LESSON_ID, "not yours", null);
		long commentId = this.commentRepository.findAll().getLast().getId();

		assertThrows(IllegalArgumentException.class, () -> this.commentService.deleteComment(other, commentId));
		assertFalse(this.commentRepository.findById(commentId).orElseThrow().isDeleted());
	}

	@Test
	void deleteButtonShownForOwnComment() {
		User author = this.createUser("delete_own_view", UserRole.USER);
		this.commentService.postComment(author, SECTION_ID, LESSON_ID, "visible delete", null);

		var views = this.commentService.listForLesson(SECTION_ID, LESSON_ID, author);
		assertTrue(views.stream()
				.filter(view -> view.username().equals(author.getUsername()))
				.allMatch(view -> view.canModerate() && view.ownComment()));
	}

	@Test
	void deleteButtonHiddenForModeratorWhenAuthorIsStaff() {
		User author = this.createUser("delete_staff_author", UserRole.ADMIN);
		User moderator = this.createUser("delete_staff_mod", UserRole.MODERATOR);
		this.commentService.postComment(author, SECTION_ID, LESSON_ID, "staff comment", null);

		var views = this.commentService.listForLesson(SECTION_ID, LESSON_ID, moderator);
		assertTrue(views.stream().anyMatch(view -> view.username().equals(author.getUsername())));
		assertTrue(views.stream()
				.filter(view -> view.username().equals(author.getUsername()))
				.allMatch(view -> !view.canModerate()));
	}

	private User createUser(String username, UserRole role) {
		User user = this.userRepository.save(new User(username, username + "@test.com", "hash"));
		user.setRole(role);
		return this.userRepository.save(user);
	}
}
