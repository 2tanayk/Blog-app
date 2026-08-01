package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.AuthenticationResponseDto;
import com.tanay.blogapp.dto.LoginRequestDto;
import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.dto.RegisterRequestDto;
import com.tanay.blogapp.service.AuthenticationService;
import com.tanay.blogapp.service.JwtService;
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
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        log.info("POST /auth/register - registering user: {}", request.email());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        log.info("POST /auth/login - login attempt: {}", request.email());
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout(HttpServletRequest request) {
        String token = jwtService.extractToken(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, "jwt=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Lax")
                .body(authenticationService.logout(token));
    }
}
