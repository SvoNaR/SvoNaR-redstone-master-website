package ru.redstonemaster.web.profile;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkinToAvatarServiceTest {

	private final SkinToAvatarService service = new SkinToAvatarService();

	@Test
	void convertsStandardSkinToAvatar() throws Exception {
		BufferedImage skin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = skin.createGraphics();
		graphics.setColor(Color.RED);
		graphics.fillRect(8, 8, 8, 8);
		graphics.setColor(new Color(0, 0, 255, 128));
		graphics.fillRect(40, 8, 8, 8);
		graphics.dispose();

		BufferedImage avatar = this.service.convertSkinToAvatar(skin);
		assertEquals(8, avatar.getWidth());
		assertEquals(8, avatar.getHeight());
	}

	@Test
	void rejectsWrongSkinSize() {
		BufferedImage skin = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		assertThrows(AvatarValidationException.class, () -> this.service.convertSkinToAvatar(skin));
	}

	@Test
	void rejectsWrongAvatarSize() {
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		assertThrows(AvatarValidationException.class, () -> this.service.validateAvatar(image));
	}

	@Test
	void acceptsExactAvatarSize() throws Exception {
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
		BufferedImage avatar = this.service.validateAvatar(image);
		assertEquals(8, avatar.getWidth());
	}
}
