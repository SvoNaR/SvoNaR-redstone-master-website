package ru.redstonemaster.web.admin;

import org.springframework.data.domain.Page;

public record AdminPageView(
		int currentPage,
		int totalPages,
		long totalItems,
		String search
) {
	public static AdminPageView from(Page<?> page, String search) {
		return new AdminPageView(
				page.getTotalElements() == 0 ? 1 : page.getNumber() + 1,
				Math.max(page.getTotalPages(), 1),
				page.getTotalElements(),
				search == null ? "" : search
		);
	}

	public boolean hasPrevious() {
		return this.currentPage > 1;
	}

	public boolean hasNext() {
		return this.currentPage < this.totalPages;
	}

	public int previousPage() {
		return this.currentPage - 1;
	}

	public int nextPage() {
		return this.currentPage + 1;
	}

	public boolean hasPagination() {
		return this.totalPages > 1;
	}
}
