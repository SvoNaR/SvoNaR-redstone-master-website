package ru.redstonemaster.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.redstonemaster.web.locale.WebLocale;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLocaleFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		String lang = request.getParameter("lang");
		if (lang != null && !lang.isBlank()) {
			LocaleContextHolder.setLocale(Locale.forLanguageTag(WebLocale.fromCode(lang).getCode()));
		}
		try {
			filterChain.doFilter(request, response);
		} finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}
}

