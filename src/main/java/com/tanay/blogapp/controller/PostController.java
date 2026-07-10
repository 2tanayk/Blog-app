package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.AddPostDto;
import com.tanay.blogapp.dto.PostDto;
import com.tanay.blogapp.dto.PostSummaryDto;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody @Valid AddPostDto addPostDto, @AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(addPostDto, user.getId()));
    }

    @GetMapping
    public ResponseEntity<Page<PostSummaryDto>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody @Valid AddPostDto addPostDto) {
        return ResponseEntity.ok(postService.updatePost(id, addPostDto));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<PostDto> publishPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.publishPost(id));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<PostDto> unpublishPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.unpublishPost(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // TODO - create a UserController for this
//    @GetMapping("/{userId}/posts")
//    public ResponseEntity<List<PostDto>> getAllPostsByUserId(@PathVariable Long userId) {
//        return ResponseEntity.ok(postService.getAllPostsByUserId(userId));
//    }

    @GetMapping("/me")
    public ResponseEntity<Page<PostSummaryDto>> getAllPostsForAuthenticatedUser(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getAllPostsByUserId(user.getId(), pageable));
    }
}
