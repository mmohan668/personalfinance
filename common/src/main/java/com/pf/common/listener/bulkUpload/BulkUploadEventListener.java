package com.pf.common.listener.bulkUpload;

import com.pf.common.record.bulkUpload.BulkUploadCreatedEvent;
import com.pf.common.service.bulkUpload.BulkUploadProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BulkUploadEventListener {
    private final BulkUploadProcessingService bulkUploadProcessingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BulkUploadCreatedEvent event) {
        bulkUploadProcessingService.processBulkUpload(event.bulkUploadStatusId(), event.userId());
    }
}
