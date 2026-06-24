package ru.redstonemaster.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.modauth.ModAuthService;
import ru.redstonemaster.web.modauth.ModAuthSession;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.profile.AvatarValidationException;
import ru.redstonemaster.web.profile.ChangeEmailForm;
import ru.redstonemaster.web.profile.ChangeEmailFormValidator;
import ru.redstonemaster.web.profile.LoginForm;
import ru.redstonemaster.web.profile.PendingChangeEmailFormValidator;
import ru.redstonemaster.web.profile.ProfileUserView;
import ru.redstonemaster.web.profile.RegisterForm;
import ru.redstonemaster.web.profile.RegisterFormValidator;
import ru.redstonemaster.web.profile.SkinToAvatarService;
import ru.redstonemaster.web.security.LoginHelper;
import ru.redstonemaster.web.user.EmailVerificationService;
import ru.redstonemaster.web.user.PendingRegistration;
import ru.redstonemaster.web.user.PendingRegistrationLookup;
import ru.redstonemaster.web.user.PendingRegistrationService;
import ru.redstonemaster.web.user.PendingRegistrationSession;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserService;
import ru.redstonemaster.web.tutorial.TutorialProgressService;
import ru.redstonemaster.web.tutorial.TutorialProgressStats;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

@Controller
public class ProfileController {

	private final UserService userService;
	private final PendingRegistrationService pendingRegistrationService;
	private final EmailVerificationService emailVerificationService;
	private final RegisterFormValidator registerFormValidator;
	private final ChangeEmailFormValidator changeEmailFormValidator;
	private final PendingChangeEmailFormValidator pendingChangeEmailFormValidator;
	private final LoginHelper loginHelper;
	private final AvatarService avatarService;
	private final ModAuthService modAuthService;
	private final TutorialProgressService tutorialProgressService;

	public ProfileController(
			UserService userService,
			PendingRegistrationService pendingRegistrationService,
			EmailVerificationService emailVerificationService,
			RegisterFormValidator registerFormValidator,
			ChangeEmailFormValidator changeEmailFormValidator,
			PendingChangeEmailFormValidator pendingChangeEmailFormValidator,
			LoginHelper loginHelper,
			AvatarService avatarService,
			ModAuthService modAuthService,
			TutorialProgressService tutorialProgressService
	) {
		this.userService = userService;
		this.pendingRegistrationService = pendingRegistrationService;
		this.emailVerificationService = emailVerificationService;
		this.registerFormValidator = registerFormValidator;
		this.changeEmailFormValidator = changeEmailFormValidator;
		this.pendingChangeEmailFormValidator = pendingChangeEmailFormValidator;
		this.loginHelper = loginHelper;
		this.avatarService = avatarService;
		this.modAuthService = modAuthService;
		this.tutorialProgressService = tutorialProgressService;
	}

	@GetMapping("/profile")
	public String profile(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "tab", defaultValue = "login") String tab,
			@RequestParam(name = "error", required = false) String error,
			@RequestParam(name = "login", required = false) String loginSuccess,
			@RequestParam(name = "logout", required = false) String logoutSuccess,
			@RequestParam(name = "registrationExpired", required = false) String registrationExpired,
			@RequestParam(name = "modAuth", required = false) String modAuth,
			Authentication authentication,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Profile" : "Профиль");
		model.addAttribute("activeTab", tab);
		model.addAttribute("modAuthFlow", modAuth != null);

		if (authentication != null && authentication.isAuthenticated()
				&& !"anonymousUser".equals(authentication.getPrincipal())) {
			User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
			TutorialProgressStats progressStats = this.tutorialProgressService.getStats(user.getId());
			model.addAttribute("profileUser", ProfileUserView.from(user, this.avatarService));
			model.addAttribute("tutorialProgress", progressStats);
			model.addAttribute("showProfileIntro", !user.isProfileIntroSeen());
			model.addAttribute("pendingEmail", user.getPendingEmail());
			return "profile/index";
		}

