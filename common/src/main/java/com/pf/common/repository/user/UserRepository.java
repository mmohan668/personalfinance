package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(U) > 0
            FROM User U
            WHERE LOWER(TRIM(U.username)) = :username
            """)
    boolean existsByUsername(@Param("username") String username);

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(u) > 0
            FROM User u
            WHERE u.id = :userId
            AND u.adminUser.id = :adminUserId
            AND u.isDeleted = false
            """)
    boolean existsByIdAndAdminUserId(
            @Param("userId") Long userId,
            @Param("adminUserId") Long adminUserId
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(u) > 0
            FROM User u
            WHERE u.id = :userId
            AND u.id = u.adminUser.id
            AND u.isDeleted = false
            """)
    boolean existsAdminUserById(@Param("userId") Long userId);

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(U) > 0
            FROM User U
            WHERE LOWER(TRIM(U.email)) = :email
            """)
    boolean existsByEmail(@Param("email") String email);

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE User u
            SET u.isDeleted = true, u.deletedBy = :deletedBy, u.deletedAT = :deletedAT
            WHERE u.active = true
            AND u.id <> u.adminUser.id
            AND u.id IN (:ids)
            """)
    int deleteUsers(
            @Param("ids") List<Long> ids,
            @Param("deletedBy") String deletedBy,
            @Param("deletedAT") LocalDateTime deletedAT
    );

    @Transactional(readOnly = true)
    @Query("""
            SELECT COUNT(u) > 0
            FROM User u
            WHERE u.id = :id
            AND u.active = true
            """)
    boolean alreadyActive(@Param("id") Long id);

    @Transactional(readOnly = true)
    Optional<User> findByUsername(String username);

    @Transactional(readOnly = true)
    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(TRIM(u.username)) = LOWER(TRIM(:username))
            AND LOWER(TRIM(u.displayName)) = LOWER(TRIM(:displayName))
            AND LOWER(TRIM(u.email)) = LOWER(TRIM(:email))
            """)
    Optional<User> findByUsernameAndDisplayNameAndEmail(
            @Param("username") String username,
            @Param("displayName") String displayName,
            @Param("email") String email
    );
}
