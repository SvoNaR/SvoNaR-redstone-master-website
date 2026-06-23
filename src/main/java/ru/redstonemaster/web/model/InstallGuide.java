package ru.redstonemaster.web.model;

import java.util.List;

public record InstallGuide(
		String title,
		String description,
		List<RequirementRow> requirements,
		List<String> steps,
		List<String> notes
) {
}
