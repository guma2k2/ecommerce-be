package com.yas.system.notification.handler;

import com.yas.system.notification.events.ResetPasswordEvent;
import com.yas.system.notification.events.VerifyEmailEvent;
import com.yas.system.notification.mail.service.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResetPasswordListener {
    MailService mailService;
    private static final String RESET_PASSWORD_SUBJECT = "Reset Password";

    @ApplicationModuleListener
    public void handle(ResetPasswordEvent event) {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("name", event.name());
        templateModel.put("resetUrl", event.resetUrl());
        templateModel.put("expirationMinutes", event.expirationMinutes());
        mailService.sendTemplatedEmail(event.email(), RESET_PASSWORD_SUBJECT, "/templates/forgot-password.html", templateModel);
    }
}
