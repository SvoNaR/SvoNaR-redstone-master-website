package ru.redstonemaster.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileRegistrationValidationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsInvalidRegistrationInput() throws Exception {
		this.mockMvc.perform(post("/profile/register")
						.param("lang", "ru")
						.param("username", "ab")
						.param("email", "bad-email")
						.param("password", "short")
						.param("confirmPassword", "other")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("profile/guest"))
				.andExpect(content().string(containsString("Никнейм: от 3 до 20 символов")))
				.andExpect(content().string(containsString("Некорректный адрес почты")))
				.andExpect(content().string(containsString("Пароли не совпадают")));
	}

	@Test
	void rejectsReservedUsername() throws Exception {
		this.mockMvc.perform(post("/profile/register")
						.param("lang", "ru")
						.param("username", "admin")
						.param("email", "newuser@example.com")
						.param("password", "Password1")
						.param("confirmPassword", "Password1")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("profile/guest"))
				.andExpect(content().string(containsString("зарезервирован")));
	}
}

