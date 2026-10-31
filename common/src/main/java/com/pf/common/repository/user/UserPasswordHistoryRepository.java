package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.UserPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserPasswordHistoryRepository
        extends JpaRepository<UserPasswordHistory, Long> {

    @Query("""
            SELECT MAX(p.changedAt)
            FROM UserPasswordHistory p
            WHERE p.user.id = :userId
            """)
    Optional<Instant> findLastChangedAtByUserId(
            @Param("userId") Long userId);
}
