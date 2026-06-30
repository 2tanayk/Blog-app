package com.tanay.blogapp.security;

import com.tanay.blogapp.dto.AuthenticationResponseDto;
import com.tanay.blogapp.exception.AuthenticationProviderMismatchException;
import com.tanay.blogapp.exception.OAuthAuthenticationException;
import com.tanay.blogapp.exception.UserAlreadyExistsException;
import com.tanay.blogapp.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final AuthenticationService authenticationService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.jwt.cookie-max-age-seconds:86400}")
    private int cookieMaxAge;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId();

        try {
            AuthenticationResponseDto authenticationResponse = authenticationService.oauth2Login(oAuth2User, registrationId);

            // Set JWT as HttpOnly cookie; never touches the URL or JS.
            response.setHeader("Set-Cookie", "jwt=" + authenticationResponse.token() + "; HttpOnly" + "; Secure" + "; SameSite=Lax" + "; Path=/" + "; Max-Age=" + cookieMaxAge);

            response.sendRedirect(frontendUrl + "/dashboard");
        } catch (UserAlreadyExistsException | AuthenticationProviderMismatchException |
                 OAuthAuthenticationException exception) {
            response.sendRedirect(frontendUrl + "/login?error=" + encodeError(exception.getMessage()));
        } catch (Exception exception) {
            log.error("OAuth2 login failed unexpectedly", exception);
            response.sendRedirect(frontendUrl + "/login?error=" + encodeError("oauth_failed"));
        }
    }

    private String encodeError(String message) {
        return java.net.URLEncoder.encode(message != null ? message : "oauth_failed", java.nio.charset.StandardCharsets.UTF_8);
    }
}
