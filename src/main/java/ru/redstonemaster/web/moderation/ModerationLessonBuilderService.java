package ru.redstonemaster.web.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import ru.redstonemaster.web.user.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ModerationLessonBuilderService {

	private final ModerationProperties properties;
	private final ObjectMapper objectMapper;

	public ModerationLessonBuilderService(ModerationProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	public Path createWorkspace(User moderator, String sectionId, String lessonId) throws IOException {
		Path workspace = Path.of(this.properties.getWorkspaceDir())
				.resolve("user-" + moderator.getId())
				.resolve(sectionId + "_" + lessonId + "_" + System.currentTimeMillis());
		Files.createDirectories(workspace);
		Files.createDirectories(workspace.resolve("assets/redstone-master/tutorials/videos"));
		Files.createDirectories(workspace.resolve("assets/redstone-master/textures/tutorial"));
		Files.createDirectories(workspace.resolve("assets/redstone-master/tutorial/packs"));
		return workspace;
	}

	public LessonSubmission createPseudoVideoDraft(User moderator, String videoId) throws IOException {
		String placeholderRu = "Псевдо-видео «" + videoId + "» (черновик модератора).";
		String placeholderEn = "Pseudo-video \"" + videoId + "\" (moderator draft).";
		return this.createDraft(
				moderator,
				"_pseudo_video",
				videoId,
				videoId,
				placeholderRu,
				placeholderEn,
				placeholderRu,
				placeholderEn,
				List.of()
		);
	}

	public LessonSubmission createDraft(
			User moderator,
			String sectionId,
			String lessonId,
			String videoId,
			String titleRu,
			String titleEn,
			String bodyRu,
			String bodyEn,
			List<String> imageResourcePaths
	) throws IOException {
		Path workspace = this.createWorkspace(moderator, sectionId, lessonId);
		this.writePack(workspace, sectionId, lessonId, videoId, titleRu, titleEn, bodyRu, bodyEn, imageResourcePaths);
		return new LessonSubmission(
				moderator,
				sectionId,
				lessonId,
				videoId,
				titleRu,
				titleEn,
				bodyRu,
				bodyEn,
				workspace.toString()
		);
	}

	public Path videoDirectory(LessonSubmission submission) {
		return Path.of(submission.getWorkspacePath())
				.resolve("assets/redstone-master/tutorials/videos")
				.resolve(submission.getVideoId());
	}

	public void writePack(
			Path workspace,
			String sectionId,
			String lessonId,
			String videoId,
			String titleRu,
			String titleEn,
			String bodyRu,
			String bodyEn,
			List<String> imageResourcePaths
	) throws IOException {
		this.writePackFile(workspace, "ru_ru", sectionId, lessonId, videoId, titleRu, bodyRu, imageResourcePaths);
		this.writePackFile(workspace, "en_us", sectionId, lessonId, videoId, titleEn, bodyEn, imageResourcePaths);
	}

	private void writePackFile(
			Path workspace,
			String localeCode,
			String sectionId,
			String lessonId,
			String videoId,
			String title,
			String body,
			List<String> imageResourcePaths
	) throws IOException {
		ObjectNode lesson = this.objectMapper.createObjectNode();
		lesson.put("id", lessonId);
		lesson.put("title", title);
		lesson.put("body", body);
		lesson.put("search", title.toLowerCase());

		ArrayNode images = lesson.putArray("images");
		for (String image : imageResourcePaths) {
			images.add(image);
		}
		ArrayNode videos = lesson.putArray("videos");
		videos.add(videoId);

		ArrayNode lessons = this.objectMapper.createArrayNode();
		lessons.add(lesson);

		ObjectNode section = this.objectMapper.createObjectNode();
		section.put("id", sectionId);
		section.put("title", sectionId);
		section.set("lessons", lessons);

		ArrayNode sections = this.objectMapper.createArrayNode();
		sections.add(section);

		ObjectNode root = this.objectMapper.createObjectNode();
		root.set("sections", sections);

		Path packFile = workspace.resolve("assets/redstone-master/tutorial/packs")
				.resolve(lessonId + "_" + localeCode + ".json");
		Files.createDirectories(packFile.getParent());
		this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(packFile.toFile(), root);
	}
}
