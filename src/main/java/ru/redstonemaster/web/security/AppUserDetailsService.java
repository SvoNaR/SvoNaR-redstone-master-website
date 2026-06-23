package ru.redstonemaster.web.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserService;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

	private final UserService userService;

	public AppUserDetailsService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = this.userService.findByLogin(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPasswordHash(),
				List.of(new SimpleGrantedAuthority(user.getRole().getAuthority()))
		);
	}
}
