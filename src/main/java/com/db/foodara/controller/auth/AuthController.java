package com.db.foodara.controller.auth;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.auth.SessionResponse;
import com.db.foodara.dto.response.auth.TokenResponse;
import com.db.foodara.dto.request.auth.*;
import com.db.foodara.service.auth.AuthService;
import com.db.foodara.service.auth.IpLocationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final IpLocationService ipLocationService;

    @Value("${app.jwt.refresh-token-expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;

    @Value("${IS_PRODUCTION:false}")
    private boolean isProduction;

    // POST /api/auth/register
    @PostMapping("/register")
    public ApiResponse<TokenResponse> register(@RequestBody @Valid RegisterRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.register(request);
        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());
        return ApiResponse.success("Registration successful",
            TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .build());
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        String remoteAddr = httpRequest.getRemoteAddr();
        String ipAddress = ipLocationService.extractIpFromRequest(xForwardedFor, remoteAddr);

        String userAgent = httpRequest.getHeader("User-Agent");

        TokenResponse tokenResponse = authService.login(request, ipAddress, userAgent);
        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());
        return ApiResponse.success("Login successful",
            TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .build());
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication,
                                    @CookieValue(value = "refreshToken", required = false) String refreshToken,
                                    HttpServletResponse response) {
        String userId = authentication.getName();
        authService.logout(userId, refreshToken);
        clearRefreshTokenCookie(response);
        return ApiResponse.success("Logout successful");
    }

    // POST /api/auth/refresh-token
    @PostMapping("/refresh-token")
    public ApiResponse<TokenResponse> refreshToken(
        @CookieValue(value = "refreshToken", required = true) String refreshToken,
        HttpServletResponse response
    ) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        TokenResponse tokenResponse = authService.refreshToken(request);

        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        return ApiResponse.success(
            TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .build());
    }

    // POST /api/auth/verify-email
    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ApiResponse.success("Email verified successfully");
    }

    // POST /api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success("Password reset email sent");
    }

    // POST /api/auth/reset-password
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password reset successful");
    }

    // GET /api/auth/sessions
    @GetMapping("/sessions")
    public ApiResponse<List<SessionResponse>> getSessions(
            Authentication authentication,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        String userId = authentication.getName();
        List<SessionResponse> sessions = authService.getSessions(userId);

        // Mark current session if refresh token matches
        if (refreshToken != null && !refreshToken.isEmpty()) {
            sessions.forEach(session -> {
                // Simple check: mark the most recent session as current
                // In production, you'd compare tokenHash
            });
            // Mark the most recent session as current
            if (!sessions.isEmpty()) {
                sessions.get(0).setCurrent(true);
            }
        }

        return ApiResponse.success(sessions);
    }

    // DELETE /api/auth/sessions/{id}
    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> deleteSession(Authentication authentication, @PathVariable String id) {
        String userId = authentication.getName();
        authService.deleteSession(userId, id);
        return ApiResponse.success("Session deleted");
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));
        cookie.setSecure(Boolean.parseBoolean(System.getenv("IS_PRODUCTION")));

        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(Boolean.parseBoolean(System.getenv("IS_PRODUCTION")));
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}