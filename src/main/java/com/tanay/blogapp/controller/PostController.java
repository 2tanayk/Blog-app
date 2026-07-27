package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.*;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.repository.PostRepository;
import com.tanay.blogapp.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody @Valid AddPostDto addPostDto, @AuthenticationPrincipal User user) {
        log.info("POST /posts - creating post by user {}: {}", user.getId(), addPostDto.title());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(addPostDto, user.getId()));
    }

    @GetMapping
    public ResponseEntity<Page<PostSummaryDto>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /posts - listing all posts, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        log.info("GET /posts/{} - getting single post", id);
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody @Valid AddPostDto addPostDto) {
        log.info("PUT /posts/{} - updating post", id);
        return ResponseEntity.ok(postService.updatePost(id, addPostDto));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<PostDto> publishPost(@PathVariable Long id) {
        log.info("PATCH /posts/{}/publish - publishing post", id);
        return ResponseEntity.ok(postService.publishPost(id));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<PostDto> unpublishPost(@PathVariable Long id) {
        log.info("PATCH /posts/{}/unpublish - unpublishing post", id);
        return ResponseEntity.ok(postService.unpublishPost(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("DELETE /posts/{} - deleting post", id);
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
        log.info("GET /posts/me - listing posts for user {}", user.getId());
        return ResponseEntity.ok(postService.getAllPostsByUserId(user.getId(), pageable));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> addCommentOnPost(@PathVariable Long id, @RequestBody @Valid AddCommentDto addCommentDto, @AuthenticationPrincipal User user) {
        log.info("POST /posts/{}/comments - adding comment by user {}", id, user.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.addComment(id, addCommentDto, user.getId()));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentDto>> getAllCommentsForPost(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /posts/{}/comments - listing comments, page={}", id, pageable.getPageNumber());
        return ResponseEntity.ok(postService.getAllCommentsForPost(id, pageable));
    }

    @DeleteMapping("{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentOnPost(@PathVariable Long postId, @PathVariable Long commentId) {
        log.info("DELETE /posts/{}/comments/{} - deleting comment", postId, commentId);
        postService.deleteCommentOnPost(postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<LikeToggleDto> toggleLikeUnlike(@PathVariable Long id, @AuthenticationPrincipal User user) {
        log.info("POST /posts/{}/like - toggle like by user {}", id, user.getId());
        var response = postService.toggleLikeUnlike(id,user.getId());

        return ResponseEntity
                .status(response.liked() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long postId) {
        log.info("GET /posts/{}/likes/count - getting like count", postId);
        return ResponseEntity.ok(postService.getLikeCount(postId));
    }
}
