package com.pf.mailer.consumer;

import com.pf.common.dto.generic.AccountActivationRequest;
import com.pf.common.dto.generic.ForgotPasswordEmailRequest;
import com.pf.mailer.service.SendEmailService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import static com.pf.common.constants.CommonConstants.ACCOUNT_ACTIVATION_MAIL;
import static com.pf.common.constants.CommonConstants.FORGOT_PASSWORD_MAIL;

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
        log.info("Starting account activation email message processing");
        boolean completedSuccessfully = false;
        try {
            Object payload = messageConverter.fromMessage(message);
            if (payload instanceof AccountActivationRequest accountActivationRequest) {
                log.info("Received account activation email request");
                sendEmailService.sendAccountActivationEmail(accountActivationRequest);
            } else {
                log.warn("Received unsupported account activation message type: {}",
                        message.getClass().getName());
            }
            completedSuccessfully = true;
        } finally {
            log.info("Account activation email message processing {}",
                    completedSuccessfully ? "completed successfully" : "failed");
        }
    }

    @JmsListener(
            destination = FORGOT_PASSWORD_MAIL,
            containerFactory = "jmsListenerContainerFactory"
    )
    public void receiveForgotPasswordMessage(Message message)
            throws JMSException {
        log.info("Starting forgot-password email message processing");
        boolean completedSuccessfully = false;
        try {
            Object payload = messageConverter.fromMessage(message);
            if (payload instanceof ForgotPasswordEmailRequest request) {
                log.info("Received forgot-password email request");
                sendEmailService.sendForgotPasswordEmail(request);
            } else {
                log.warn("Unexpected forgot-password message type: {}",
                        payload.getClass().getName());
            }
            completedSuccessfully = true;
        } finally {
            log.info("Forgot-password email message processing {}",
                    completedSuccessfully ? "completed successfully" : "failed");
        }
    }
}
