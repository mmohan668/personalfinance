package com.pf.common.repository.user;

import com.pf.common.entity.userManagement.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {
    Optional<UserAuthProvider> findByProviderAndProviderUserId(
            String provider,
            String providerUserId
    );

    Optional<UserAuthProvider>
    findByProviderAndProviderUserIdAndUser_ActiveTrueAndUser_IsDeletedFalse(
            String provider,
            String providerUserId
    );
}
