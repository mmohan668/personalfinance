package com.pf.mailer.service;

import com.pf.common.dto.generic.AccountActivationRequest;
import com.pf.common.dto.generic.ForgotPasswordEmailRequest;
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
    public static final String ACTIVATE_ACCOUNT = "/api/auth/activateAccount";
    public static final String TOKEN = "?token=";
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.urls.warehouse-rest-url}")
    private String activationUrl;

    public void sendAccountActivationEmail(AccountActivationRequest accountActivationRequest) {
        log.info("Starting account activation email delivery");
        boolean completedSuccessfully = false;
        try {
            if (accountActivationRequest == null
                    || accountActivationRequest.getEmailId() == null
                    || accountActivationRequest.getEmailId().isBlank()
                    || accountActivationRequest.getToken() == null
                    || accountActivationRequest.getToken().isBlank()) {
                log.warn("Account activation email request rejected: required fields are missing");
                throw new IllegalArgumentException(
                        "Email address and activation token are required");
            }

            try {
                String link = activationUrl + ACTIVATE_ACCOUNT + TOKEN
                        + java.net.URLEncoder.encode(
                        accountActivationRequest.getToken(),
                        java.nio.charset.StandardCharsets.UTF_8);

                Context context = new Context();
                context.setVariable("activationLink", link);

                log.info("Rendering account activation email template");
                String htmlContent = templateEngine.process(
                        "account-activation", context);
                log.info("Rendered account activation email template");

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(
                        message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(accountActivationRequest.getEmailId());
                helper.setSubject("Activate your ExpenseTrack account");
                helper.setText(htmlContent, true);

                log.info("Sending account activation email");
                mailSender.send(message);
                log.info("Account activation email sent successfully");
            } catch (MessagingException | MailException e) {
                log.error("Failed to send account activation email", e);
                throw new IllegalStateException(
                        "Unable to send account activation email", e);
            }
            completedSuccessfully = true;
        } finally {
            log.info("Account activation email delivery {}",
                    completedSuccessfully ? "completed successfully" : "failed");
        }
    }


    public void sendForgotPasswordEmail(ForgotPasswordEmailRequest request) {
        log.info("Starting forgot-password email delivery");
        boolean completedSuccessfully = false;
        try {
            if (request == null
                    || request.getEmailId() == null
                    || request.getEmailId().isBlank()
                    || request.getTemporaryPassword() == null
                    || request.getTemporaryPassword().isBlank()) {
                log.warn("Forgot-password email request rejected: required fields are missing");
                throw new IllegalArgumentException(
                        "Email address and temporary password are required");
            }

            try {
                Context context = new Context();
                context.setVariable(
                        "temporaryPassword",
                        request.getTemporaryPassword()
                );

                log.info("Rendering forgot-password email template");
                String htmlContent = templateEngine.process(
                        "forgot-password",
                        context
                );
                log.info("Rendered forgot-password email template");

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper =
                        new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(request.getEmailId());
                helper.setSubject("Your Temporary Password");
                helper.setText(htmlContent, true);

                log.info("Sending forgot-password email");
                mailSender.send(message);
                log.info("Forgot-password email sent successfully");
            } catch (MessagingException | MailException e) {
                log.error("Failed to send forgot-password email", e);
                throw new IllegalStateException(
                        "Unable to send forgot-password email", e);
            }
            completedSuccessfully = true;
        } finally {
            log.info("Forgot-password email delivery {}",
                    completedSuccessfully ? "completed successfully" : "failed");
        }
    }

}
