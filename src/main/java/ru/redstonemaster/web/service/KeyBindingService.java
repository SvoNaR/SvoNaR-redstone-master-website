package ru.redstonemaster.web.service;

import org.springframework.stereotype.Service;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.KeyBindingInfo;

import java.util.List;

@Service
public class KeyBindingService {
	private final ModLangService modLangService;

	public KeyBindingService(ModLangService modLangService) {
		this.modLangService = modLangService;
	}

	public List<KeyBindingInfo> getBindings(WebLocale locale) {
		boolean english = locale == WebLocale.EN;
		return List.of(
				new KeyBindingInfo(
						this.modLangService.get(locale, "key.redstone-master.open_gui", "Open Redstone Master"),
						"]"
				),
				new KeyBindingInfo(
						english ? "Close window" : "Закрыть окно",
						english ? "X button, Esc" : "Кнопка X, Esc"
				),
				new KeyBindingInfo(
						this.modLangService.get(locale, "gui.redstone-master.settings.close_on_repeat",
								"Close on repeat key"),
						english ? "] (when enabled in settings)" : "] (если включено в настройках)"
				),
				new KeyBindingInfo(
						this.modLangService.get(locale, "key.redstone-master.open_gui.navigation_back",
								"Navigate back"),
						english ? "Mouse button 4" : "Кнопка мыши 4"
				),
				new KeyBindingInfo(
						this.modLangService.get(locale, "key.redstone-master.open_gui.navigation_forward",
								"Navigate forward"),
						english ? "Mouse button 5" : "Кнопка мыши 5"
				),
				new KeyBindingInfo(
						english ? "Open while typing text" : "Открытие при вводе текста",
						english
								? "Blocked: chat, signs, books, Creative search, recipe book, mod search fields"
								: "Заблокировано: чат, таблички, книги, поиск в креативе, книга рецептов, поиск в моде"
				),
				new KeyBindingInfo(
						english ? "Rebind keys" : "Переназначение",
						english ? "Minecraft Settings -> Controls -> Redstone Master"
								: "Настройки Minecraft -> Управление -> Redstone Master"
				)
		);
	}
}
