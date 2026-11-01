package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.UserPasswordHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserPasswordHistoryRepository
        extends JpaRepository<UserPasswordHistory, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT MAX(p.changedAt)
            FROM UserPasswordHistory p
            WHERE p.user.id = :userId
            """)
    Optional<Instant> findLastChangedAtByUserId(
            @Param("userId") Long userId);

    @Transactional(readOnly = true)
    @Query("""
            SELECT p
            FROM UserPasswordHistory p
            WHERE p.user.id = :userId
            ORDER BY p.changedAt DESC
            """)
    List<UserPasswordHistory> findByUser_IdOrderByChangedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
