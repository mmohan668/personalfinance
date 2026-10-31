package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProfilePictureRepository
        extends JpaRepository<ProfilePicture, Long> {

    @Transactional(readOnly = true)
    Optional<ProfilePicture> findByUserId(Long userId);

    @Transactional(readOnly = true)
    Optional<ProfilePicture> findByUserUsername(String username);
}