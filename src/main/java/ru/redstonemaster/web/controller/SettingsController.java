package ru.redstonemaster.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.service.KeyBindingService;
import ru.redstonemaster.web.service.ModLangService;
import ru.redstonemaster.web.service.SettingsReferenceService;

@Controller
@RequestMapping("/settings")
public class SettingsController {

	private final SettingsReferenceService settingsReferenceService;
	private final KeyBindingService keyBindingService;
	private final ModLangService modLangService;

	public SettingsController(
			SettingsReferenceService settingsReferenceService,
			KeyBindingService keyBindingService,
			ModLangService modLangService
	) {
		this.settingsReferenceService = settingsReferenceService;
		this.keyBindingService = keyBindingService;
		this.modLangService = modLangService;
	}

	@GetMapping
	public String settings(
			@RequestParam(name = "lang", defaultValue = "ru") String lang,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(lang);
		model.addAttribute("settings", this.settingsReferenceService.getSettings(locale));
		model.addAttribute("referenceNotes", this.settingsReferenceService.getReferenceNotes(locale));
		model.addAttribute("keyBindings", this.keyBindingService.getBindings(locale));
		model.addAttribute("disclaimer", this.modLangService.get(locale, "gui.redstone-master.settings.disclaimer"));
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Settings" : "Настройки");
		return "settings";
	}
}
