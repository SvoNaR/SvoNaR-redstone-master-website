package ru.redstonemaster.web.modauth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redstonemaster.web.tutorial.TutorialProgressService;
import ru.redstonemaster.web.tutorial.TutorialProgressStats;
import ru.redstonemaster.web.user.User;

import java.util.List;

@RestController
@RequestMapping("/api/mod")
public class ModSyncRestController {

	private final ModSyncAuthService modSyncAuthService;
	private final ModAuthService modAuthService;
	private final TutorialProgressService tutorialProgressService;

	public ModSyncRestController(
			ModSyncAuthService modSyncAuthService,
			ModAuthService modAuthService,
			TutorialProgressService tutorialProgressService
	) {
		this.modSyncAuthService = modSyncAuthService;
		this.modAuthService = modAuthService;
		this.tutorialProgressService = tutorialProgressService;
	}

	@GetMapping("/profile")
	public ModAuthProfileResponse profile(@RequestHeader(value = "Authorization", required = false) String authorization) {
		User user = this.modSyncAuthService.requireUser(authorization);
		return this.modAuthService.buildProfileResponse(user);
	}

	@PostMapping("/tutorial/progress")
	public ModAuthProfileResponse syncProgress(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody ModSyncProgressRequest request
	) {
		User user = this.modSyncAuthService.requireUser(authorization);
		TutorialProgressStats stats = this.tutorialProgressService.mergeCompletedLessons(
				user.getId(),
				this.tutorialProgressService.parseLessonKeys(
						request != null ? request.completedLessons() : List.of()
				)
		);
		return this.modAuthService.buildProfileResponse(user, stats);
	}
}
