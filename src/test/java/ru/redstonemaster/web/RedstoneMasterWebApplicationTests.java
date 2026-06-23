package ru.redstonemaster.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class RedstoneMasterWebApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void homePageLoads() throws Exception {
		this.mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("index"));
	}

	@Test
	void tutorialPageLoads() throws Exception {
		this.mockMvc.perform(get("/tutorial"))
				.andExpect(status().isOk())
				.andExpect(view().name("tutorial/index"));
	}

	@Test
	void settingsPageLoads() throws Exception {
		this.mockMvc.perform(get("/settings"))
				.andExpect(status().isOk())
				.andExpect(view().name("settings"));
	}

	@Test
	void infoApiReturnsModData() throws Exception {
		this.mockMvc.perform(get("/api/info"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Redstone Master"))
				.andExpect(jsonPath("$.version").value("1.0.0"))
				.andExpect(jsonPath("$.publicBaseUrl").value("http://localhost:8080"));
	}

	@Test
	void profilePageLoads() throws Exception {
		this.mockMvc.perform(get("/profile"))
				.andExpect(status().isOk())
				.andExpect(view().name("profile/guest"));
	}

	@Test
	void tutorialApiReturnsSections() throws Exception {
		this.mockMvc.perform(get("/api/tutorial/sections"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").exists());
	}
}
