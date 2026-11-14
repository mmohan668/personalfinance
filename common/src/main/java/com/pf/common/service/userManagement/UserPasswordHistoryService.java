package com.pf.common.service.userManagement;

import com.pf.common.entity.userManagement.User;
import com.pf.common.entity.userManagement.UserPasswordHistory;
import com.pf.common.repository.user.UserPasswordHistoryRepository;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserPasswordHistoryService extends BaseService {
    private final UserPasswordHistoryRepository userPasswordHistoryRepository;

    @Transactional
    public void updatePasswordHistory(User user, UserPasswordHistory.PasswordActionType passwordActionType) {
        UserPasswordHistory userPasswordHistory = new UserPasswordHistory();
        userPasswordHistory.setUser(user);
        userPasswordHistory.setPasswordHash(user.getPasswordHash());
        userPasswordHistory.setActionType(passwordActionType);
        userPasswordHistory.setChangedAt(Instant.now());
        userPasswordHistory.setChangedBy(fetchLoginUsername());
        userPasswordHistoryRepository.save(userPasswordHistory);
    }
}
