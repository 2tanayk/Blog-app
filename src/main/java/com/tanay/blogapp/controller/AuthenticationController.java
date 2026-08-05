package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.AuthenticationResponseDto;
import com.tanay.blogapp.dto.ErrorResponseDto;
import com.tanay.blogapp.dto.LoginRequestDto;
import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.dto.RegisterRequestDto;
import com.tanay.blogapp.service.AuthenticationService;
import com.tanay.blogapp.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Public endpoints for registering, logging in, and logging out")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    @Operation(summary = "Register a new user", description = "Creates an account with email + password and returns a JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered and JWT issued",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        log.info("POST /auth/register - registering user: {}", request.email());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @Operation(summary = "Log in", description = "Authenticates with email + password and returns a JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, JWT returned",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account uses a different provider",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        log.info("POST /auth/login - login attempt: {}", request.email());
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @Operation(summary = "Log out", description = "Blacklists the current token and clears the JWT cookie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully logged out",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout(HttpServletRequest request) {
        String token = jwtService.extractToken(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, "jwt=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Lax")
                .body(authenticationService.logout(token));
    }
}