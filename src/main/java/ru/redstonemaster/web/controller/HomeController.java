package ru.redstonemaster.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.service.AboutContentService;
import ru.redstonemaster.web.service.InstallInfoService;
import ru.redstonemaster.web.service.ModLangService;

@Controller
public class HomeController {

	private final AboutContentService aboutContentService;
	private final InstallInfoService installInfoService;
	private final ModLangService modLangService;

	public HomeController(
			AboutContentService aboutContentService,
			InstallInfoService installInfoService,
			ModLangService modLangService
	) {
		this.aboutContentService = aboutContentService;
		this.installInfoService = installInfoService;
		this.modLangService = modLangService;
	}

	@GetMapping("/")
	public String home(
			@RequestParam(name = "lang", defaultValue = "ru") String lang,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(lang);
		model.addAttribute("aboutSections", this.aboutContentService.getSections(locale));
		model.addAttribute("install", this.installInfoService.getInstallInfo(locale));
		model.addAttribute("greeting", this.modLangService.get(locale, "gui.redstone-master.main_menu.greeting"));
		model.addAttribute("titleSuffix", this.modLangService.get(locale, "gui.redstone-master.main_menu.title_suffix"));
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Home" : "Главная");
		return "index";
	}
}
