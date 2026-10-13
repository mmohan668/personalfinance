package com.pf.common.service.export;

import com.pf.common.dto.export.ExportRequest;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.service.activeMQ.ActiveMqProducer;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.pf.common.constants.CommonConstants.SERVER_SIDE_GRID_EXPORT;

@Service
@RequiredArgsConstructor
public class ExportService extends BaseService {
    private final ActiveMqProducer activeMqProducer;

    public ApiResponse exportServer(ExportRequest exportRequest) {
        activeMqProducer.sendMessage(SERVER_SIDE_GRID_EXPORT, exportRequest);
        return success("Export request sent to server. Please check downloads section after some time");
    }
}
