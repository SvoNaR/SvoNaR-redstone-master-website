package ru.redstonemaster.web.profile;

import org.springframework.stereotype.Service;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Service
public class SkinToAvatarService {

	public static final int SKIN_SIZE = 64;
	public static final int AVATAR_SIZE = 8;
	private static final int FACE_X = 8;
	private static final int FACE_Y = 8;
	private static final int HAT_X = 40;
	private static final int HAT_Y = 8;

	public BufferedImage convertSkinToAvatar(BufferedImage skin) {
		int width = skin.getWidth();
		int height = skin.getHeight();
		if (width != SKIN_SIZE || height != SKIN_SIZE) {
			throw new AvatarValidationException(
					"Нужен скин Minecraft размером ровно 64×64 пикселя (загружено: " + width + "×" + height + ")."
			);
		}

		BufferedImage rgba = toRgba(skin);
		BufferedImage face = rgba.getSubimage(FACE_X, FACE_Y, AVATAR_SIZE, AVATAR_SIZE);
		BufferedImage avatar = copyImage(face);

		if (width >= HAT_X + AVATAR_SIZE && height >= HAT_Y + AVATAR_SIZE) {
			BufferedImage hat = rgba.getSubimage(HAT_X, HAT_Y, AVATAR_SIZE, AVATAR_SIZE);
			alphaComposite(avatar, hat);
		}

		return avatar;
	}

	public BufferedImage validateAvatar(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		if (width != AVATAR_SIZE || height != AVATAR_SIZE) {
			throw new AvatarValidationException(
					"Нужна аватарка размером ровно 8×8 пикселей (загружено: " + width + "×" + height + ")."
			);
		}
		return copyImage(toRgba(image));
	}

	private static BufferedImage toRgba(BufferedImage source) {
		if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
			return source;
		}
		BufferedImage converted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = converted.createGraphics();
		graphics.drawImage(source, 0, 0, null);
		graphics.dispose();
		return converted;
	}

	private static BufferedImage copyImage(BufferedImage source) {
		BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = copy.createGraphics();
		graphics.drawImage(source, 0, 0, null);
		graphics.dispose();
		return copy;
	}

	private static void alphaComposite(BufferedImage base, BufferedImage overlay) {
		Graphics2D graphics = base.createGraphics();
		graphics.setComposite(AlphaComposite.SrcOver);
		graphics.drawImage(overlay, 0, 0, null);
		graphics.dispose();
	}
}
