package ru.redstonemaster.web.service;

import org.springframework.stereotype.Service;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.InstallGuide;
import ru.redstonemaster.web.model.InstallInfo;
import ru.redstonemaster.web.model.RequirementRow;

import java.util.List;

@Service
public class InstallInfoService {

	public InstallInfo getInstallInfo(WebLocale locale) {
		if (locale == WebLocale.EN) {
			return new InstallInfo(
					this.playerGuideEn(),
					this.developerGuideEn(),
					"build/libs/redstone-master-1.0.0.jar",
					"config/redstone-master.json"
			);
		}
		return new InstallInfo(
				this.playerGuideRu(),
				this.developerGuideRu(),
				"build/libs/redstone-master-1.0.0.jar",
				"config/redstone-master.json"
		);
	}

	private InstallGuide playerGuideRu() {
		return new InstallGuide(
				"Для игрока",
				"Скачайте готовый JAR мода и установите его в клиент Minecraft через лаунчер с Fabric.",
				List.of(
						new RequirementRow("Minecraft", "1.21.11"),
						new RequirementRow("Fabric Loader", ">= 0.19.2"),
						new RequirementRow("Fabric API", "0.141.4+1.21.11")
				),
				List.of(
						"Скачайте файл redstone-master-1.0.0.jar (релиз на GitHub или собранный JAR).",
						"Установите Fabric Loader для Minecraft 1.21.11 в своём лаунчере (официальный, Prism, MultiMC и т.д.).",
						"Скачайте мод Fabric API для версии 1.21.11 и положите его в папку mods.",
						"Скопируйте redstone-master-1.0.0.jar в папку mods клиента Minecraft.",
						"Пример пути: %appdata%\\.minecraft\\mods (Windows) или ~/.minecraft/mods (Linux/macOS).",
						"Запустите Minecraft с профилем Fabric для версии 1.21.11.",
						"В игре нажмите ], чтобы открыть окно мода (переназначение: Настройки -> Управление -> Redstone Master)."
				),
				List.of(
						"Серверная установка не требуется — мод только для клиента.",
						"Настройки мода сохраняются в config/redstone-master.json после первого запуска."
				)
		);
	}

	private InstallGuide developerGuideRu() {
		return new InstallGuide(
				"Для разработчиков",
				"Клонируйте репозиторий Redstone Master, соберите мод из исходников или запустите клиент из IDE.",
				List.of(
						new RequirementRow("Minecraft", "1.21.11"),
						new RequirementRow("Fabric Loader", ">= 0.19.2"),
						new RequirementRow("Fabric API", "0.141.4+1.21.11"),
						new RequirementRow("JDK", "21")
				),
				List.of(
						"Клонируйте репозиторий: git clone https://github.com/SvoNaR/redstone-master.git",
						"Перейдите в папку проекта: cd redstone-master",
						"Соберите мод: ./gradlew build (Windows: gradlew.bat build)",
						"Готовый JAR: build/libs/redstone-master-1.0.0.jar — скопируйте в mods или используйте для распространения.",
						"Для разработки запустите клиент: ./gradlew runClient (Windows: gradlew.bat runClient)",
						"Веб-приложение (отдельно): cd other_projects/redstone-master-web && mvn spring-boot:run"
				),
				List.of(
						"Исходники мода: src/client/java и src/main/resources.",
						"Конфигурация Gradle и версии зависимостей — в gradle.properties и build.gradle."
				)
		);
	}

	private InstallGuide playerGuideEn() {
		return new InstallGuide(
				"For players",
				"Download the mod JAR and install it in your Minecraft client via a Fabric-enabled launcher.",
				List.of(
						new RequirementRow("Minecraft", "1.21.11"),
						new RequirementRow("Fabric Loader", ">= 0.19.2"),
						new RequirementRow("Fabric API", "0.141.4+1.21.11")
				),
				List.of(
						"Download redstone-master-1.0.0.jar (GitHub release or a built JAR).",
						"Install Fabric Loader for Minecraft 1.21.11 in your launcher (official, Prism, MultiMC, etc.).",
						"Download the Fabric API mod for 1.21.11 and place it in the mods folder.",
						"Copy redstone-master-1.0.0.jar into your Minecraft mods folder.",
						"Example path: %appdata%\\.minecraft\\mods (Windows) or ~/.minecraft/mods (Linux/macOS).",
						"Launch Minecraft with the Fabric profile for 1.21.11.",
						"In game, press ] to open the mod window (rebind in Settings -> Controls -> Redstone Master)."
				),
				List.of(
						"No server install required — client-only mod.",
						"Mod settings are saved to config/redstone-master.json after the first run."
				)
		);
	}

	private InstallGuide developerGuideEn() {
		return new InstallGuide(
				"For developers",
				"Clone the Redstone Master repository, build the mod from source, or run the client from your IDE.",
				List.of(
						new RequirementRow("Minecraft", "1.21.11"),
						new RequirementRow("Fabric Loader", ">= 0.19.2"),
						new RequirementRow("Fabric API", "0.141.4+1.21.11"),
						new RequirementRow("JDK", "21")
				),
				List.of(
						"Clone the repository: git clone https://github.com/SvoNaR/redstone-master.git",
						"Enter the project directory: cd redstone-master",
						"Build the mod: ./gradlew build (Windows: gradlew.bat build)",
						"Output JAR: build/libs/redstone-master-1.0.0.jar — copy to mods or distribute.",
						"For development run the client: ./gradlew runClient (Windows: gradlew.bat runClient)",
						"Web app (separate): cd other_projects/redstone-master-web && mvn spring-boot:run"
				),
				List.of(
						"Mod sources: src/client/java and src/main/resources.",
						"Gradle configuration and dependency versions: gradle.properties and build.gradle."
				)
		);
	}
}
