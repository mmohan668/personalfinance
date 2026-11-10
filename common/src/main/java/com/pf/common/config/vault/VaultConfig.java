package com.pf.common.config.vault;

import com.em.pu.util.PasswordUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(VaultConfig.VaultProperties.class)
public class VaultConfig {

    @Bean
    public VaultTemplate vaultTemplate(VaultProperties properties) {

        if (properties.getUri() == null || properties.getUri().isBlank()) {
            throw new IllegalStateException(
                    "Missing configuration property: app.vault.uri"
            );
        }

        if (properties.getRoleId() == null || properties.getRoleId().isBlank()
                || properties.getSecretId() == null || properties.getSecretId().isBlank()) {
            throw new IllegalStateException(
                    "Vault AppRole environment variables are not configured"
            );
        }

        VaultEndpoint endpoint = VaultEndpoint.from(
                URI.create(PasswordUtil.decrypt(properties.getUri()))
        );

        AppRoleAuthenticationOptions options =
                AppRoleAuthenticationOptions.builder()
                        .roleId(
                                AppRoleAuthenticationOptions.RoleId.provided(PasswordUtil.decrypt(properties.getRoleId()))
                        )
                        .secretId(
                                AppRoleAuthenticationOptions.SecretId.provided(PasswordUtil.decrypt(properties.getSecretId()))
                        )
                        .build();

        RestTemplate restTemplate = VaultClients.createRestTemplate(endpoint, new SimpleClientHttpRequestFactory());
        AppRoleAuthentication authentication =
                new AppRoleAuthentication(options, restTemplate);

        return new VaultTemplate(endpoint, authentication);
    }

    @Getter
    @Setter
    @ConfigurationProperties("app.vault")
    public static class VaultProperties {
        private String uri;
        private String roleId;
        private String secretId;
    }
}
