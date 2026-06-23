package ru.redstonemaster.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.news.NewsPost;
import ru.redstonemaster.web.news.NewsService;
import ru.redstonemaster.web.news.PublishNewsForm;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserService;

@Controller
public class NewsController {

	private final NewsService newsService;
	private final UserService userService;

	public NewsController(NewsService newsService, UserService userService) {
		this.newsService = newsService;
		this.userService = userService;
	}

	@GetMapping("/news")
	public String newsList(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "News" : "Новости");
		model.addAttribute("newsPosts", this.newsService.getAll(langCode));
		return "news/index";
	}

	@GetMapping("/news/{id}")
	public String newsDetail(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Model model
	) {
		return this.newsService.findById(id, langCode)
				.map(post -> {
					WebLocale locale = WebLocale.fromCode(langCode);
					model.addAttribute("pageTitle", post.title());
					model.addAttribute("newsPost", post);
					return "news/detail";
				})
				.orElse("redirect:/news?lang=" + langCode);
	}

	@GetMapping("/news/publish")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	public String publishForm(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		if (!model.containsAttribute("publishNewsForm")) {
			model.addAttribute("publishNewsForm", new PublishNewsForm());
		}
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Publish news" : "Публикация новости");
		return "news/publish";
	}

	@PostMapping("/news/publish")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	public String publish(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@Valid @ModelAttribute("publishNewsForm") PublishNewsForm form,
			BindingResult bindingResult,
			Authentication authentication,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", locale == WebLocale.EN ? "Publish news" : "Публикация новости");
			return "news/publish";
		}
		User author = this.userService.findByUsername(authentication.getName()).orElseThrow();
		NewsPost post = this.newsService.publish(form, author);
		redirectAttributes.addFlashAttribute("publishSuccess", true);
		return "redirect:/news/" + post.getId() + "?lang=" + langCode;
	}

	@GetMapping("/news/{id}/edit")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	public String editForm(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		return this.newsService.findPostById(id)
				.map(post -> {
					if (!model.containsAttribute("publishNewsForm")) {
						PublishNewsForm form = new PublishNewsForm();
						form.setTitleRu(post.getTitleRu());
						form.setTitleEn(post.getTitleEn());
						form.setBodyRu(post.getBodyRu());
						form.setBodyEn(post.getBodyEn());
						model.addAttribute("publishNewsForm", form);
					}
					model.addAttribute("newsId", id);
					model.addAttribute("pageTitle", locale == WebLocale.EN ? "Edit news" : "Редактирование новости");
					return "news/edit";
				})
				.orElse("redirect:/news?lang=" + langCode);
	}

	@PostMapping("/news/{id}/edit")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	public String edit(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@Valid @ModelAttribute("publishNewsForm") PublishNewsForm form,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		if (bindingResult.hasErrors()) {
			model.addAttribute("newsId", id);
			model.addAttribute("pageTitle", locale == WebLocale.EN ? "Edit news" : "Редактирование новости");
			return "news/edit";
		}
		return this.newsService.update(id, form)
				.map(post -> {
					redirectAttributes.addFlashAttribute("editSuccess", true);
					return "redirect:/news/" + post.getId() + "?lang=" + langCode;
				})
				.orElse("redirect:/news?lang=" + langCode);
	}

	@PostMapping("/news/{id}/delete")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	public String delete(
			@PathVariable Long id,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			RedirectAttributes redirectAttributes
	) {
		if (this.newsService.delete(id)) {
			redirectAttributes.addFlashAttribute("deleteSuccess", true);
		}
		return "redirect:/news?lang=" + langCode;
	}
}
