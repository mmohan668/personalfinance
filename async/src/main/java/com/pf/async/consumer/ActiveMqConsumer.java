package com.pf.async.consumer;

import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.service.export.ProcessExportService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.jms.support.converter.MessageConverter;

import java.io.IOException;

import static com.pf.common.constants.CommonConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveMqConsumer {
    private final MessageConverter messageConverter;
    private final ProcessExportService processExportService;

    @JmsListener(
            destination = SERVER_SIDE_GRID_EXPORT,
            containerFactory = "jmsListenerContainerFactory"
    )
    public void receiveMessage(Message message) throws JMSException, IOException {
        log.info("Receive message from {} ", message.getJMSMessageID());
        Object payload = messageConverter.fromMessage(message);
        if (payload instanceof SearchCriteria searchCriteria) {
            log.info("Received search criteria for server side export {} ", searchCriteria);
            processExportService.processServerSideExport(searchCriteria);
        } else {
            log.warn("Received message type: {}", message.getClass().getName());
        }
    }
}
