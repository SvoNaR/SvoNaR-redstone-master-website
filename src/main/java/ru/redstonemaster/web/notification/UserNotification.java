package ru.redstonemaster.web.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ru.redstonemaster.web.user.User;

import java.time.Instant;

@Entity
@Table(name = "user_notifications")
public class UserNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private NotificationType type;

	@Column(nullable = false, length = 64)
	private String sourceKey;

	@Column(nullable = false, length = 255)
	private String titleRu;

	@Column(nullable = false, length = 255)
	private String titleEn;

	@Column(nullable = false, length = 1024)
	private String messageRu;

	@Column(nullable = false, length = 1024)
	private String messageEn;

	@Column(nullable = false, length = 255)
	private String actionPath;

	@Column(nullable = false, length = 64)
	private String actionLabelRu;

	@Column(nullable = false, length = 64)
	private String actionLabelEn;

	@Column(nullable = false)
	private boolean read = false;

	private Instant readAt;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	protected UserNotification() {
	}

	public UserNotification(
			User user,
			NotificationType type,
			String sourceKey,
			String titleRu,
			String titleEn,
			String messageRu,
			String messageEn,
			String actionPath,
			String actionLabelRu,
			String actionLabelEn
	) {
		this.user = user;
		this.type = type;
		this.sourceKey = sourceKey;
		this.titleRu = titleRu;
		this.titleEn = titleEn;
		this.messageRu = messageRu;
		this.messageEn = messageEn;
		this.actionPath = actionPath;
		this.actionLabelRu = actionLabelRu;
		this.actionLabelEn = actionLabelEn;
	}

	public Long getId() {
		return this.id;
	}

	public User getUser() {
		return this.user;
	}

	public NotificationType getType() {
		return this.type;
	}

	public String getSourceKey() {
		return this.sourceKey;
	}

	public String getTitleRu() {
		return this.titleRu;
	}

	public String getTitleEn() {
		return this.titleEn;
	}

	public String getMessageRu() {
		return this.messageRu;
	}

	public String getMessageEn() {
		return this.messageEn;
	}

	public String getActionPath() {
		return this.actionPath;
	}

	public String getActionLabelRu() {
		return this.actionLabelRu;
	}

	public String getActionLabelEn() {
		return this.actionLabelEn;
	}

	public boolean isRead() {
		return this.read;
	}

	public Instant getReadAt() {
		return this.readAt;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void markRead() {
		this.read = true;
		this.readAt = Instant.now();
	}

	public void markUnread() {
		this.read = false;
		this.readAt = null;
	}
}
