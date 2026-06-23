package ru.redstonemaster.web.moderation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.redstonemaster.web.user.User;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModLessonJarServiceTest {

	@TempDir
	Path tempDir;

	@Test
	void buildsJarWithFabricModJsonAndAssets() throws Exception {
		ModerationProperties properties = new ModerationProperties();
		properties.setJarOutputDir(this.tempDir.resolve("jars").toString());

		Path workspace = this.tempDir.resolve("workspace");
		Path pack = workspace.resolve("assets/redstone-master/tutorial/packs/demo_ru_ru.json");
		Files.createDirectories(pack.getParent());
		Files.writeString(pack, "{\"sections\":[]}");

		Path videoFrame = workspace.resolve("assets/redstone-master/tutorials/videos/demo/frame_00000.png");
		Files.createDirectories(videoFrame.getParent());
		Files.write(videoFrame, new byte[] {1, 2, 3});

		User moderator = mock(User.class);
		when(moderator.getId()).thenReturn(1L);
		LessonSubmission submission = new LessonSubmission(
				moderator,
				"redstone_signal",
				"demo_lesson",
				"demo",
				"Заголовок",
				"Title",
				"Текст",
				"Body",
				workspace.toString()
		);
		var idField = LessonSubmission.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(submission, 1L);

		ModLessonJarService service = new ModLessonJarService(properties);
		Path jar = service.buildLessonJar(submission);

		assertTrue(Files.exists(jar));
		assertTrue(Files.size(jar) > 100);
	}
}
