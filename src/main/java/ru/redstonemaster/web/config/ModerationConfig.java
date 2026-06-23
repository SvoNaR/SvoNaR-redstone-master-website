package ru.redstonemaster.web.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.redstonemaster.web.moderation.ModerationProperties;

@Configuration
@EnableConfigurationProperties(ModerationProperties.class)
public class ModerationConfig {
}
