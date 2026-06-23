(function () {
	"use strict";

	const DEFAULT_WIDTH = 854;
	const DEFAULT_HEIGHT = 480;
	const PRELOAD_AHEAD = 20;
	const MAX_CACHE_SIZE = 80;

	function formatTime(seconds) {
		const total = Math.max(0, Math.floor(seconds));
		const minutes = Math.floor(total / 60);
		const secs = total % 60;
		return minutes + ":" + String(secs).padStart(2, "0");
	}

	function frameUrl(baseUrl, index) {
		return baseUrl + "frame_" + String(index).padStart(5, "0") + ".png";
	}

	class PseudoVideoPlayer {
		constructor(root) {
			this.root = root;
			this.baseUrl = root.dataset.videoBase || "";
			this.canvas = root.querySelector(".pseudo-video-player__canvas");
			this.ctx = this.canvas.getContext("2d");
			this.slider = root.querySelector(".pseudo-video-player__slider");
			this.timeLabel = root.querySelector(".pseudo-video-player__time");
			this.errorLabel = root.querySelector(".pseudo-video-player__error");
			this.playOverlay = root.querySelector(".pseudo-video-player__play-overlay");
			this.playToggle = root.querySelector(".pseudo-video-player__play-toggle");
			this.seekBack = root.querySelector(".pseudo-video-player__seek-back");
			this.seekForward = root.querySelector(".pseudo-video-player__seek-forward");
			this.frameElement = root.querySelector(".pseudo-video-player__frame");

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
			this.canvas.style.width = "100%";
			this.canvas.style.height = "100%";
		}

		bindEvents() {
			this.playOverlay.addEventListener("click", () => this.togglePlay());
			this.playToggle.addEventListener("click", () => this.togglePlay());
			this.seekBack.addEventListener("click", () => this.seekBySeconds(-5));
			this.seekForward.addEventListener("click", () => this.seekBySeconds(5));

			this.slider.addEventListener("input", () => {
				this.scrubbing = true;
				this.stopPlaybackLoop();
				this.bumpDisplayGeneration();
				this.presentFrame(Number(this.slider.value));
			});
			this.slider.addEventListener("change", () => {
				this.scrubbing = false;
			});
			this.slider.addEventListener("pointerdown", () => {
				this.scrubbing = true;
				this.stopPlaybackLoop();
				this.bumpDisplayGeneration();
			});
			this.slider.addEventListener("pointerup", () => {
				this.scrubbing = false;
			});
		}

		bumpDisplayGeneration() {
			this.displayGeneration += 1;
		}

		frameDurationMs() {
			const fps = this.meta && this.meta.fps ? this.meta.fps : 15;
			return 1000 / Math.max(1, fps);
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
				const frameCount = this.meta.frameCount || 0;
				if (frameCount <= 0) {
					throw new Error("Video has no frames");
				}
				this.slider.max = String(frameCount - 1);
				this.slider.value = "0";
				this.resizeCanvas();
				await this.presentFrame(0);
				this.preloadRange(1, PRELOAD_AHEAD);
				this.updateTimeLabel();
				this.updatePlayUi();
			} catch (error) {
				console.error("Pseudo video load failed", error);
				this.showError(
					this.root.dataset.locale === "en"
						? "Could not load tutorial video."
						: "Не удалось загрузить видео урока."
				);
			}
		}

		resizeCanvas() {
			if (!this.meta) {
				return;
			}
			const width = this.meta.width || DEFAULT_WIDTH;
			const height = this.meta.height || DEFAULT_HEIGHT;
			this.canvas.width = width;
			this.canvas.height = height;
			if (this.frameElement) {
				this.frameElement.style.aspectRatio = width + " / " + height;
			}
			this.presentFrame(this.frameIndex);
		}

		trimCache() {
			while (this.imagePromises.size > MAX_CACHE_SIZE) {
				const oldest = this.imagePromises.keys().next().value;
				this.imagePromises.delete(oldest);
				this.loadedImages.delete(oldest);
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
			image.src = frameUrl(this.baseUrl, index);
			this.imagePromises.set(index, promise);
			this.trimCache();
			return promise;
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

		async presentFrame(index) {
			if (!this.meta) {
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

		stopPlaybackLoop() {
			if (this.playTimer !== null) {
				clearTimeout(this.playTimer);
				this.playTimer = null;
			}
		}

		startPlaybackLoop() {
			this.stopPlaybackLoop();
			const schedule = () => {
				if (!this.playing || this.scrubbing || !this.meta) {
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
						this.preloadRange(nextIndex + 1, PRELOAD_AHEAD);
						this.updatePlayUi();
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

		togglePlay() {
			if (!this.meta) {
				return;
			}
			if (this.playing) {
				this.playing = false;
				this.bumpDisplayGeneration();
				this.stopPlaybackLoop();
			} else {
				if (this.frameIndex >= this.meta.frameCount - 1) {
					this.bumpDisplayGeneration();
					this.presentFrame(0);
				}
				this.playing = true;
				this.preloadRange(this.frameIndex + 1, PRELOAD_AHEAD);
				this.startPlaybackLoop();
			}
			this.updatePlayUi();
		}

		seekBySeconds(deltaSeconds) {
			if (!this.meta) {
				return;
			}
			const wasPlaying = this.playing;
			this.playing = false;
			this.bumpDisplayGeneration();
			this.stopPlaybackLoop();

			const fps = this.meta.fps || 15;
			const next = this.frameIndex + Math.round(deltaSeconds * fps);
			this.presentFrame(next).then(() => {
				this.preloadRange(this.frameIndex + 1, PRELOAD_AHEAD);
				if (wasPlaying) {
					this.playing = true;
					this.startPlaybackLoop();
					this.updatePlayUi();
				}
			});
		}

		updateTimeLabel() {
			if (!this.meta) {
				return;
			}
			const fps = this.meta.fps || 15;
			const current = this.frameIndex / fps;
			const total = this.meta.frameCount / fps;
			this.timeLabel.textContent = formatTime(current) + " / " + formatTime(total);
		}

		updatePlayUi() {
			const symbol = this.playing ? "\u23F8" : "\u25B6";
			this.playToggle.textContent = symbol;
			this.playOverlay.textContent = symbol;
			this.playOverlay.hidden = this.playing;
		}
	}

	document.querySelectorAll(".pseudo-video-player").forEach((root) => {
		new PseudoVideoPlayer(root);
	});
})();
