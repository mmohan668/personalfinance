package com.pf.mailer.config;

import com.pf.common.dto.generic.AccountActivationRequest;
import com.pf.common.dto.generic.ForgotPasswordEmailRequest;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {
    @Bean
    public JacksonJsonMessageConverter jacksonJmsMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();

        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(java.util.Map.of("AccountActivationRequest", AccountActivationRequest.class, "ForgotPasswordEmailRequest", ForgotPasswordEmailRequest.class));

        return converter;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory, JacksonJsonMessageConverter jacksonJmsMessageConverter) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter);
        factory.setSessionTransacted(true);

        return factory;
    }

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        RedeliveryPolicy policy = new RedeliveryPolicy();

        policy.setMaximumRedeliveries(5);
        policy.setInitialRedeliveryDelay(2000);
        policy.setUseExponentialBackOff(true);
        policy.setBackOffMultiplier(2.0);

        factory.setRedeliveryPolicy(policy);

        return factory;
    }
}
