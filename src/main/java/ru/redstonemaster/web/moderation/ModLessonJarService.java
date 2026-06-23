package ru.redstonemaster.web.moderation;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ModLessonJarService {

	private final ModerationProperties properties;

	public ModLessonJarService(ModerationProperties properties) {
		this.properties = properties;
	}

	public Path buildLessonJar(LessonSubmission submission) throws IOException {
		Path workspace = Path.of(submission.getWorkspacePath());
		if (!Files.isDirectory(workspace)) {
			throw new IOException("Workspace not found: " + workspace);
		}

		Path outputDir = Path.of(this.properties.getJarOutputDir());
		Files.createDirectories(outputDir);
		String jarName = String.format(
				Locale.ROOT,
				"redstone-master-lesson-%s-%d.jar",
				submission.getLessonId(),
				submission.getId()
		);
		Path jarPath = outputDir.resolve(jarName);

		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(jarPath))) {
			this.writeLessonFabricModJson(zip, submission.getLessonId());
			Path assetsRoot = workspace.resolve("assets");
			if (Files.isDirectory(assetsRoot)) {
				Files.walkFileTree(assetsRoot, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						String entryName = assetsRoot.relativize(file).toString().replace('\\', '/');
						zip.putNextEntry(new ZipEntry("assets/" + entryName));
						Files.copy(file, zip);
						zip.closeEntry();
						return FileVisitResult.CONTINUE;
					}
				});
			}
		}
		return jarPath;
	}

	public Path buildVideoOnlyJar(LessonSubmission submission) throws IOException {
		Path workspace = Path.of(submission.getWorkspacePath());
		Path videoDir = workspace.resolve("assets/redstone-master/tutorials/videos").resolve(submission.getVideoId());
		if (!Files.isDirectory(videoDir)) {
			throw new IOException("Video frames not found for id: " + submission.getVideoId());
		}
		try (Stream<Path> stream = Files.list(videoDir)) {
			boolean hasPng = stream.anyMatch(path -> path.getFileName().toString().endsWith(".png"));
			if (!hasPng) {
				throw new IOException("No PNG frames in video directory");
			}
		}

		Path outputDir = Path.of(this.properties.getJarOutputDir());
		Files.createDirectories(outputDir);
		String jarName = String.format(
				Locale.ROOT,
				"redstone-master-pseudo-video-%s-%d.jar",
				submission.getVideoId(),
				submission.getId()
		);
		Path jarPath = outputDir.resolve(jarName);

		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(jarPath))) {
			this.writePseudoVideoFabricModJson(zip, submission.getVideoId());
			String prefix = "assets/redstone-master/tutorials/videos/" + submission.getVideoId() + "/";
			try (Stream<Path> stream = Files.list(videoDir)) {
				for (Path file : stream.filter(Files::isRegularFile).sorted().toList()) {
					zip.putNextEntry(new ZipEntry(prefix + file.getFileName()));
					Files.copy(file, zip);
					zip.closeEntry();
				}
			}
		}
		return jarPath;
	}

	private void writeLessonFabricModJson(ZipOutputStream zip, String lessonId) throws IOException {
		String json = """
				{
				  "schemaVersion": 1,
				  "id": "redstone-master-lesson-%s",
				  "version": "1.0.0",
				  "name": "Redstone Master Lesson %s",
				  "description": "Lesson pack created by moderation tools",
				  "environment": "client",
				  "depends": {
				    "redstone-master": "*"
				  }
				}
				""".formatted(lessonId, lessonId);
		zip.putNextEntry(new ZipEntry("fabric.mod.json"));
		zip.write(json.getBytes());
		zip.closeEntry();
	}

	private void writePseudoVideoFabricModJson(ZipOutputStream zip, String videoId) throws IOException {
		String json = """
				{
				  "schemaVersion": 1,
				  "id": "redstone-master-pseudo-video-%s",
				  "version": "1.0.0",
				  "name": "Redstone Master Pseudo-Video %s",
				  "description": "Pseudo-video frame pack for Redstone Master",
				  "environment": "client",
				  "depends": {
				    "redstone-master": "*"
				  }
				}
				""".formatted(videoId, videoId);
		zip.putNextEntry(new ZipEntry("fabric.mod.json"));
		zip.write(json.getBytes());
		zip.closeEntry();
	}

	public boolean workspaceHasVideoFrames(LessonSubmission submission) throws IOException {
		Path videoDir = Path.of(submission.getWorkspacePath())
				.resolve("assets/redstone-master/tutorials/videos")
				.resolve(submission.getVideoId());
		if (!Files.isDirectory(videoDir)) {
			return false;
		}
		try (Stream<Path> stream = Files.list(videoDir)) {
			return stream.anyMatch(path -> {
				String name = path.getFileName().toString();
				return name.endsWith(".png");
			});
		}
	}
}
