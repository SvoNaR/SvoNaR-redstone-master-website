package ru.redstonemaster.web.news;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PublishNewsForm {

	@NotBlank(message = "{news.validation.titleRu.required}")
	@Size(max = 255, message = "{news.validation.titleRu.size}")
	private String titleRu;

	@NotBlank(message = "{news.validation.titleEn.required}")
	@Size(max = 255, message = "{news.validation.titleEn.size}")
	private String titleEn;

	@NotBlank(message = "{news.validation.bodyRu.required}")
	@Size(max = 4096, message = "{news.validation.bodyRu.size}")
	private String bodyRu;

	@NotBlank(message = "{news.validation.bodyEn.required}")
	@Size(max = 4096, message = "{news.validation.bodyEn.size}")
	private String bodyEn;

	public String getTitleRu() { return this.titleRu; }
	public void setTitleRu(String titleRu) { this.titleRu = titleRu == null ? null : titleRu.trim(); }

	public String getTitleEn() { return this.titleEn; }
	public void setTitleEn(String titleEn) { this.titleEn = titleEn == null ? null : titleEn.trim(); }

	public String getBodyRu() { return this.bodyRu; }
	public void setBodyRu(String bodyRu) { this.bodyRu = bodyRu == null ? null : bodyRu.trim(); }

	public String getBodyEn() { return this.bodyEn; }
	public void setBodyEn(String bodyEn) { this.bodyEn = bodyEn == null ? null : bodyEn.trim(); }
}
