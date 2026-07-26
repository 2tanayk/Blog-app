package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.PostSummaryDto;
import com.tanay.blogapp.dto.UserProfileDto;
import com.tanay.blogapp.service.PostService;
import com.tanay.blogapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final PostService postService;

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
}
