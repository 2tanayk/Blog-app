package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.dto.PromoteUserRequestDto;
import com.tanay.blogapp.dto.UserDetailsDto;
import com.tanay.blogapp.service.AdminService;
import jakarta.validation.Valid;
import com.tanay.blogapp.service.PostService;
import com.tanay.blogapp.service.TagService;
import com.tanay.blogapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final AdminService adminService;
    private final PostService postService;
    private final TagService tagService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<String> admin(Authentication authentication) {
        log.info("GET /admin - admin dashboard accessed by {}", authentication.getName());
        return ResponseEntity.ok("Admin access granted for " + authentication.getName());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDetailsDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getAllUsersDetails(pageable));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailsDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserDetails(userId));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("DELETE /admin/posts/{} - admin deleting post", id);
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.info("DELETE /admin/tags/{} - admin deleting tag", id);
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentOnPost(@PathVariable Long postId, @PathVariable Long commentId) {
        log.info("DELETE /admin/posts/{}/comments/{} - admin deleting comment", postId, commentId);
        postService.deleteCommentOnPost(postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/promote")
    public ResponseEntity<MessageResponseDto> promoteUserToAdmin(
            @Valid @RequestBody PromoteUserRequestDto request
    ) {
        return ResponseEntity.ok(adminService.promoteUserToAdmin(request.email()));
    }
}
