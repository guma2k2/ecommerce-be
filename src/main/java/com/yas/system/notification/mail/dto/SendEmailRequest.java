package com.yas.system.notification.mail.dto;

public record SendEmailRequest(
        String toAddress,
        String subject,
        String body,
        boolean isHtml
) {}
