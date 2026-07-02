package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.entity.type.AuthProviderType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @EntityGraph(value = "User.withRolesAndPrivileges")
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPrivileges(@Param("email")String email);
}
