package ru.redstonemaster.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.redstonemaster.web.locale.WebLocale;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ModLangService {
	private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;
	private final Map<WebLocale, Map<String, String>> cache = new ConcurrentHashMap<>();

	public ModLangService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String get(WebLocale locale, String key) {
		return this.getMap(locale).getOrDefault(key, key);
	}

	public String get(WebLocale locale, String key, String fallback) {
		return this.getMap(locale).getOrDefault(key, fallback);
	}

	private Map<String, String> getMap(WebLocale locale) {
		return this.cache.computeIfAbsent(locale, this::load);
	}

	private Map<String, String> load(WebLocale locale) {
		String path = "mod-data/lang/" + locale.getResourceCode() + ".json";
		try (InputStream input = new ClassPathResource(path).getInputStream()) {
			Map<String, String> loaded = this.objectMapper.readValue(input, MAP_TYPE);
			return loaded != null ? loaded : Map.of();
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to load lang file: " + path, exception);
		}
	}
}
