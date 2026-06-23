package ru.redstonemaster.web.moderation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.moderation")
public class ModerationProperties {
	private String workspaceDir = "./data/moderation/workspaces";
	private String jarOutputDir = "./data/moderation/jars";
	private String ffmpegExecutable = "ffmpeg";
	private int videoFps = 15;
	private int videoWidth = 854;
	private int videoHeight = 480;

	public String getWorkspaceDir() {
		return this.workspaceDir;
	}

	public void setWorkspaceDir(String workspaceDir) {
		this.workspaceDir = workspaceDir;
	}

	public String getJarOutputDir() {
		return this.jarOutputDir;
	}

	public void setJarOutputDir(String jarOutputDir) {
		this.jarOutputDir = jarOutputDir;
	}

	public String getFfmpegExecutable() {
		return this.ffmpegExecutable;
	}

	public void setFfmpegExecutable(String ffmpegExecutable) {
		this.ffmpegExecutable = ffmpegExecutable;
	}

	public int getVideoFps() {
		return this.videoFps;
	}

	public void setVideoFps(int videoFps) {
		this.videoFps = videoFps;
	}

	public int getVideoWidth() {
		return this.videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
		return this.videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}
}
