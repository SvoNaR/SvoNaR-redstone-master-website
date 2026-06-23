package ru.redstonemaster.web.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.redstonemaster.web.modauth.ModAuthService;
import ru.redstonemaster.web.modauth.ModAuthSession;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ModAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final ModAuthService modAuthService;
	private final UserRepository userRepository;

	public ModAwareAuthenticationSuccessHandler(ModAuthService modAuthService, UserRepository userRepository) {
		this.modAuthService = modAuthService;
		this.userRepository = userRepository;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException, ServletException {
		String lang = request.getParameter("lang");
		if (lang == null || lang.isBlank()) {
			lang = "ru";
		}
		HttpSession session = request.getSession(false);
		if (session != null) {
			Object rawState = session.getAttribute(ModAuthSession.SESSION_STATE_KEY);
			if (rawState instanceof String state && !state.isBlank()) {
				User user = this.userRepository.findByUsernameIgnoreCase(authentication.getName()).orElseThrow();
				this.modAuthService.complete(state, user);
				session.removeAttribute(ModAuthSession.SESSION_STATE_KEY);
				response.sendRedirect("/auth/mod/return?state=" + state + "&lang=" + lang);
				return;
			}
		}
		response.sendRedirect("/profile?lang=" + lang + "&login=success");
	}
}
