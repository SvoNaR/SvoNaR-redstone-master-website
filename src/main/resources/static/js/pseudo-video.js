(function () {
	"use strict";

	const DEFAULT_WIDTH = 854;
	const DEFAULT_HEIGHT = 480;

	function formatTime(seconds) {
		const total = Math.max(0, Math.floor(seconds));
		const minutes = Math.floor(total / 60);
		const secs = total % 60;
		return minutes + ":" + String(secs).padStart(2, "0");
	}

	class PseudoVideoPlayer {
		constructor(root) {
			this.root = root;
			this.baseUrl = root.dataset.videoBase || "";
			this.frameElement = root.querySelector(".pseudo-video-player__frame");
			this.video = root.querySelector(".pseudo-video-player__video");
			this.canvas = root.querySelector(".pseudo-video-player__canvas");
			this.ctx = this.canvas ? this.canvas.getContext("2d") : null;
			this.slider = root.querySelector(".pseudo-video-player__slider");
			this.timeLabel = root.querySelector(".pseudo-video-player__time");
			this.errorLabel = root.querySelector(".pseudo-video-player__error");
			this.playOverlay = root.querySelector(".pseudo-video-player__play-overlay");
			this.playToggle = root.querySelector(".pseudo-video-player__play-toggle");
			this.seekBack = root.querySelector(".pseudo-video-player__seek-back");
			this.seekForward = root.querySelector(".pseudo-video-player__seek-forward");

			this.mode = null;
			this.meta = null;
			this.frameIndex = 0;
			this.playing = false;
			this.scrubbing = false;
			this.displayGeneration = 0;
			this.playTimer = null;
			this.imagePromises = new Map();
			this.loadedImages = new Map();

			this.applyInitialLayout();
			this.bindEvents();
			this.load();
		}

		applyInitialLayout() {
			if (this.frameElement) {
				this.frameElement.style.aspectRatio = DEFAULT_WIDTH + " / " + DEFAULT_HEIGHT;
			}
			if (this.canvas) {
				this.canvas.style.width = "100%";
				this.canvas.style.height = "100%";
			}
			if (this.video) {
				this.video.style.width = "100%";
				this.video.style.height = "100%";
				this.video.playsInline = true;
				this.video.preload = "auto";
			}
		}

		bindEvents() {
			this.playOverlay.addEventListener("click", () => this.togglePlay());
			this.playToggle.addEventListener("click", () => this.togglePlay());
			this.seekBack.addEventListener("click", () => this.seekBySeconds(-5));
			this.seekForward.addEventListener("click", () => this.seekBySeconds(5));

			this.slider.addEventListener("input", () => {
				this.scrubbing = true;
				this.seekToProgress(Number(this.slider.value) / Number(this.slider.max || 1));
			});
			this.slider.addEventListener("change", () => {
				this.scrubbing = false;
			});
			this.slider.addEventListener("pointerdown", () => {
				this.scrubbing = true;
				if (this.mode === "frames") {
					this.stopFramePlaybackLoop();
					this.bumpDisplayGeneration();
				}
			});
			this.slider.addEventListener("pointerup", () => {
				this.scrubbing = false;
			});

			if (this.video) {
				this.video.addEventListener("timeupdate", () => {
					if (this.mode !== "html5" || this.scrubbing) {
						return;
					}
					this.syncSliderFromVideo();
					this.updateTimeLabel();
				});
				this.video.addEventListener("play", () => {
					if (this.mode === "html5") {
						this.playing = true;
						this.updatePlayUi();
					}
				});
				this.video.addEventListener("pause", () => {
					if (this.mode === "html5") {
						this.playing = false;
						this.updatePlayUi();
					}
				});
				this.video.addEventListener("ended", () => {
					if (this.mode === "html5") {
						this.playing = false;
						this.updatePlayUi();
					}
				});
			}
		}

		bumpDisplayGeneration() {
			this.displayGeneration += 1;
		}

		showError(message) {
			if (this.errorLabel) {
				this.errorLabel.hidden = false;
				this.errorLabel.textContent = message;
			}
		}

		async load() {
			try {
				const response = await fetch(this.baseUrl + "meta.json");
				if (!response.ok) {
					throw new Error("meta.json HTTP " + response.status);
				}
				this.meta = await response.json();
				if (await this.tryLoadHtml5Video()) {
					return;
				}
				await this.loadFramePlayer();
			} catch (error) {
				console.error("Pseudo video load failed", error);
				this.showError(
					this.root.dataset.locale === "en"
						? "Could not load tutorial video."
						: "Не удалось загрузить видео урока."
				);
			}
		}

		async tryLoadHtml5Video() {
			if (!this.video) {
				return false;
			}
			const videoUrl = this.baseUrl + "playback.mp4";
			try {
				const probe = await fetch(videoUrl, { method: "HEAD" });
				if (!probe.ok) {
					return false;
				}
			} catch (error) {
				return false;
			}

			this.mode = "html5";
			if (this.canvas) {
				this.canvas.hidden = true;
			}
			this.video.hidden = false;
			this.video.src = videoUrl;

			await new Promise((resolve, reject) => {
				const onReady = () => {
					cleanup();
					resolve();
				};
				const onError = () => {
					cleanup();
					reject(new Error("playback.mp4 failed to load"));
				};
				const cleanup = () => {
					this.video.removeEventListener("loadedmetadata", onReady);
					this.video.removeEventListener("error", onError);
				};
				this.video.addEventListener("loadedmetadata", onReady);
				this.video.addEventListener("error", onError);
				this.video.load();
			});

			this.slider.max = "1000";
			this.slider.value = "0";
			this.applyAspectRatio(this.meta.width || DEFAULT_WIDTH, this.meta.height || DEFAULT_HEIGHT);
			this.updateTimeLabel();
			this.updatePlayUi();
			return true;
		}

		async loadFramePlayer() {
			this.mode = "frames";
			if (this.video) {
				this.video.hidden = true;
			}
			if (this.canvas) {
				this.canvas.hidden = false;
			}

			const frameCount = this.meta.frameCount || 0;
			if (frameCount <= 0) {
				throw new Error("Video has no frames");
			}
			this.slider.max = String(frameCount - 1);
			this.slider.value = "0";
			this.resizeCanvas();
			await this.presentFrame(0);
			this.preloadRange(1, 40);
			this.updateTimeLabel();
			this.updatePlayUi();
		}

		applyAspectRatio(width, height) {
			if (this.frameElement) {
				this.frameElement.style.aspectRatio = width + " / " + height;
			}
		}

		resizeCanvas() {
			if (!this.meta || !this.canvas) {
				return;
			}
			const width = this.meta.width || DEFAULT_WIDTH;
			const height = this.meta.height || DEFAULT_HEIGHT;
			this.canvas.width = width;
			this.canvas.height = height;
			this.applyAspectRatio(width, height);
			this.presentFrame(this.frameIndex);
		}

		preloadRange(startIndex, count) {
			if (!this.meta) {
				return;
			}
			const end = Math.min(this.meta.frameCount, startIndex + count);
			for (let index = startIndex; index < end; index += 1) {
				this.loadImage(index).catch(() => {
				});
			}
		}

		loadImage(index) {
			if (this.loadedImages.has(index)) {
				return Promise.resolve(this.loadedImages.get(index));
			}
			if (this.imagePromises.has(index)) {
				return this.imagePromises.get(index);
			}

			const image = new Image();
			image.decoding = "async";
			const promise = new Promise((resolve, reject) => {
				image.onload = () => {
					this.loadedImages.set(index, image);
					resolve(image);
				};
				image.onerror = () => reject(new Error("Frame load failed: " + index));
			});
			image.src = this.baseUrl + "frame_" + String(index).padStart(5, "0") + ".png";
			this.imagePromises.set(index, promise);
			return promise;
		}

		async presentFrame(index) {
			if (!this.meta || !this.ctx) {
				return;
			}
			const clamped = Math.max(0, Math.min(this.meta.frameCount - 1, index));
			const generation = this.displayGeneration;
			try {
				const image = await this.loadImage(clamped);
				if (generation !== this.displayGeneration) {
					return;
				}
				this.frameIndex = clamped;
				this.slider.value = String(clamped);
				this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
				this.ctx.drawImage(image, 0, 0, this.canvas.width, this.canvas.height);
				this.updateTimeLabel();
			} catch (error) {
				console.warn(error);
			}
		}

		stopFramePlaybackLoop() {
			if (this.playTimer !== null) {
				clearTimeout(this.playTimer);
				this.playTimer = null;
			}
		}

		frameDurationMs() {
			const fps = this.meta && this.meta.fps ? this.meta.fps : 15;
			return 1000 / Math.max(1, fps);
		}

		startFramePlaybackLoop() {
			this.stopFramePlaybackLoop();
			const schedule = () => {
				if (!this.playing || this.scrubbing || !this.meta || this.mode !== "frames") {
					return;
				}
				const generation = this.displayGeneration;
				const nextIndex = this.frameIndex + 1;
				if (nextIndex >= this.meta.frameCount) {
					this.playing = false;
					this.updatePlayUi();
					return;
				}

				this.loadImage(nextIndex)
					.then(() => {
						if (!this.playing || this.scrubbing || generation !== this.displayGeneration) {
							return;
						}
						return this.presentFrame(nextIndex);
					})
					.then(() => {
						if (!this.playing || this.scrubbing || generation !== this.displayGeneration) {
							return;
						}
						this.preloadRange(nextIndex + 1, 40);
						this.playTimer = setTimeout(schedule, this.frameDurationMs());
					})
					.catch((error) => {
						console.warn(error);
						if (this.playing && generation === this.displayGeneration) {
							this.playTimer = setTimeout(schedule, this.frameDurationMs());
						}
					});
			};
			this.playTimer = setTimeout(schedule, this.frameDurationMs());
		}

		syncSliderFromVideo() {
			if (!this.video || !this.video.duration) {
				return;
			}
			const progress = this.video.currentTime / this.video.duration;
			this.slider.value = String(Math.round(progress * 1000));
		}

		seekToProgress(progress) {
			if (this.mode === "html5" && this.video && this.video.duration) {
				this.video.currentTime = Math.max(0, Math.min(1, progress)) * this.video.duration;
				this.syncSliderFromVideo();
				this.updateTimeLabel();
				return;
			}
			if (this.mode === "frames" && this.meta) {
				const index = Math.round(progress * (this.meta.frameCount - 1));
				this.presentFrame(index);
			}
		}

		togglePlay() {
			if (this.mode === "html5" && this.video) {
				if (this.video.paused) {
					void this.video.play();
				} else {
					this.video.pause();
				}
				return;
			}
			if (!this.meta) {
				return;
			}
			if (this.playing) {
				this.playing = false;
				this.bumpDisplayGeneration();
				this.stopFramePlaybackLoop();
			} else {
				if (this.frameIndex >= this.meta.frameCount - 1) {
					this.bumpDisplayGeneration();
					void this.presentFrame(0);
				}
				this.playing = true;
				this.preloadRange(this.frameIndex + 1, 40);
				this.startFramePlaybackLoop();
			}
			this.updatePlayUi();
		}

		seekBySeconds(deltaSeconds) {
			if (this.mode === "html5" && this.video) {
				this.video.currentTime = Math.max(
					0,
					Math.min(this.video.duration || 0, this.video.currentTime + deltaSeconds)
				);
				this.syncSliderFromVideo();
				this.updateTimeLabel();
				return;
			}
			if (!this.meta) {
				return;
			}
			const wasPlaying = this.playing;
			this.playing = false;
			this.bumpDisplayGeneration();
			this.stopFramePlaybackLoop();
			const fps = this.meta.fps || 15;
			const next = this.frameIndex + Math.round(deltaSeconds * fps);
			void this.presentFrame(next).then(() => {
				this.preloadRange(this.frameIndex + 1, 40);
				if (wasPlaying) {
					this.playing = true;
					this.startFramePlaybackLoop();
					this.updatePlayUi();
				}
			});
		}

		updateTimeLabel() {
			if (!this.meta || !this.timeLabel) {
				return;
			}
			if (this.mode === "html5" && this.video) {
				const current = this.video.currentTime || 0;
				const total = this.video.duration || (this.meta.frameCount / (this.meta.fps || 15));
				this.timeLabel.textContent = formatTime(current) + " / " + formatTime(total);
				return;
			}
			const fps = this.meta.fps || 15;
			const current = this.frameIndex / fps;
			const total = this.meta.frameCount / fps;
			this.timeLabel.textContent = formatTime(current) + " / " + formatTime(total);
		}

		updatePlayUi() {
			const playing = this.mode === "html5" && this.video
				? !this.video.paused
				: this.playing;
			const symbol = playing ? "\u23F8" : "\u25B6";
			this.playToggle.textContent = symbol;
			this.playOverlay.textContent = symbol;
			this.playOverlay.hidden = playing;
		}
	}

	document.querySelectorAll(".pseudo-video-player").forEach((root) => {
		new PseudoVideoPlayer(root);
	});
})();
