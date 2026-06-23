package ru.redstonemaster.web.moderation;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PseudoVideoArchiveService {

	private final PseudoVideoWorkspaceService workspaceService;

	public PseudoVideoArchiveService(PseudoVideoWorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	public Path buildFramesZip(LessonSubmission submission) throws IOException {
		Path videoDir = this.workspaceService.videoDirectory(submission);
		if (!Files.isDirectory(videoDir)) {
			throw new IOException("Video directory not found");
		}
		PseudoVideoStatus status = this.workspaceService.readStatus(submission);
		if (!status.ready()) {
			throw new IOException("PNG frames are not ready");
		}

		Path zipDir = videoDir.getParent().getParent().getParent().resolve("_exports");
		Files.createDirectories(zipDir);
		String zipName = String.format(
				Locale.ROOT,
				"%s-pseudo-video-%d.zip",
				submission.getVideoId(),
				submission.getId() != null ? submission.getId() : System.currentTimeMillis()
		);
		Path zipPath = zipDir.resolve(zipName);

		String assetPrefix = "assets/redstone-master/tutorials/videos/" + submission.getVideoId() + "/";
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipPath))) {
			for (Path file : this.workspaceService.listFramePaths(submission)) {
				String entryName = assetPrefix + file.getFileName();
				zip.putNextEntry(new ZipEntry(entryName));
				Files.copy(file, zip);
				zip.closeEntry();
			}
			Path meta = videoDir.resolve("meta.json");
			if (Files.exists(meta)) {
				zip.putNextEntry(new ZipEntry(assetPrefix + "meta.json"));
				Files.copy(meta, zip);
				zip.closeEntry();
			}
			zip.putNextEntry(new ZipEntry("README.txt"));
			String readme = """
					Pseudo-video for Redstone Master mod
					====================================
					Unpack into a Fabric lesson JAR or copy into:
					assets/redstone-master/tutorials/videos/%s/

					Reference in lesson JSON: "videos": ["%s"]
					""".formatted(submission.getVideoId(), submission.getVideoId());
			zip.write(readme.getBytes());
			zip.closeEntry();
		}
		return zipPath;
	}
}
