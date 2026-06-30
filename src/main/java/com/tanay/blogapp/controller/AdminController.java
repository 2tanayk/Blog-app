package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.PromoteUserRequestDto;
import com.tanay.blogapp.service.AdminService;
import com.tanay.blogapp.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final PostService postService;

    @GetMapping
    public ResponseEntity<String> admin(Authentication authentication) {
        return ResponseEntity.ok("Admin access granted for " + authentication.getName());
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

//    @PatchMapping("/users/promote")
//    public ResponseEntity<String> promoteUserToAdmin(
//            @Valid @RequestBody PromoteUserRequestDto request
//    ) {
//        return ResponseEntity.ok(adminService.promoteUserToAdmin(request.email()));
//    }
}
