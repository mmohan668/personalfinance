package com.pf.common.config.datasource;

import com.em.pu.util.PasswordUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.credential-path}")
    private String databaseCredentialPath;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DatabaseProperties {
        private String url;
        private String username;
        private String password;
    }

    @Bean
    @Primary
    public DataSource dataSource(VaultTemplate vaultTemplate) {
        return getHikariDataSource(vaultTemplate);
    }

    private @NonNull HikariDataSource getHikariDataSource(VaultTemplate vaultTemplate) {
        VaultResponse vaultResponse = vaultTemplate.read(PasswordUtil.decrypt(databaseCredentialPath));
        Map<String, Object> secretData = getSecretData(vaultResponse);
        DatabaseProperties databaseProperties = new ObjectMapper()
                .convertValue(secretData, DatabaseProperties.class);
        if (databaseProperties.getUrl() == null
                || databaseProperties.getUsername() == null
                || databaseProperties.getPassword() == null) {
            throw new IllegalStateException(
                    "Vault database credentials are incomplete");
        }
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(PasswordUtil.decrypt(databaseProperties.getUrl()));
        hikariDataSource.setUsername(PasswordUtil.decrypt(databaseProperties.getUsername()));
        hikariDataSource.setPassword(PasswordUtil.decrypt(databaseProperties.getPassword()));
        return hikariDataSource;
    }

    private Map<String, Object> getSecretData(VaultResponse vaultResponse) {
        if (vaultResponse == null) {
            throw new IllegalStateException(
                    "Failed to fetch vault response for provided credential path" + databaseCredentialPath
            );
        }
        Map<String, Object> responseData = vaultResponse.getData();
        if (responseData == null) {
            throw new IllegalStateException(
                    "No responseData found"
            );
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> secretData =
                (Map<String, Object>) responseData.get("data");
        return secretData;
    }
}