		if (!model.containsAttribute("loginForm")) {
			model.addAttribute("loginForm", new LoginForm());
		}
		if (!model.containsAttribute("registerForm")) {
			model.addAttribute("registerForm", new RegisterForm());
		}
		model.addAttribute("loginError", "login".equals(error));
		model.addAttribute("loginSuccess", loginSuccess != null);
		model.addAttribute("logoutSuccess", logoutSuccess != null);
		model.addAttribute("registrationExpired", registrationExpired != null);
		return "profile/guest";
	}

	@GetMapping("/profile/pending-verification")
	public String pendingVerificationPage(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			HttpSession session,
			Model model
	) {
		PendingRegistrationLookup lookup = this.lookupPendingRegistration(session);
		if (lookup.pending().isEmpty()) {
			if (lookup.wasExpired()) {
				return "redirect:/profile?lang=" + langCode + "&tab=register&registrationExpired=1";
			}
			return "redirect:/profile?lang=" + langCode + "&tab=register";
		}
		PendingRegistration pending = lookup.pending().get();
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Confirm your email" : "Подтверждение почты");
		model.addAttribute("pendingUsername", pending.getUsername());
		model.addAttribute("pendingEmail", pending.getEmail());
		model.addAttribute("verificationExpiresAt", pending.getVerificationExpiresAt());
		model.addAttribute("mailConfigured", this.emailVerificationService.isMailConfigured());
		if (!model.containsAttribute("changeEmailForm")) {
			ChangeEmailForm form = new ChangeEmailForm();
			form.setEmail(pending.getEmail());
			model.addAttribute("changeEmailForm", form);
		}
		return "profile/pending-verification";
	}

	@GetMapping("/profile/change-email")
	public String changeEmailPage(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Authentication authentication,
			Model model
	) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return "redirect:/profile?lang=" + langCode;
		}
		WebLocale locale = WebLocale.fromCode(langCode);
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		if (!model.containsAttribute("changeEmailForm")) {
			ChangeEmailForm form = new ChangeEmailForm();
			form.setEmail(user.getPendingEmail() != null ? user.getPendingEmail() : user.getEmail());
			model.addAttribute("changeEmailForm", form);
		}
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Change email" : "Смена почты");
		model.addAttribute("profileUser", ProfileUserView.from(user, this.avatarService));
		model.addAttribute("pendingEmail", user.getPendingEmail());
		return "profile/change-email";
	}

	@PostMapping("/profile/change-email")
	public String changeEmail(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@Valid @ModelAttribute("changeEmailForm") ChangeEmailForm form,
			BindingResult bindingResult,
			Authentication authentication,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/profile?lang=" + langCode;
		}
		LocaleContextHolder.setLocale(Locale.forLanguageTag(WebLocale.fromCode(langCode).getCode()));
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		this.changeEmailFormValidator.validate(form, bindingResult, user);
		if (bindingResult.hasErrors()) {
			WebLocale locale = WebLocale.fromCode(langCode);
			model.addAttribute("pageTitle", locale == WebLocale.EN ? "Change email" : "Смена почты");
			model.addAttribute("profileUser", ProfileUserView.from(user, this.avatarService));
			model.addAttribute("pendingEmail", user.getPendingEmail());
			return "profile/change-email";
		}

		this.userService.changeEmail(user, form.getEmail());
		boolean sent = this.emailVerificationService.sendPendingEmailChangeEmail(user, langCode);
		this.flashPendingEmailChangeResult(user, langCode, sent, redirectAttributes);
		redirectAttributes.addFlashAttribute("emailChangePending", true);
		return "redirect:/profile?lang=" + langCode;
	}

	@GetMapping("/profile/avatar")
	public String avatarPage(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "error", required = false) String error,
			Authentication authentication,
			Model model
	) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return "redirect:/profile?lang=" + langCode;
		}
		WebLocale locale = WebLocale.fromCode(langCode);
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Change avatar" : "Смена аватарки");
		model.addAttribute("profileUser", ProfileUserView.from(user, this.avatarService));
		model.addAttribute("skinSize", SkinToAvatarService.SKIN_SIZE);
		model.addAttribute("avatarSize", SkinToAvatarService.AVATAR_SIZE);
		model.addAttribute("avatarError", error);
		return "profile/avatar";
	}

	@PostMapping("/profile/register")
	public String register(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@Valid @ModelAttribute("registerForm") RegisterForm form,
			BindingResult bindingResult,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		LocaleContextHolder.setLocale(Locale.forLanguageTag(WebLocale.fromCode(langCode).getCode()));
		this.registerFormValidator.validate(form, bindingResult);
		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", WebLocale.fromCode(langCode) == WebLocale.EN ? "Profile" : "Профиль");
			model.addAttribute("activeTab", "register");
			model.addAttribute("loginForm", new LoginForm());
			return "profile/guest";
		}

		PendingRegistration pending = this.pendingRegistrationService.startRegistration(form);
		boolean sent = this.emailVerificationService.sendRegistrationVerificationEmail(pending, langCode);
		session.setAttribute(PendingRegistrationSession.SESSION_KEY, pending.getId());
		this.flashRegistrationVerificationResult(pending, langCode, sent, redirectAttributes);
		return "redirect:/profile/pending-verification?lang=" + langCode;
	}

	@PostMapping("/profile/pending-verification/change-email")
	public String changePendingEmail(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@Valid @ModelAttribute("changeEmailForm") ChangeEmailForm form,
			BindingResult bindingResult,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		PendingRegistrationLookup lookup = this.lookupPendingRegistration(session);
		if (lookup.pending().isEmpty()) {
			if (lookup.wasExpired()) {
				return "redirect:/profile?lang=" + langCode + "&tab=register&registrationExpired=1";
			}
			return "redirect:/profile?lang=" + langCode + "&tab=register";
		}
		PendingRegistration pending = lookup.pending().get();
		LocaleContextHolder.setLocale(Locale.forLanguageTag(WebLocale.fromCode(langCode).getCode()));
		this.pendingChangeEmailFormValidator.validate(form, bindingResult, pending);
		if (bindingResult.hasErrors()) {
			this.populatePendingVerificationModel(model, pending, langCode);
			return "profile/pending-verification";
		}

		this.pendingRegistrationService.changeEmail(pending, form.getEmail());
		boolean sent = this.emailVerificationService.sendRegistrationVerificationEmail(pending, langCode);
		this.flashRegistrationVerificationResult(pending, langCode, sent, redirectAttributes);
		redirectAttributes.addFlashAttribute("emailChanged", true);
		return "redirect:/profile/pending-verification?lang=" + langCode;
	}

	@PostMapping("/profile/pending-verification/resend")
	public String resendPendingVerification(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			HttpSession session,
			RedirectAttributes redirectAttributes
	) {
		PendingRegistrationLookup lookup = this.lookupPendingRegistration(session);
		if (lookup.pending().isEmpty()) {
			if (lookup.wasExpired()) {
				return "redirect:/profile?lang=" + langCode + "&tab=register&registrationExpired=1";
			}
			return "redirect:/profile?lang=" + langCode + "&tab=register";
		}
		PendingRegistration pending = lookup.pending().get();
		this.pendingRegistrationService.issueVerificationToken(pending);
		boolean sent = this.emailVerificationService.sendRegistrationVerificationEmail(pending, langCode);
		this.flashRegistrationVerificationResult(pending, langCode, sent, redirectAttributes);
		if (sent) {
			redirectAttributes.addFlashAttribute("resent", true);
		}
		return "redirect:/profile/pending-verification?lang=" + langCode;
	}

	@PostMapping("/profile/avatar")
	public String uploadAvatar(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			@RequestParam(name = "mode") String mode,
			@RequestParam(name = "file") MultipartFile file,
			Authentication authentication,
			RedirectAttributes redirectAttributes
	) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/profile?lang=" + langCode;
		}
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		try {
			if ("skin".equals(mode)) {
				this.avatarService.uploadSkin(user, file);
			} else if ("avatar".equals(mode)) {
				this.avatarService.uploadAvatar(user, file);
			} else {
				redirectAttributes.addFlashAttribute("avatarError", "invalid-mode");
				return "redirect:/profile/avatar?lang=" + langCode;
			}
			this.avatarService.markProfileIntroSeen(user);
			redirectAttributes.addFlashAttribute("avatarSuccess", true);
			return "redirect:/profile?lang=" + langCode;
		} catch (AvatarValidationException | IOException exception) {
			redirectAttributes.addFlashAttribute("avatarErrorMessage", exception.getMessage());
			return "redirect:/profile/avatar?lang=" + langCode + "&error=1";
		}
	}

	@PostMapping("/profile/intro-seen")
	public String markIntroSeen(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Authentication authentication
	) {
		if (authentication != null && authentication.isAuthenticated()) {
			User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
			this.avatarService.markProfileIntroSeen(user);
		}
		return "redirect:/profile?lang=" + langCode;
	}

	@GetMapping("/profile/verify")
	public String verifyEmail(
			@RequestParam(name = "token") String token,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session,
			Model model
	) {
		Optional<String> usernameOptional = this.userService.verifyEmail(token);
		if (usernameOptional.isPresent()) {
			session.removeAttribute(PendingRegistrationSession.SESSION_KEY);
			this.loginHelper.login(usernameOptional.get(), request, response);
			Object rawState = session.getAttribute(ModAuthSession.SESSION_STATE_KEY);
			if (rawState instanceof String state && !state.isBlank()) {
				User user = this.userService.findByUsername(usernameOptional.get()).orElseThrow();
				this.modAuthService.complete(state, user);
				session.removeAttribute(ModAuthSession.SESSION_STATE_KEY);
				return "redirect:/auth/mod/return?state=" + state + "&lang=" + langCode;
			}
			return "redirect:/profile?lang=" + langCode;
		}
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Email verification" : "Подтверждение почты");
		model.addAttribute("verified", false);
		return "profile/verify";
	}

	private PendingRegistrationLookup lookupPendingRegistration(HttpSession session) {
		Object rawId = session.getAttribute(PendingRegistrationSession.SESSION_KEY);
		if (!(rawId instanceof Long pendingId)) {
			return PendingRegistrationLookup.notFound();
		}
		PendingRegistrationLookup lookup = this.pendingRegistrationService.lookupById(pendingId);
		if (lookup.pending().isEmpty()) {
			session.removeAttribute(PendingRegistrationSession.SESSION_KEY);
		}
		return lookup;
	}

	private void populatePendingVerificationModel(Model model, PendingRegistration pending, String langCode) {
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Confirm your email" : "Подтверждение почты");
		model.addAttribute("pendingUsername", pending.getUsername());
		model.addAttribute("pendingEmail", pending.getEmail());
		model.addAttribute("verificationExpiresAt", pending.getVerificationExpiresAt());
		model.addAttribute("mailConfigured", this.emailVerificationService.isMailConfigured());
	}

	private void flashRegistrationVerificationResult(
			PendingRegistration pending,
			String langCode,
			boolean sent,
			RedirectAttributes redirectAttributes
	) {
		if (sent) {
			return;
		}
		if (!this.emailVerificationService.isMailConfigured()) {
			redirectAttributes.addFlashAttribute(
					"verificationUrl",
					this.emailVerificationService.buildRegistrationVerificationUrl(pending, langCode)
			);
			return;
		}
		redirectAttributes.addFlashAttribute("emailSendFailed", true);
	}

	private void flashPendingEmailChangeResult(
			User user,
			String langCode,
			boolean sent,
			RedirectAttributes redirectAttributes
	) {
		if (sent) {
			return;
		}
		if (!this.emailVerificationService.isMailConfigured()) {
			redirectAttributes.addFlashAttribute(
					"verificationUrl",
					this.emailVerificationService.buildPendingEmailChangeUrl(user, langCode)
			);
			return;
		}
		redirectAttributes.addFlashAttribute("emailSendFailed", true);
	}
}
