package com.pf.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.web")
public record WebAppConfigProperties(
        String apiUrl,
        String environment,
        List<Integer> pageSizeOptions,
        int defaultPageSize,
        int maxExportRows,
        int maxSuggestionRows,
        int debounceTime
) {
}
