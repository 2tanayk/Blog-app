package com.tanay.blogapp.service;

import com.tanay.blogapp.entity.Role;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.exception.BadRequestException;
import com.tanay.blogapp.exception.ResourceNotFoundException;
import com.tanay.blogapp.repository.RoleRepository;
import com.tanay.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public MessageResponseDto promoteUserToAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found in database"));

        if (user.getRoles().contains(adminRole)) {
            throw new BadRequestException("User is already an admin");
        }

        user.getRoles().add(adminRole);

        return new MessageResponseDto("User promoted to admin");
    }
}
