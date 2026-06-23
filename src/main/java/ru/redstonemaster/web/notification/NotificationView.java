package ru.redstonemaster.web.notification;

import java.time.Instant;

public record NotificationView(
		Long id,
		NotificationType type,
		String title,
		String message,
		String actionUrl,
		String actionLabel,
		boolean read,
		Instant createdAt
) {
	public static NotificationView from(UserNotification notification, String langCode) {
		boolean english = "en".equals(langCode);
		return new NotificationView(
				notification.getId(),
				notification.getType(),
				english ? notification.getTitleEn() : notification.getTitleRu(),
				english ? notification.getMessageEn() : notification.getMessageRu(),
				notification.getActionPath() + "?lang=" + langCode,
				english ? notification.getActionLabelEn() : notification.getActionLabelRu(),
				notification.isRead(),
				notification.getCreatedAt()
		);
	}
}
