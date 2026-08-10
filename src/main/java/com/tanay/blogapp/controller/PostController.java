package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.*;
import com.tanay.blogapp.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Posts", description = "Endpoints for creating, reading, updating, and interacting with posts")
public class PostController {
    private final PostService postService;

    @Operation(summary = "Create a post", description = "Creates a new post as the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody @Valid AddPostDto addPostDto, @AuthenticationPrincipal User user) {
        log.info("POST /posts - creating post by user {}: {}", user.getId(), addPostDto.title());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(addPostDto, user.getId()));
    }

    @Operation(summary = "List all posts", description = "Paginated list of posts (published to all users)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated post listing")
    })
    @GetMapping
    public ResponseEntity<Page<PostSummaryDto>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /posts - listing all posts, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @Operation(summary = "Get a post by ID", description = "Fetches a single post with full details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post found",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        log.info("GET /posts/{} - getting single post", id);
        return ResponseEntity.ok(postService.getPostById(id, user));
    }

    @Operation(summary = "Update a post", description = "Updates the title/content of a post (author or admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post updated",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the author or admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody @Valid AddPostDto addPostDto) {
        log.info("PUT /posts/{} - updating post", id);
        return ResponseEntity.ok(postService.updatePost(id, addPostDto));
    }

    @Operation(summary = "Publish a post", description = "Changes a post's status to PUBLISHED (author or admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post published",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the author or admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/{id}/publish")
    public ResponseEntity<PostDto> publishPost(@PathVariable Long id) {
        log.info("PATCH /posts/{}/publish - publishing post", id);
        return ResponseEntity.ok(postService.publishPost(id));
    }

    @Operation(summary = "Unpublish a post", description = "Changes a post's status to DRAFT (author or admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post unpublished",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the author or admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<PostDto> unpublishPost(@PathVariable Long id) {
        log.info("PATCH /posts/{}/unpublish - unpublishing post", id);
        return ResponseEntity.ok(postService.unpublishPost(id));
    }

    @Operation(summary = "Delete a post", description = "Deletes a post (author or admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the author or admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("DELETE /posts/{} - deleting post", id);
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List authenticated user's posts", description = "Paginated list of the current user's own posts")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated post listing"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<Page<PostSummaryDto>> getAllPostsForAuthenticatedUser(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /posts/me - listing posts for user {}", user.getId());
        return ResponseEntity.ok(postService.getAllPostsByUserId(user.getId(), pageable));
    }

    @Operation(summary = "Add a comment", description = "Posts a comment on a post as the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> addCommentOnPost(@PathVariable Long id, @RequestBody @Valid AddCommentDto addCommentDto, @AuthenticationPrincipal User user) {
        log.info("POST /posts/{}/comments - adding comment by user {}", id, user.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.addComment(id, addCommentDto, user.getId()));
    }

    @Operation(summary = "List comments for a post", description = "Paginated list of comments on a post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated comment listing"),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentDto>> getAllCommentsForPost(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /posts/{}/comments - listing comments, page={}", id, pageable.getPageNumber());
        return ResponseEntity.ok(postService.getAllCommentsForPost(id, pageable));
    }

    @Operation(summary = "Delete a comment", description = "Deletes a comment on a post (author or admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the comment author or admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post or comment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentOnPost(@PathVariable Long postId, @PathVariable Long commentId) {
        log.info("DELETE /posts/{}/comments/{} - deleting comment", postId, commentId);
        postService.deleteCommentOnPost(postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Like or unlike a post", description = "Toggles the like status of a post for the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post liked",
                    content = @Content(schema = @Schema(implementation = LikeToggleDto.class))),
            @ApiResponse(responseCode = "200", description = "Post unliked",
                    content = @Content(schema = @Schema(implementation = LikeToggleDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<LikeToggleDto> toggleLikeUnlike(@PathVariable Long id, @AuthenticationPrincipal User user) {
        log.info("POST /posts/{}/like - toggle like by user {}", id, user.getId());
        var response = postService.toggleLikeUnlike(id,user.getId());

        return ResponseEntity
                .status(response.liked() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "Get like count for a post", description = "Returns the total number of likes on a post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Like count",
                    content = @Content(schema = @Schema(implementation = Long.class)))
    })
    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long postId) {
        log.info("GET /posts/{}/likes/count - getting like count", postId);
        return ResponseEntity.ok(postService.getLikeCount(postId));
    }
}