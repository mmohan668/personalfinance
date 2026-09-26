package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
