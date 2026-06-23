package ru.redstonemaster.web.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class AvatarController {

	private final Path userAvatarsDir;

	public AvatarController(@org.springframework.beans.factory.annotation.Value("${app.avatars.user-dir:./data/avatars/users}") String userAvatarsDir) {
		this.userAvatarsDir = Path.of(userAvatarsDir);
	}

	@GetMapping("/avatars/users/{userId}.png")
	public ResponseEntity<Resource> userAvatar(@PathVariable long userId) {
		Path file = this.userAvatarsDir.resolve(userId + ".png");
		if (!Files.isRegularFile(file)) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_PNG)
				.body(new FileSystemResource(file));
	}
}
