package ru.redstonemaster.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.KeyBindingInfo;
import ru.redstonemaster.web.model.ModInfo;
import ru.redstonemaster.web.model.SettingInfo;
import ru.redstonemaster.web.model.TutorialSection;
import ru.redstonemaster.web.service.KeyBindingService;
import ru.redstonemaster.web.service.ModInfoService;
import ru.redstonemaster.web.service.SettingsReferenceService;
import ru.redstonemaster.web.service.TutorialContentService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ModInfoRestController {

	private final ModInfoService modInfoService;
	private final TutorialContentService tutorialContentService;
	private final SettingsReferenceService settingsReferenceService;
	private final KeyBindingService keyBindingService;

	public ModInfoRestController(
			ModInfoService modInfoService,
			TutorialContentService tutorialContentService,
			SettingsReferenceService settingsReferenceService,
			KeyBindingService keyBindingService
	) {
		this.modInfoService = modInfoService;
		this.tutorialContentService = tutorialContentService;
		this.settingsReferenceService = settingsReferenceService;
		this.keyBindingService = keyBindingService;
	}

	@GetMapping("/info")
	public ModInfo info() {
		return this.modInfoService.getInfo();
	}

	@GetMapping("/tutorial/sections")
	public List<TutorialSection> tutorialSections(@RequestParam(name = "lang", defaultValue = "ru") String lang) {
		return this.tutorialContentService.getSections(WebLocale.fromCode(lang));
	}

	@GetMapping("/settings")
	public List<SettingInfo> settings(@RequestParam(name = "lang", defaultValue = "ru") String lang) {
		return this.settingsReferenceService.getSettings(WebLocale.fromCode(lang));
	}

	@GetMapping("/keys")
	public List<KeyBindingInfo> keys(@RequestParam(name = "lang", defaultValue = "ru") String lang) {
		return this.keyBindingService.getBindings(WebLocale.fromCode(lang));
	}
}
