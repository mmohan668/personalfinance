package com.pf.common.service.activeMQ;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActiveMqProducer {

    private final JmsTemplate jmsTemplate;

    public void sendMessage(String destination, Object message) {
        jmsTemplate.convertAndSend(destination, message);
    }
}

