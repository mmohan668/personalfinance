package com.pf.mailer.service;

import com.pf.common.dto.generic.AccountActivationRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.activation-url}")
    private String activationUrl;

    public void sendAccountActivationEmail(AccountActivationRequest accountActivationRequest) {
        if (accountActivationRequest == null
                || accountActivationRequest.getEmailId() == null
                || accountActivationRequest.getEmailId().isBlank()
                || accountActivationRequest.getToken() == null
                || accountActivationRequest.getToken().isBlank()) {
            throw new IllegalArgumentException(
                    "Email address and activation token are required");
        }

        try {
            String link = activationUrl + "?token="
                    + java.net.URLEncoder.encode(
                    accountActivationRequest.getToken(),
                    java.nio.charset.StandardCharsets.UTF_8);

            Context context = new Context();
            context.setVariable("activationLink", link);

            String htmlContent = templateEngine.process(
                    "account-activation", context);

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(accountActivationRequest.getEmailId());
            helper.setSubject("Activate your ExpenseTrack account");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Account activation email sent successfully");

        } catch (MessagingException | MailException e) {
            log.error("Failed to send account activation email");
            throw new IllegalStateException(
                    "Unable to send account activation email", e);
        }
    }
}
