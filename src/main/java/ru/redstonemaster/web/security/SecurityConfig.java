package ru.redstonemaster.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final ModAwareAuthenticationSuccessHandler modAwareAuthenticationSuccessHandler;

	public SecurityConfig(ModAwareAuthenticationSuccessHandler modAwareAuthenticationSuccessHandler) {
		this.modAwareAuthenticationSuccessHandler = modAwareAuthenticationSuccessHandler;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/news/publish").hasAnyRole("MODERATOR", "ADMIN")
				.requestMatchers("/news/*/edit", "/news/*/delete").hasAnyRole("MODERATOR", "ADMIN")
				.requestMatchers(
						"/css/**",
						"/mod-assets/**",
						"/avatars/**",
						"/api/**",
						"/auth/mod/**",
						"/profile",
						"/profile/register",
						"/profile/verify",
						"/profile/pending-verification",
						"/profile/pending-verification/**",
						"/news",
						"/news/*"
				).permitAll()
				.requestMatchers(
						"/profile/avatar",
						"/profile/change-email",
						"/profile/intro-seen",
						"/notifications",
						"/notifications/*"
				).authenticated()
				.requestMatchers("/moderation/**").hasAnyRole("MODERATOR", "ADMIN")
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().permitAll()
		);
		http.formLogin(form -> form
				.loginPage("/profile")
				.loginProcessingUrl("/profile/login")
				.usernameParameter("login")
				.passwordParameter("password")
				.successHandler(this.modAwareAuthenticationSuccessHandler)
				.failureUrl("/profile?error=login")
				.permitAll()
		);
		http.logout(logout -> logout
				.logoutUrl("/profile/logout")
				.logoutSuccessHandler((request, response, authentication) -> {
					String lang = request.getParameter("lang");
					if (lang == null || lang.isBlank()) {
						lang = "ru";
					}
					response.sendRedirect("/profile?lang=" + lang + "&logout=1");
				})
				.permitAll()
		);
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

