package com.tanay.blogapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @GetMapping
    public ResponseEntity<String> test(Authentication authentication) {
        log.info("GET /test - basic auth check for {}", authentication.getName());
        return ResponseEntity.ok("JWT is valid for " + authentication.getName());
    }


    // ==========================================
    // 1. COARSE-GRAINED SECURITY (Role Checks)
    // ==========================================

    @GetMapping("/user-dashboard")
    @PreAuthorize("hasRole('USER')") // Checks for "ROLE_USER" authority
    public String userDashboard() {
        log.info("GET /test/user-dashboard - ROLE_USER access");
        return "SUCCESS: You reached the User Dashboard! Anyone with ROLE_USER can see this.";
    }

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')") // Checks for "ROLE_ADMIN" authority
    public String adminDashboard() {
        log.info("GET /test/admin-dashboard - ROLE_ADMIN access");
        return "SUCCESS: Welcome Supreme Commander! Only accounts with ROLE_ADMIN can see this.";
    }

    // ==========================================
    // 2. FINE-GRAINED SECURITY (Privilege Checks)
    // ==========================================

    @GetMapping("/create-post")
    @PreAuthorize("hasAuthority('POST_CREATE')") // Looks for the exact permission string
    public String createPostPermission() {
        log.info("GET /test/create-post - POST_CREATE privilege check");
        return "SUCCESS: Action Allowed! You possess the fine-grained 'POST_CREATE' privilege.";
    }

    @GetMapping("/delete-user")
    @PreAuthorize("hasAuthority('USER_MANAGE')") // Looks for the exact permission string
    public String deleteUserPermission() {
        log.info("GET /test/delete-user - USER_MANAGE privilege check");
        return "SUCCESS: Action Allowed! You possess the dangerous 'USER_MANAGE' privilege.";
    }
}
