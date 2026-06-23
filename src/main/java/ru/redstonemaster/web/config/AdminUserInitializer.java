package ru.redstonemaster.web.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.redstonemaster.web.user.UserService;

@Component
@Order(2)
public class AdminUserInitializer implements ApplicationRunner {

	private final UserService userService;

	public AdminUserInitializer(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void run(ApplicationArguments args) {
		this.userService.ensureAdminExists();
	}
}
