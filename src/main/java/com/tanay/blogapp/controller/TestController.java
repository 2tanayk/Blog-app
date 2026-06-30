package com.tanay.blogapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public ResponseEntity<String> test(Authentication authentication) {
        return ResponseEntity.ok("JWT is valid for " + authentication.getName());
    }


    // ==========================================
    // 1. COARSE-GRAINED SECURITY (Role Checks)
    // ==========================================

    @GetMapping("/user-dashboard")
    @PreAuthorize("hasRole('USER')") // Checks for "ROLE_USER" authority
    public String userDashboard() {
        return "SUCCESS: You reached the User Dashboard! Anyone with ROLE_USER can see this.";
    }

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')") // Checks for "ROLE_ADMIN" authority
    public String adminDashboard() {
        return "SUCCESS: Welcome Supreme Commander! Only accounts with ROLE_ADMIN can see this.";
    }

    // ==========================================
    // 2. FINE-GRAINED SECURITY (Privilege Checks)
    // ==========================================

    @GetMapping("/create-post")
    @PreAuthorize("hasAuthority('POST_CREATE')") // Looks for the exact permission string
    public String createPostPermission() {
        return "SUCCESS: Action Allowed! You possess the fine-grained 'POST_CREATE' privilege.";
    }

    @GetMapping("/delete-user")
    @PreAuthorize("hasAuthority('USER_MANAGE')") // Looks for the exact permission string
    public String deleteUserPermission() {
        return "SUCCESS: Action Allowed! You possess the dangerous 'USER_MANAGE' privilege.";
    }
}
