package ru.redstonemaster.web.modauth;

public record ModAuthExchangeRequest(
		String state,
		String code
) {
}
