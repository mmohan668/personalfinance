package com.pf.warehouse.config;

import com.pf.common.dto.export.ExportRequest;
import com.pf.common.dto.generic.AccountActivationRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {
    @Bean
    public JacksonJsonMessageConverter jacksonJmsMessageConverter() {
        JacksonJsonMessageConverter converter =
                new JacksonJsonMessageConverter();

        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(
                java.util.Map.of(
                        "ExportRequest", ExportRequest.class,
                        "AccountActivationRequest", AccountActivationRequest.class
                )
        );

        return converter;
    }
}
