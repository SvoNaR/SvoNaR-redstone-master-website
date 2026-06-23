package ru.redstonemaster.web.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceAdminListTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void findUsersByRoleSupportsSearchAndPagination() {
		for (int i = 1; i <= 12; i++) {
			User user = this.userRepository.save(new User("admin_list_" + i, "admin-list-" + i + "@test.com", "hash"));
			user.setEmailVerified(true);
			this.userRepository.save(user);
		}

		Page<User> firstPage = this.userService.findUsersByRole(UserRole.USER, "", 1);
		assertEquals(UserService.ADMIN_PAGE_SIZE, firstPage.getContent().size());
		assertTrue(firstPage.getTotalElements() >= 12);

		Page<User> secondPage = this.userService.findUsersByRole(UserRole.USER, "", 2);
		assertTrue(secondPage.getContent().size() >= 2);

		Page<User> searchPage = this.userService.findUsersByRole(UserRole.USER, "admin_list_12", 1);
		assertEquals(1, searchPage.getTotalElements());
		assertEquals("admin_list_12", searchPage.getContent().getFirst().getUsername());
	}
}
