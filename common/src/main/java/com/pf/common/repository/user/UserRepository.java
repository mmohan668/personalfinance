package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional(readOnly = true)
    User findByUsername(String username);
}
