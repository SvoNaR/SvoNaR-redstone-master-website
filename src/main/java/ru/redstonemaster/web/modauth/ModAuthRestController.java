package ru.redstonemaster.web.modauth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/mod")
public class ModAuthRestController {

	private final ModAuthService modAuthService;

	public ModAuthRestController(ModAuthService modAuthService) {
		this.modAuthService = modAuthService;
	}

	@PostMapping("/exchange")
	public ModAuthProfileResponse exchange(@RequestBody ModAuthExchangeRequest request) {
		return this.modAuthService.exchange(request.state(), request.code());
	}
}
