package ru.redstonemaster.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.redstonemaster.web.model.ModInfo;

@Service
public class ModInfoService {

	private final ModInfo info;

	public ModInfoService(@Value("${app.base-url}") String publicBaseUrl) {
		this.info = new ModInfo(
				"Redstone Master",
				"1.0.0",
				"1.21.11",
				"Клиентский Fabric-мод с обучающими материалами по редстоун-механикам Minecraft.",
				"https://github.com/SvoNaR/redstone-master",
				normalizeBaseUrl(publicBaseUrl)
		);
	}

	public ModInfo getInfo() {
		return this.info;
	}

	private static String normalizeBaseUrl(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "https://redstone-master.ru";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}
}
