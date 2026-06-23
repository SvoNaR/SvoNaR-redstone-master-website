package ru.redstonemaster.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.news.NewsPostRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NewsPublishEncodingTest {

	private static final String TITLE_RU = "Создание «Воздушной Тропы»";
	private static final String BODY_RU = "У нас вышло полное описание :)";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private NewsPostRepository newsPostRepository;

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void publishNewsPreservesRussianText() throws Exception {
		this.mockMvc.perform(post("/news/publish")
						.param("lang", "ru")
						.param("titleRu", TITLE_RU)
						.param("titleEn", "Air Path update")
						.param("bodyRu", BODY_RU)
						.param("bodyEn", "Full description is live.")
						.with(csrf())
						.characterEncoding("UTF-8"))
				.andExpect(status().is3xxRedirection());

		var post = this.newsPostRepository.findAll().getFirst();
		assertEquals(TITLE_RU, post.getTitleRu());
		assertEquals(BODY_RU, post.getBodyRu());

		this.mockMvc.perform(get("/news/" + post.getId()).param("lang", "ru"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(TITLE_RU)))
				.andExpect(content().string(containsString(BODY_RU)));
	}
}
