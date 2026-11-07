package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            UPDATE UserPermission permission
            SET permission.hasPermission = :hasPermission,
                permission.updatedBy = :updatedBy,
                permission.updatedAt = :updatedAt
            WHERE permission.id IN :permissionIds
              AND permission.user.id = :userId
            """)
    int updatePermissions(
            @Param("permissionIds") List<Long> permissionIds,
            @Param("userId") Long userId,
            @Param("hasPermission") Boolean hasPermission,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
