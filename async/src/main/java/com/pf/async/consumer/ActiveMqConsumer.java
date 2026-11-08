package com.pf.async.consumer;

import com.pf.common.dto.export.ExportRequest;
import com.pf.common.service.export.ProcessExportService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.pf.common.constants.CommonConstants.SERVER_SIDE_GRID_EXPORT;

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
        log.info("Starting server-side export message processing");
        boolean completedSuccessfully = false;
        try {
            Object payload = messageConverter.fromMessage(message);
            if (payload instanceof ExportRequest exportRequest) {
                log.info("Received server-side export request");
                processExportService.processServerSideExport(exportRequest);
            } else {
                log.warn("Received unsupported server-side export message type: {}",
                        message.getClass().getName());
            }
            completedSuccessfully = true;
        } finally {
            log.info("Server-side export message processing {}",
                    completedSuccessfully ? "completed successfully" : "failed");
        }
    }
}
