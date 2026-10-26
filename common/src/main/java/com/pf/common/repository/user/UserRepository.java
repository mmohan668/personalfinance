package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(U) > 0
            FROM User U
            WHERE LOWER(TRIM(U.username)) = :username
            """)
    boolean existsByUsername(@Param("username") String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE User u
            SET u.active = true, u.updatedBy = :updatedBy, u.updatedAt = :updatedAt
            WHERE u.active = false
            AND u.id IN (:ids)
            """)
    int activateUsers(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") String updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE User u
            SET u.active = false, u.updatedBy = :updatedBy, u.updatedAt = :updatedAt
            WHERE u.active = true
            AND u.id <> u.adminUser.id
            AND u.id IN (:ids)
            """)
    int inactivateUsers(
            @Param("ids") List<Long> ids,
            @Param("updatedBy") String updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
