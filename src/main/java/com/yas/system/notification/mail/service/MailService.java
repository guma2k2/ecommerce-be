package com.yas.system.notification.mail.service;

import com.yas.system.notification.mail.dto.SendEmailRequest;

import java.util.Map;

public interface MailService {
    void sendEmail(SendEmailRequest request);
    void sendTemplatedEmail(String toAddress, String subject, String templateName, Map<String, Object> templateModel);
}
