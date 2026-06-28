package com.villo.domain.auth.repository;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.entity.type.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByNickname(String nickname);
}
