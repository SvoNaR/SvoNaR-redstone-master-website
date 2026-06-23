package ru.redstonemaster.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.ModInfo;
import ru.redstonemaster.web.service.ModInfoService;

@ControllerAdvice
public class WebModelAdvice {

	private final ModInfoService modInfoService;

	public WebModelAdvice(ModInfoService modInfoService) {
		this.modInfoService = modInfoService;
	}

	@ModelAttribute("mod")
	public ModInfo modInfo() {
		return this.modInfoService.getInfo();
	}

	@ModelAttribute("locale")
	public WebLocale locale(@RequestParam(name = "lang", defaultValue = "ru") String lang) {
		return WebLocale.fromCode(lang);
	}

	@ModelAttribute("currentPath")
	public String currentPath(HttpServletRequest request) {
		return request.getRequestURI();
	}
}
