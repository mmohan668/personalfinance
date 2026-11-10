package com.pf.mailer.config;

import com.em.pu.util.PasswordUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.credential-path}")
    private String mailCredentialPath;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class EmailProperties {
        private String host;
        private String port;
        private String username;
        private String password;
    }

    @Bean
    public JavaMailSender javaMailSender(VaultTemplate vaultTemplate) {
        EmailProperties properties = getEmailProperties(vaultTemplate);

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(PasswordUtil.decrypt(properties.getHost()));
        mailSender.setPort(Integer.parseInt(PasswordUtil.decrypt(properties.getPort())));
        mailSender.setUsername(PasswordUtil.decrypt(properties.getUsername()));
        mailSender.setPassword(PasswordUtil.decrypt(properties.getPassword()));

        Properties mailProperties = mailSender.getJavaMailProperties();
        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }

    private EmailProperties getEmailProperties(VaultTemplate vaultTemplate) {
        VaultResponse vaultResponse = vaultTemplate.read(PasswordUtil.decrypt(mailCredentialPath));

        if (vaultResponse == null || vaultResponse.getData() == null) {
            throw new IllegalStateException("Unable to read email credentials from Vault");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> secretData =
                (Map<String, Object>) vaultResponse.getData().get("data");

        if (secretData == null) {
            throw new IllegalStateException("No email credentials found in Vault");
        }

        EmailProperties properties = new ObjectMapper()
                .convertValue(secretData, EmailProperties.class);

        if (properties.getHost() == null
                || properties.getUsername() == null
                || properties.getPassword() == null
                || properties.getPort() == null) {
            throw new IllegalStateException("Vault email credentials are incomplete");
        }

        return properties;
    }
}
