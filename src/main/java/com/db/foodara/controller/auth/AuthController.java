package com.db.foodara.controller.auth;

import com.db.foodara.dto.request.user.UserRoleRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.auth.SessionResponse;
import com.db.foodara.dto.response.auth.TokenResponse;
import com.db.foodara.dto.response.auth.RegisterCheckResponse;
import com.db.foodara.dto.request.auth.*;
import com.db.foodara.entity.user.UserRole;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.service.auth.AuthService;
import com.db.foodara.service.auth.IpLocationService;
import com.db.foodara.service.user.UserRoleService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final IpLocationService ipLocationService;
    private final UserRoleService userRoleService;

    @Value("${app.jwt.refresh-token-expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;

    @Value("${IS_PRODUCTION:false}")
    private boolean isProduction;

    // POST /api/auth/register
    @PostMapping("/register")
    public ApiResponse<TokenResponse> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        String remoteAddr = httpRequest.getRemoteAddr();
        String ipAddress = ipLocationService.extractIpFromRequest(xForwardedFor, remoteAddr);
        String userAgent = httpRequest.getHeader("User-Agent");

        TokenResponse tokenResponse = authService.register(request, ipAddress, userAgent);
        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());
        return ApiResponse.success("Registration successful",
toPublicToken(tokenResponse));
    }

    @PostMapping("/register/check")
    public ApiResponse<RegisterCheckResponse> checkRegister(
            @RequestBody @Valid RegisterRequest request,
            @RequestParam(defaultValue = "CUSTOMER") String role) {
        return ApiResponse.success(authService.checkRegister(request, role));
    }

    @PostMapping("/link-role")
    public ApiResponse<TokenResponse> linkRole(
            @RequestBody @Valid LinkRoleRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        String remoteAddr = httpRequest.getRemoteAddr();
        String ipAddress = ipLocationService.extractIpFromRequest(xForwardedFor, remoteAddr);
        String userAgent = httpRequest.getHeader("User-Agent");

        TokenResponse tokenResponse = authService.linkRole(request, ipAddress, userAgent);
        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());
        return ApiResponse.success("Role linked successfully", toPublicToken(tokenResponse));
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
toPublicToken(tokenResponse));
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
        @CookieValue(value = "refreshToken", required = false) String refreshToken,
        HttpServletResponse response
    ) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        TokenResponse tokenResponse = authService.refreshToken(request);

        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        return ApiResponse.success(
toPublicToken(tokenResponse));
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


    private TokenResponse toPublicToken(TokenResponse tokenResponse) {
        return TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));
        cookie.setSecure(isProduction);

        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(isProduction);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @PostMapping("/user-role")
    private ApiResponse<UserRole> saveUserRole(@RequestBody UserRoleRequest userRole){
        // userid name-role
        return ApiResponse.success(userRoleService.addUserRole(userRole));
    }
}
