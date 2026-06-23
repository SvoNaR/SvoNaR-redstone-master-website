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
			this.accumulator = 0;
			this.lastTick = 0;
			this.imageCache = new Map();

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
				this.frameIndex = Number(this.slider.value);
				this.drawFrame(this.frameIndex);
				this.updateTimeLabel();
			});
			this.slider.addEventListener("change", () => {
				this.scrubbing = false;
			});
			this.slider.addEventListener("pointerdown", () => {
				this.scrubbing = true;
			});
			this.slider.addEventListener("pointerup", () => {
				this.scrubbing = false;
			});
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
				await this.drawFrame(0);
				this.updateTimeLabel();
				this.updatePlayUi();
				requestAnimationFrame((time) => this.tick(time));
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
			this.drawFrame(this.frameIndex);
		}

		async loadImage(index) {
			if (this.imageCache.has(index)) {
				return this.imageCache.get(index);
			}
			const image = new Image();
			image.decoding = "async";
			const promise = new Promise((resolve, reject) => {
				image.onload = () => resolve(image);
				image.onerror = () => reject(new Error("Frame load failed: " + index));
			});
			image.src = frameUrl(this.baseUrl, index);
			this.imageCache.set(index, promise);
			if (this.imageCache.size > 24) {
				const oldest = this.imageCache.keys().next().value;
				this.imageCache.delete(oldest);
			}
			return promise;
		}

		async drawFrame(index) {
			if (!this.meta) {
				return;
			}
			const clamped = Math.max(0, Math.min(this.meta.frameCount - 1, index));
			this.frameIndex = clamped;
			this.slider.value = String(clamped);
			try {
				const image = await this.loadImage(clamped);
				this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
				this.ctx.drawImage(image, 0, 0, this.canvas.width, this.canvas.height);
			} catch (error) {
				console.warn(error);
			}
		}

		tick(now) {
			if (this.meta && this.playing && !this.scrubbing) {
				if (this.lastTick === 0) {
					this.lastTick = now;
				}
				const delta = (now - this.lastTick) / 1000;
				this.lastTick = now;
				this.accumulator += delta;
				const frameDuration = 1 / (this.meta.fps || 15);
				while (this.accumulator >= frameDuration) {
					this.accumulator -= frameDuration;
					if (this.frameIndex >= this.meta.frameCount - 1) {
						this.playing = false;
						this.accumulator = 0;
						break;
					}
					this.frameIndex += 1;
					this.drawFrame(this.frameIndex);
				}
				this.updateTimeLabel();
				this.updatePlayUi();
			} else {
				this.lastTick = now;
			}
			requestAnimationFrame((time) => this.tick(time));
		}

		togglePlay() {
			if (!this.meta) {
				return;
			}
			if (this.playing) {
				this.playing = false;
			} else {
				if (this.frameIndex >= this.meta.frameCount - 1) {
					this.frameIndex = 0;
					this.drawFrame(0);
				}
				this.playing = true;
				this.lastTick = 0;
				this.accumulator = 0;
			}
			this.updatePlayUi();
		}

		seekBySeconds(deltaSeconds) {
			if (!this.meta) {
				return;
			}
			const fps = this.meta.fps || 15;
			const next = this.frameIndex + Math.round(deltaSeconds * fps);
			this.drawFrame(next);
			this.updateTimeLabel();
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
