package com.pf.warehouse.config;

import com.em.pu.util.PasswordUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.List;
import java.util.Map;

@Getter
@Configuration
public class GoogleCredentialConfig {

    private final String clientId;

    public GoogleCredentialConfig(
            VaultTemplate vaultTemplate,
            @Value("${app.google.credential-path}") String credentialPath) {

        VaultResponse response = vaultTemplate.read(PasswordUtil.decrypt(credentialPath));

        if (response == null || response.getData() == null) {
            throw new IllegalStateException(
                    "Failed to fetch Google credentials from Vault");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> secretData =
                (Map<String, Object>) response.getData().get("data");

        if (secretData == null || secretData.get("client-id") == null) {
            throw new IllegalStateException(
                    "Google Client ID is missing in Vault");
        }

        this.clientId = PasswordUtil.decrypt(secretData.get("client-id").toString());
    }

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {

        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(List.of(clientId))
                .build();
    }

}