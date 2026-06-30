package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.entity.type.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProviderType providerType);
}
