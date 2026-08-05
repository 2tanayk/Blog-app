package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.ErrorResponseDto;
import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.dto.PostSummaryDto;
import com.tanay.blogapp.dto.UpdateUserPasswordDto;
import com.tanay.blogapp.dto.UpdateUserProfileDto;
import com.tanay.blogapp.dto.UserProfileDto;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.service.JwtService;
import com.tanay.blogapp.service.PostService;
import com.tanay.blogapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users", description = "Endpoints for browsing user profiles and managing the current user's account")
public class UserController {
    private final UserService userService;
    private final PostService postService;
    private final JwtService jwtService;

    @Operation(summary = "Get a user profile", description = "Fetches the public profile of a user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getUserProfileDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfileDetails(id));
    }

    @Operation(summary = "List a user's published posts", description = "Paginated list of published posts written by a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated post listing"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("{id}/posts")
    public ResponseEntity<Page<PostSummaryDto>> getAllPublishedPosts(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getPublishedPostsForUser(id, pageable));
    }

    @Operation(summary = "Get current user profile", description = "Fetches the profile of the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user profile",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserProfileDetails(user.getId()));
    }

    @Operation(summary = "Update current user profile", description = "Updates the name and/or bio of the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUser(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserProfileDto request
    ) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @Operation(summary = "Change current user password", description = "Updates the password and invalidates the current JWT, logging the user out")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Wrong current password or OAuth account",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
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

    @Operation(summary = "Delete current user account", description = "Deletes the authenticated user's account and invalidates their JWT. Content is reassigned to a ghost user.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
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