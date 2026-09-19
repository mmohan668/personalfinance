package com.pf.common.service.generic;

import com.pf.common.dto.gp.GridResult;
import com.pf.common.entity.generic.ApiResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseService extends GenericCriteriaService {

    public ApiResponse success(String message) {
        return success(message, null);
    }

    public ApiResponse failure(String message) {
        return failure(message, null);
    }

    public ApiResponse failure(String message, Object data) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    public ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public GridResult gridResult(long totalRecords, List<?> recordDetails) {
        return GridResult.builder()
                .totalRecords(totalRecords)
                .recordDetails(recordDetails)
                .build();
    }
}
