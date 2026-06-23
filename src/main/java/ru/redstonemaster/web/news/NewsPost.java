package ru.redstonemaster.web.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "news_posts")
public class NewsPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String titleRu;

	@Column(nullable = false, length = 255)
	private String titleEn;

	@Column(nullable = false, length = 4096)
	private String bodyRu;

	@Column(nullable = false, length = 4096)
	private String bodyEn;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	protected NewsPost() {
	}

	public NewsPost(String titleRu, String titleEn, String bodyRu, String bodyEn, User author) {
		this.titleRu = titleRu;
		this.titleEn = titleEn;
		this.bodyRu = bodyRu;
		this.bodyEn = bodyEn;
		this.author = author;
	}

	public Long getId() {
		return this.id;
	}

	public String getTitleRu() {
		return this.titleRu;
	}

	public String getTitleEn() {
		return this.titleEn;
	}

	public String getBodyRu() {
		return this.bodyRu;
	}

	public String getBodyEn() {
		return this.bodyEn;
	}

	public User getAuthor() {
		return this.author;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void updateContent(String titleRu, String titleEn, String bodyRu, String bodyEn) {
		this.titleRu = titleRu;
		this.titleEn = titleEn;
		this.bodyRu = bodyRu;
		this.bodyEn = bodyEn;
	}
}
