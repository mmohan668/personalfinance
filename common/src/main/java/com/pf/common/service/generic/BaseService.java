package com.pf.common.service.generic;

import com.pf.common.constants.CommonConstants;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.entity.userManagement.User;
import com.pf.common.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseService extends GenericCriteriaService {

    @Autowired
    private UserRepository userRepository;

    User user;

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

    public User fetchLoginUser() {
        if (user == null) {
            user = userRepository.findById(CommonConstants.TEST_USER_ID).orElse(null);
        }
        if (user == null) {
            throw new RuntimeException("Error while authenticating user");
        }
        return user;
    }

}
