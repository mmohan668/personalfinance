package com.pf.common.config.redis;

import com.em.pu.util.PasswordUtil;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.credential-path}")
    private String redisCredentialPath;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RedisProperties {
        private String host;
        private String port;
        private String password;
        private String database;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(VaultTemplate vaultTemplate) {

        VaultResponse response = vaultTemplate.read(PasswordUtil.decrypt(redisCredentialPath));

        Map<String, Object> secretData = getSecretData(response);

        RedisProperties redisProperties = new ObjectMapper().convertValue(secretData, RedisProperties.class);

        RedisStandaloneConfiguration redisConfig =
                new RedisStandaloneConfiguration();

        redisConfig.setHostName(PasswordUtil.decrypt(redisProperties.getHost()));
        redisConfig.setPort(Integer.parseInt(PasswordUtil.decrypt(redisProperties.getPort())));
        redisConfig.setDatabase(Integer.parseInt(redisProperties.getDatabase()));
        redisConfig.setPassword(RedisPassword.of(PasswordUtil.decrypt(redisProperties.getPassword())));

        return new LettuceConnectionFactory(redisConfig);
    }

    private @org.jspecify.annotations.NonNull Map<String, Object> getSecretData(VaultResponse response) {
        if (response == null || response.getData() == null) {
            throw new IllegalStateException(
                    "Redis credentials not found in Vault: " + redisCredentialPath
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> secretData =
                (Map<String, Object>) response.getData().get("data");

        if (secretData == null
                || secretData.get("host") == null
                || secretData.get("port") == null
                || secretData.get("password") == null
                || secretData.get("database") == null) {

            throw new IllegalStateException(
                    "Redis details are missing from Vault");
        }
        return secretData;
    }
}
