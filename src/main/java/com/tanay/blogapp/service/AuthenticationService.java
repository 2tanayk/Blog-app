package com.tanay.blogapp.service;

import com.tanay.blogapp.dto.AuthenticationResponseDto;
import com.tanay.blogapp.dto.LoginRequestDto;
import com.tanay.blogapp.dto.RegisterRequestDto;
import com.tanay.blogapp.entity.Role;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.entity.type.AuthProviderType;
import com.tanay.blogapp.exception.AuthenticationProviderMismatchException;
import com.tanay.blogapp.exception.OAuthAuthenticationException;
import com.tanay.blogapp.exception.UserAlreadyExistsException;
import com.tanay.blogapp.repository.RoleRepository;
import com.tanay.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public AuthenticationResponseDto register(RegisterRequestDto request) {
        userRepository.findByEmail(request.email()).ifPresent(existingUser -> {
            if (isPasswordAccount(existingUser)) {
                throw new UserAlreadyExistsException("Email is already registered");
            }

            throw new UserAlreadyExistsException(
                    "This email is registered with " + existingUser.getProviderType() + " login");
        });

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found in database."));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .providerType(AuthProviderType.EMAIL)
                .build();

        user.getRoles().add(defaultRole);

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthenticationResponseDto(token);
    }

    public AuthenticationResponseDto login(LoginRequestDto request) {
        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!isPasswordAccount(user)) {
            throw new AuthenticationProviderMismatchException(
                    "This account uses " + user.getProviderType() + " login");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(user);

        return new AuthenticationResponseDto(token);
    }

    public AuthenticationResponseDto oauth2Login(OAuth2User oAuth2User, String registrationId) {
        AuthProviderType providerType = getProviderTypeFromRegistrationId(registrationId);
        String providerId = determineProviderIdFromOAuth2User(oAuth2User, registrationId);
        String email = determineEmailFromOAuth2User(oAuth2User);

        User user = userRepository.findByEmail(email)
                .map(existingUser -> handleExistingOAuthUser(existingUser, providerType, providerId))
                .orElseGet(() -> createOAuthUser(oAuth2User, email, providerId, providerType));

        String token = jwtService.generateToken(user);

        return new AuthenticationResponseDto(token);
    }

    private AuthProviderType getProviderTypeFromRegistrationId(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProviderType.GOOGLE;
            case "github" -> AuthProviderType.GITHUB;
            case "facebook" -> AuthProviderType.FACEBOOK;
            default -> throw new OAuthAuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        };
    }


    private String determineProviderIdFromOAuth2User(OAuth2User oAuth2User, String registrationId) {
        String providerId = switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "github", "facebook" -> {
                Object id = oAuth2User.getAttribute("id");
                yield id == null ? null : id.toString();
            }

            default -> {
                log.error("Unsupported OAuth2 provider: {}", registrationId);
                throw new OAuthAuthenticationException("Unsupported OAuth2 provider: " + registrationId);
            }
        };

        if (providerId == null || providerId.isBlank()) {
            log.error("Unable to determine providerId for provider: {}", registrationId);
            throw new OAuthAuthenticationException("Unable to determine providerId for OAuth2 login");
        }
        return providerId;
    }

    private User handleExistingOAuthUser(User user, AuthProviderType providerType, String providerId) {
        if (isPasswordAccount(user)) {
            throw new AuthenticationProviderMismatchException("This email is registered with password login");
        }

        if (user.getProviderType() != providerType) {
            throw new AuthenticationProviderMismatchException(
                    "This email is registered with " + user.getProviderType() + " login");
        }

        if (user.getProviderId() != null && !user.getProviderId().equals(providerId)) {
            throw new OAuthAuthenticationException("OAuth account mismatch");
        }

        if (user.getProviderId() == null) {
            user.setProviderId(providerId);
            return userRepository.save(user);
        }

        return user;
    }

    private boolean isPasswordAccount(User user) {
        return user.getProviderType() == AuthProviderType.EMAIL;
    }

    private User createOAuthUser(OAuth2User oAuth2User, String email, String providerId, AuthProviderType providerType) {
        User user = userRepository.save(User.builder()
                .name(determineNameFromOAuth2User(oAuth2User, email))
                .email(email)
                .providerId(providerId)
                .providerType(providerType)
                .build());

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Critical Error: ROLE_USER not found in database."));

        user.getRoles().add(defaultRole);

        return user;
    }

    private String determineEmailFromOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            throw new OAuthAuthenticationException(
                    "OAuth2 provider did not return an email address");
        }

        return email;
    }

    private String determineNameFromOAuth2User(OAuth2User oAuth2User, String email) {
        String name = oAuth2User.getAttribute("name");
        if (name != null && !name.isBlank()) {
            return name;
        }

        return email.substring(0, email.indexOf("@"));
    }
}
