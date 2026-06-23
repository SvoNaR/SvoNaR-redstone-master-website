package ru.redstonemaster.web.profile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvatarService {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final String DEFAULT_PREFIX = "defaults/";

	private final SkinToAvatarService skinToAvatarService;
	private final UserRepository userRepository;
	private final Path userAvatarsDir;
	private final List<String> defaultAvatarFiles;

	public AvatarService(
			SkinToAvatarService skinToAvatarService,
			UserRepository userRepository,
			@Value("${app.avatars.user-dir:./data/avatars/users}") String userAvatarsDir
	) throws IOException {
		this.skinToAvatarService = skinToAvatarService;
		this.userRepository = userRepository;
		this.userAvatarsDir = Path.of(userAvatarsDir);
		Files.createDirectories(this.userAvatarsDir);
		this.defaultAvatarFiles = this.loadDefaultAvatarFiles();
	}

	public String assignRandomDefaultAvatar(User user) {
		String fileName = this.defaultAvatarFiles.get(RANDOM.nextInt(this.defaultAvatarFiles.size()));
		String avatarPath = DEFAULT_PREFIX + fileName;
		user.setAvatarPath(avatarPath);
		user.setCustomAvatar(false);
		return avatarPath;
	}

	public String getAvatarUrl(User user) {
		if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
			return "/avatars/defaults/skin1.png";
		}
		if (user.getAvatarPath().startsWith(DEFAULT_PREFIX)) {
			return "/avatars/" + user.getAvatarPath();
		}
		return "/avatars/users/" + user.getId() + ".png";
	}

	@Transactional
	public void uploadSkin(User user, MultipartFile file) throws IOException {
		User managed = this.requireUser(user.getId());
		BufferedImage skin = this.readPng(file);
		BufferedImage avatar = this.skinToAvatarService.convertSkinToAvatar(skin);
		this.saveUserAvatar(managed, avatar);
	}

	@Transactional
	public void uploadAvatar(User user, MultipartFile file) throws IOException {
		User managed = this.requireUser(user.getId());
		BufferedImage avatar = this.skinToAvatarService.validateAvatar(this.readPng(file));
		this.saveUserAvatar(managed, avatar);
	}

	@Transactional
	public void markProfileIntroSeen(User user) {
		User managed = this.requireUser(user.getId());
		managed.setProfileIntroSeen(true);
	}

	private User requireUser(long userId) {
		return this.userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	private void saveUserAvatar(User user, BufferedImage avatar) throws IOException {
		Path target = this.userAvatarsDir.resolve(user.getId() + ".png");
		ImageIO.write(avatar, "png", target.toFile());
		user.setAvatarPath("users/" + user.getId() + ".png");
		user.setCustomAvatar(true);
	}

	private BufferedImage readPng(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new AvatarValidationException("Файл не выбран.");
		}
		try (InputStream inputStream = file.getInputStream()) {
			BufferedImage image = ImageIO.read(inputStream);
			if (image == null) {
				throw new AvatarValidationException("Не удалось прочитать изображение. Загрузите PNG.");
			}
			return image;
		}
	}

	private List<String> loadDefaultAvatarFiles() throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath:/static/avatars/defaults/*.png");
		List<String> files = new ArrayList<>();
		for (Resource resource : resources) {
			String filename = resource.getFilename();
			if (filename != null) {
				files.add(filename);
			}
		}
		files.sort(String::compareToIgnoreCase);
		if (files.isEmpty()) {
			files.add("skin1.png");
		}
		return files;
	}
}
