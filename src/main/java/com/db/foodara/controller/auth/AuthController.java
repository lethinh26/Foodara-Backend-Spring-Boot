package com.db.foodara.controller.auth;

import com.db.foodara.dto.reponse.ApiResponse;
import com.db.foodara.dto.reponse.auth.SessionResponse;
import com.db.foodara.dto.reponse.auth.TokenResponse;
import com.db.foodara.dto.request.auth.*;
import com.db.foodara.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ApiResponse<TokenResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.success("Registration successful", authService.register(request));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication,
                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = authentication.getName();
        String refreshToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            refreshToken = authHeader.substring(7);
        }
        authService.logout(userId, refreshToken);
        return ApiResponse.success("Logout successful");
    }

    // POST /api/auth/refresh-token
    @PostMapping("/refresh-token")
    public ApiResponse<TokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request));
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
    public ApiResponse<List<SessionResponse>> getSessions(Authentication authentication) {
        return ApiResponse.success(authService.getSessions(authentication.getName()));
    }

    // DELETE /api/auth/sessions/{id}
    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> deleteSession(Authentication authentication, @PathVariable String id) {
        authService.deleteSession(authentication.getName(), id);
        return ApiResponse.success("Session deleted");
    }
}
