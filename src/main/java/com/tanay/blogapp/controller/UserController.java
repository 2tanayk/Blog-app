package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.dto.PostSummaryDto;
import com.tanay.blogapp.dto.UpdateUserPasswordDto;
import com.tanay.blogapp.dto.UpdateUserProfileDto;
import com.tanay.blogapp.dto.UserProfileDto;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.service.JwtService;
import com.tanay.blogapp.service.PostService;
import com.tanay.blogapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final PostService postService;
    private final JwtService jwtService;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getUserProfileDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfileDetails(id));
    }

    @GetMapping("{id}/posts")
    public ResponseEntity<Page<PostSummaryDto>> getAllPublishedPosts(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getPublishedPostsForUser(id, pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserProfileDetails(user.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUser(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserProfileDto request
    ) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<MessageResponseDto> updateCurrentUserPassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserPasswordDto request,
            HttpServletRequest servletRequest
    ) {
        String token = jwtService.extractToken(servletRequest);
        MessageResponseDto response = userService.updatePassword(user.getId(), request, token);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, "jwt=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Lax")
                .body(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(
            @AuthenticationPrincipal User user,
            HttpServletRequest servletRequest
    ) {
        String token = jwtService.extractToken(servletRequest);
        userService.deleteUser(user.getId(), token);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, "jwt=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Lax")
                .build();
    }
}
