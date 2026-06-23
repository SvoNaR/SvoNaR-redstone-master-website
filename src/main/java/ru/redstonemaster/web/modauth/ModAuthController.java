package ru.redstonemaster.web.modauth;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class ModAuthController {

	private final ModAuthService modAuthService;
	private final UserRepository userRepository;

	public ModAuthController(ModAuthService modAuthService, UserRepository userRepository) {
		this.modAuthService = modAuthService;
		this.userRepository = userRepository;
	}

	@GetMapping("/auth/mod/start")
	public String start(
			@RequestParam String state,
			@RequestParam int port,
			@RequestParam(name = "mode", defaultValue = "login") String mode,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			HttpSession session,
			Authentication authentication
	) {
		this.modAuthService.begin(state, port, mode);
		session.setAttribute(ModAuthSession.SESSION_STATE_KEY, state);
		if (authentication != null && authentication.isAuthenticated()
				&& !"anonymousUser".equals(authentication.getPrincipal())) {
			User user = this.userRepository.findByUsernameIgnoreCase(authentication.getName()).orElseThrow();
			this.modAuthService.complete(state, user);
			return "redirect:/auth/mod/return?state=" + state + "&lang=" + langCode;
		}
		String tab = "register".equals(mode) ? "register" : "login";
		return "redirect:/profile?lang=" + langCode + "&tab=" + tab + "&modAuth=1";
	}

	@GetMapping("/auth/mod/return")
	public String returnToMod(
			@RequestParam String state,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			HttpSession session
	) {
		session.removeAttribute(ModAuthSession.SESSION_STATE_KEY);
		ModAuthRequest request = this.modAuthService.requireCompleted(state);
		String exchangeCode = request.getExchangeCode();
		if (exchangeCode == null || exchangeCode.isBlank()) {
			throw new IllegalStateException("Mod auth exchange code is missing");
		}
		return "redirect:http://127.0.0.1:" + request.getCallbackPort()
				+ "/callback?state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
				+ "&code=" + URLEncoder.encode(exchangeCode, StandardCharsets.UTF_8);
	}
}
