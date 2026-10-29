package com.pf.mailer.consumer;

import com.pf.common.dto.generic.AccountActivationRequest;
import com.pf.mailer.service.SendEmailService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import static com.pf.common.constants.CommonConstants.ACCOUNT_ACTIVATION_MAIL;
@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveMqConsumer {
    private final MessageConverter messageConverter;
    private final SendEmailService sendEmailService;

    @JmsListener(
            destination = ACCOUNT_ACTIVATION_MAIL,
            containerFactory = "jmsListenerContainerFactory"
    )
    public void receiveMessage(Message message) throws JMSException {
        log.info("Receive message from {} ", message.getJMSMessageID());
        Object payload = messageConverter.fromMessage(message);
        if (payload instanceof AccountActivationRequest accountActivationRequest) {
            log.info("Received request for Account activation email {} ", accountActivationRequest);
            sendEmailService.sendAccountActivationEmail(accountActivationRequest);
        } else {
            log.warn("Received message type: {}", message.getClass().getName());
        }
    }
}
