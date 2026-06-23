package ru.redstonemaster.web.moderation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PseudoVideoWorkspaceService {

	private final ModerationLessonBuilderService lessonBuilder;
	private final ModerationProperties properties;
	private final ObjectMapper objectMapper;

	public PseudoVideoWorkspaceService(
			ModerationLessonBuilderService lessonBuilder,
			ModerationProperties properties,
			ObjectMapper objectMapper
	) {
		this.lessonBuilder = lessonBuilder;
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	public Path videoDirectory(LessonSubmission submission) {
		return this.lessonBuilder.videoDirectory(submission);
	}

	public PseudoVideoStatus readStatus(LessonSubmission submission) {
		Path videoDir = this.videoDirectory(submission);
		if (!Files.isDirectory(videoDir)) {
			return PseudoVideoStatus.empty();
		}
		try {
			int frameCount = this.countPngFrames(videoDir);
			if (frameCount <= 0) {
				return new PseudoVideoStatus(
						false,
						0,
						this.properties.getVideoFps(),
						this.properties.getVideoWidth(),
						this.properties.getVideoHeight(),
						this.findSourceFilename(videoDir)
				);
			}
			Path metaFile = videoDir.resolve("meta.json");
			if (Files.exists(metaFile)) {
				JsonNode meta = this.objectMapper.readTree(metaFile.toFile());
				return new PseudoVideoStatus(
						true,
						meta.path("frameCount").asInt(frameCount),
						meta.path("fps").asInt(this.properties.getVideoFps()),
						meta.path("width").asInt(this.properties.getVideoWidth()),
						meta.path("height").asInt(this.properties.getVideoHeight()),
						this.findSourceFilename(videoDir)
				);
			}
			return new PseudoVideoStatus(
					true,
					frameCount,
					this.properties.getVideoFps(),
					this.properties.getVideoWidth(),
					this.properties.getVideoHeight(),
					this.findSourceFilename(videoDir)
			);
		} catch (IOException exception) {
			return PseudoVideoStatus.empty();
		}
	}

	public List<Path> listFramePaths(LessonSubmission submission) throws IOException {
		Path videoDir = this.videoDirectory(submission);
		try (Stream<Path> stream = Files.list(videoDir)) {
			return stream.filter(path -> {
						String name = path.getFileName().toString();
						return name.startsWith("frame_") && name.endsWith(".png");
					})
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.toList();
		}
	}

	private int countPngFrames(Path videoDir) throws IOException {
		try (Stream<Path> stream = Files.list(videoDir)) {
			return (int) stream.filter(path -> path.getFileName().toString().endsWith(".png")).count();
		}
	}

	private String findSourceFilename(Path videoDir) throws IOException {
		try (Stream<Path> stream = Files.list(videoDir)) {
			return stream.map(path -> path.getFileName().toString())
					.filter(name -> name.startsWith("source."))
					.findFirst()
					.orElse(null);
		}
	}
}
