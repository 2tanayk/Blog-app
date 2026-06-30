package com.tanay.blogapp.service;

import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

//    public String promoteUserToAdmin(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//
//        if (user.getRole() == Role.ADMIN) {
//            return "User is already an admin";
//        }
//
//        user.setRole(Role.ADMIN);
//        userRepository.save(user);
//
//        return "User promoted to admin";
//    }
}
