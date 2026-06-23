package ru.redstonemaster.web.modauth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import java.util.Optional;

@Service
public class ModSyncAuthService {

	private final UserRepository userRepository;

	public ModSyncAuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public User requireUser(String authorizationHeader) {
		return this.resolveUser(authorizationHeader)
				.orElseThrow(() -> new IllegalArgumentException("Invalid mod sync token"));
	}

	@Transactional(readOnly = true)
	public Optional<User> resolveUser(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			return Optional.empty();
		}
		String prefix = "Bearer ";
		if (!authorizationHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
			return Optional.empty();
		}
		String token = authorizationHeader.substring(prefix.length()).trim();
		if (token.isEmpty()) {
			return Optional.empty();
		}
		return this.userRepository.findByModSyncToken(token);
	}
}
