package com.yas.system.common.mail.dto;

public record SendEmailRequest(String toAddress,
                               String subject,
                               String body,
                               boolean isHtml
) {
}
