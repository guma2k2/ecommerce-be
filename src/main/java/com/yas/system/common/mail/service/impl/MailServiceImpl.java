package com.yas.system.common.mail.service.impl;

import com.yas.system.common.mail.dto.SendEmailRequest;
import com.yas.system.common.mail.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {
    JavaMailSender mailSender;
    TemplateEngine templateEngine;


    @Value("${spring.mail.host}")
    private String fromAddress;

    @Override
    @Async
    public void sendEmail(SendEmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, "Me");
            helper.setTo(request.toAddress());
            helper.setSubject(request.subject());
            helper.setText(request.body(), request.isHtml());

            mailSender.send(message);
            log.info(">>> Email sent successfully to: {}", request.toAddress());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error(">>> Failed to send email to {}. Error: {}", request.toAddress(), e.getMessage(), e);
        }
    }

    @Override
    public void sendTemplatedEmail(String toAddress, String subject, String templateName, Map<String, Object> templateModel) {
        // 1. Load data into Thymeleaf Context
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);

        // 2. Process HTML
        String htmlBody = templateEngine.process(templateName, thymeleafContext);

        log.info(">>> Email body: {}", htmlBody);

        // 3. Fire email
        SendEmailRequest request = new SendEmailRequest(toAddress, subject, htmlBody, true);
        sendEmail(request);
    }
}
