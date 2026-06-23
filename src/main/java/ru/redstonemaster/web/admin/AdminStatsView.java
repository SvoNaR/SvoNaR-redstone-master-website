package ru.redstonemaster.web.admin;

public record AdminStatsView(
		long totalUsers,
		long moderators,
		long administrators,
		long lessonComments,
		long lessonCompletions,
		long newsPosts
) {
}
