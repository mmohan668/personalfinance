package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.UserPermissionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserPermissionViewRepository extends JpaRepository<UserPermissionView, Long> {

    @Transactional(readOnly = true)
    List<UserPermissionView> findAllByUserIdOrderByModuleDisplayOrderAscScreenDisplayOrderAscActionDisplayOrderAsc(
            Long userId
    );
}
