package ru.redstonemaster.web.mail;

public interface OutboundEmailSender {

	boolean isConfigured();

	boolean send(String to, String subject, String plainTextBody);
}
