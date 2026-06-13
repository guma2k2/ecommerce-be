package com.yas.system.notification.handler;

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
public class VerifyEmailListener {

    MailService mailService;
    private static final String VERIFY_EMAIL_SUBJECT = "Verify Email";

    @ApplicationModuleListener
    public void handle(VerifyEmailEvent event) {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("name", event.name());
        templateModel.put("code", event.code());
        templateModel.put("expirationMinutes", event.expirationMinutes());
        mailService.sendTemplatedEmail(event.email(), VERIFY_EMAIL_SUBJECT, "/templates/verify-code.html", templateModel);
    }
}
