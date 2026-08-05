package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.ErrorResponseDto;
import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.dto.PromoteUserRequestDto;
import com.tanay.blogapp.dto.StatsDto;
import com.tanay.blogapp.dto.UserDetailsDto;
import com.tanay.blogapp.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin", description = "Endpoints restricted to administrators (ROLE_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminService adminService;
    private final PostService postService;
    private final TagService tagService;
    private final UserService userService;

    @Operation(summary = "Admin dashboard", description = "Simple health check to confirm admin access")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin access confirmed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<String> admin(Authentication authentication) {
        log.info("GET /admin - admin dashboard accessed by {}", authentication.getName());
        return ResponseEntity.ok("Admin access granted for " + authentication.getName());
    }

    @Operation(summary = "List all users", description = "Paginated list of all registered users with details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated user list"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/users")
    public ResponseEntity<Page<UserDetailsDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getAllUsersDetails(pageable));
    }

    @Operation(summary = "Get user details", description = "Fetch details of a specific user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailsDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserDetails(userId));
    }

    @Operation(summary = "Delete a user", description = "Admin deletes any user account. Content is reassigned to a ghost user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId, null);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a post", description = "Admin deletes any post by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("DELETE /admin/posts/{} - admin deleting post", id);
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a tag", description = "Admin deletes a tag by ID. The tag is removed from all associated posts first.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Tag not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.info("DELETE /admin/tags/{} - admin deleting tag", id);
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a comment on a post", description = "Admin deletes any comment by post and comment ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Post or comment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentOnPost(@PathVariable Long postId, @PathVariable Long commentId) {
        log.info("DELETE /admin/posts/{}/comments/{} - admin deleting comment", postId, commentId);
        postService.deleteCommentOnPost(postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Promote user to admin", description = "Grants ROLE_ADMIN to an existing user. User must not already be an admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User promoted successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "User is already an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/users/promote")
    public ResponseEntity<MessageResponseDto> promoteUserToAdmin(
            @Valid @RequestBody PromoteUserRequestDto request
    ) {
        return ResponseEntity.ok(adminService.promoteUserToAdmin(request.email()));
    }

    @Operation(summary = "Get platform statistics", description = "Returns aggregated counts for users, posts, comments, and tags")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform statistics",
                    content = @Content(schema = @Schema(implementation = StatsDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden — not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/stats")
    public ResponseEntity<StatsDto> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}