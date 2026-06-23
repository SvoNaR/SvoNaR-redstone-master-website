package ru.redstonemaster.web.moderation;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class VideoToPngFramesService {

	private final ModerationProperties properties;

	public VideoToPngFramesService(ModerationProperties properties) {
		this.properties = properties;
	}

	public void ensureFfmpegAvailable() throws IOException {
		Process process = new ProcessBuilder(this.properties.getFfmpegExecutable(), "-version")
				.redirectErrorStream(true)
				.start();
		try {
			boolean finished = process.waitFor(15, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				throw new IOException("FFmpeg check timed out");
			}
			if (process.exitValue() != 0) {
				throw new IOException("FFmpeg is not available (exit code " + process.exitValue() + ")");
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IOException("FFmpeg check interrupted", exception);
		}
	}

	public List<Path> convertToPngFrames(Path inputVideo, Path outputDirectory) throws IOException, InterruptedException {
		this.ensureFfmpegAvailable();
		Files.createDirectories(outputDirectory);
		try (var stream = Files.list(outputDirectory)) {
			stream.filter(path -> {
				String name = path.getFileName().toString();
				return name.startsWith("frame_") && name.endsWith(".png");
			}).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ignored) {
				}
			});
		}

		String outputPattern = outputDirectory.resolve("frame_%05d.png").toString();
		List<String> command = List.of(
				this.properties.getFfmpegExecutable(),
				"-y",
				"-i", inputVideo.toString(),
				"-vf", "scale=" + this.properties.getVideoWidth() + ":" + this.properties.getVideoHeight(),
				"-r", Integer.toString(this.properties.getVideoFps()),
				outputPattern
		);
		Process process = new ProcessBuilder(command)
				.redirectErrorStream(true)
				.start();
		boolean finished = process.waitFor(10, TimeUnit.MINUTES);
		if (!finished) {
			process.destroyForcibly();
			throw new IOException("FFmpeg timed out while converting video to PNG frames");
		}
		if (process.exitValue() != 0) {
			String log = this.readProcessOutput(process);
			throw new IOException(
					"FFmpeg failed with exit code " + process.exitValue()
							+ (log.isBlank() ? "" : ": " + log)
			);
		}

		List<Path> frames = new ArrayList<>();
		try (var stream = Files.list(outputDirectory)) {
			stream.filter(path -> path.getFileName().toString().startsWith("frame_")
							&& path.getFileName().toString().endsWith(".png"))
					.sorted()
					.forEach(frames::add);
		}
		if (frames.isEmpty()) {
			throw new IOException("FFmpeg produced no PNG frames");
		}
		return frames;
	}

	public void writeVideoMeta(Path videoDirectory, int frameCount) throws IOException {
		String json = String.format(
				Locale.ROOT,
				"{\"fps\":%d,\"width\":%d,\"height\":%d,\"frameCount\":%d}%n",
				this.properties.getVideoFps(),
				this.properties.getVideoWidth(),
				this.properties.getVideoHeight(),
				frameCount
		);
		Files.writeString(videoDirectory.resolve("meta.json"), json);
	}

	private String readProcessOutput(Process process) throws IOException {
		try (InputStream input = process.getInputStream()) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
		}
	}
}
