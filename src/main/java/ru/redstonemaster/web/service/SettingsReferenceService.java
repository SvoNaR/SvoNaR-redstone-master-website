package ru.redstonemaster.web.service;

import org.springframework.stereotype.Service;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.SettingInfo;
import ru.redstonemaster.web.model.SettingReferenceNote;

import java.util.ArrayList;
import java.util.List;

@Service
public class SettingsReferenceService {
	private record SettingDefinition(
			String sectionKey,
			String nameKey,
			String tooltipKey,
			String defaultValueRu,
			String defaultValueEn
	) {
	}

	private static final List<SettingDefinition> DEFINITIONS = List.of(
			new SettingDefinition(
					"gui.redstone-master.settings.section.interface",
					"gui.redstone-master.settings.panel_scale",
					null,
					"80%",
					"80%"
			),
			new SettingDefinition(
					"gui.redstone-master.settings.section.interface",
					"gui.redstone-master.settings.background_opacity",
					"gui.redstone-master.settings.background_opacity.tooltip",
					"75%",
					"75%"
			),
			new SettingDefinition(
					"gui.redstone-master.settings.section.interface",
					"gui.redstone-master.settings.pause_on_open",
					"gui.redstone-master.settings.pause_on_open.tooltip",
					"Вкл",
					"On"
			),
			new SettingDefinition(
					"gui.redstone-master.settings.section.interface",
					"gui.redstone-master.settings.high_contrast",
					null,
					"Выкл",
					"Off"
			),
			new SettingDefinition(
					"gui.redstone-master.settings.section.interface",
					"gui.redstone-master.settings.auto_language",
					"gui.redstone-master.settings.auto_language.tooltip",
					"Вкл",
					"On"
			),
			new SettingDefinition(
					"gui.redstone-master.settings.section.interface",
					"gui.redstone-master.settings.manual_language",
					null,
					"Язык Minecraft",
					"Minecraft language"
			),
			new SettingDefinition(
					"gui.redstone-master.settings.section.controls",
					"gui.redstone-master.settings.remember_session",
					"gui.redstone-master.settings.remember_session.tooltip",
					"Вкл",
					"On"
			),
			new SettingDefinition(
					"gui.redstone-master.settings.section.controls",
					"gui.redstone-master.settings.close_on_repeat",
					"gui.redstone-master.settings.close_on_repeat.tooltip",
					"Вкл",
					"On"
			),
			new SettingDefinition(
					"gui.redstone-master.settings.section.tutorial",
					"gui.redstone-master.settings.tutorial_collapse_other",
					"gui.redstone-master.settings.tutorial_collapse_other.tooltip",
					"Выкл",
					"Off"
			)
	);

	private final ModLangService modLangService;

	public SettingsReferenceService(ModLangService modLangService) {
		this.modLangService = modLangService;
	}

	public List<SettingInfo> getSettings(WebLocale locale) {
		List<SettingInfo> settings = new ArrayList<>();
		for (SettingDefinition definition : DEFINITIONS) {
			String defaultValue = locale == WebLocale.EN
					? definition.defaultValueEn()
					: definition.defaultValueRu();
			String tooltip = definition.tooltipKey() == null
					? ""
					: this.modLangService.get(locale, definition.tooltipKey(), "");
			settings.add(new SettingInfo(
					this.modLangService.get(locale, definition.sectionKey(), definition.sectionKey()),
					this.modLangService.get(locale, definition.nameKey(), definition.nameKey()),
					defaultValue,
					tooltip
			));
		}
		return settings;
	}

	public List<SettingReferenceNote> getReferenceNotes(WebLocale locale) {
		if (locale == WebLocale.EN) {
			return List.of(
					new SettingReferenceNote(
							"Background transparency",
							"Slider from 100% (fully transparent panel) to 0% (opaque black). Default is 75%. "
									+ "The game world or menu background remains visible behind the panel."
					),
					new SettingReferenceNote(
							"Search fields",
							"Tutorial and Settings tabs have a search box at the top. When focused, a blinking cursor "
									+ "appears like in vanilla Minecraft search fields."
					),
					new SettingReferenceNote(
							"Open key while typing",
							"The ] key does not open the mod while you enter text: in-game chat, signs, books, "
									+ "Creative inventory search, survival recipe book search, or mod search fields."
					),
					new SettingReferenceNote(
							"Reset",
							"Each setting has a Reset button. «Reset all settings» at the bottom restores every default."
					)
			);
		}
		return List.of(
				new SettingReferenceNote(
						"Прозрачность фона",
						"Ползунок от 100% (полностью прозрачная панель) до 0% (непрозрачный чёрный фон). "
								+ "Стандарт — 75%. Мир или фон меню остаётся виден за панелью."
				),
				new SettingReferenceNote(
						"Поля поиска",
						"На вкладках «Обучение» и «Настройки» — строка поиска сверху. При фокусе отображается "
								+ "мигающий курсор, как в поиске Minecraft."
				),
				new SettingReferenceNote(
						"Клавиша ] при вводе текста",
						"Окно мода не открывается по ], пока вы вводите текст: чат, таблички, книги, "
								+ "поиск в креативе, книга рецептов или поля поиска мода."
				),
				new SettingReferenceNote(
						"Сброс",
						"У каждой настройки — кнопка «Сброс». «Сбросить все настройки» внизу возвращает все значения по умолчанию."
				)
		);
	}
}
