package com.db.foodara.service.auth;

import com.db.foodara.dto.reponse.auth.SessionResponse;
import com.db.foodara.dto.reponse.auth.TokenResponse;
import com.db.foodara.dto.request.auth.*;
import com.db.foodara.entity.role.Role;
import com.db.foodara.entity.user.User;
import com.db.foodara.entity.user.UserRole;
import com.db.foodara.entity.user.UserSession;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.role.RoleRepository;
import com.db.foodara.repository.user.UserRepository;
import com.db.foodara.repository.user.UserRoleRepository;
import com.db.foodara.repository.user.UserSessionRepository;
import com.db.foodara.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXISTS);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus("active");
        user = userRepository.save(user);

        var customerRole = roleRepository.findByName("CUSTOMER").orElse(null);
        if (customerRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(customerRole.getId());
            userRoleRepository.save(userRole);
        }

        List<String> roles = List.of("CUSTOMER");
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return TokenResponse.of(accessToken, refreshToken, accessTokenExpirationMs);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getUsername())
                .or(() -> userRepository.findByPhone(request.getUsername()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        if ("suspended".equals(user.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        List<String> roles = getUserRoles(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // ss
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setTokenHash(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(30));
        userSessionRepository.save(session);

        return TokenResponse.of(accessToken, refreshToken, accessTokenExpirationMs);
    }

    @Transactional
    public void logout(String userId, String refreshToken) {
        userSessionRepository.findByTokenHash(refreshToken)
                .ifPresent(userSessionRepository::delete);
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        UserSession session = userSessionRepository.findByTokenHash(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSessionRepository.delete(session);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<String> roles = getUserRoles(userId);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, user.getEmail(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        session.setTokenHash(newRefreshToken);
        userSessionRepository.save(session);

        return TokenResponse.of(newAccessToken, newRefreshToken, accessTokenExpirationMs);
    }

    public void verifyEmail(VerifyEmailRequest request) {
        throw new AppException(ErrorCode.INVALID_TOKEN);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        throw new AppException(ErrorCode.INVALID_TOKEN);
    }

    public List<SessionResponse> getSessions(String userId) {
        return userSessionRepository.findByUserId(userId).stream()
                .map(session -> SessionResponse.builder()
                        .id(session.getId())
                        .deviceName(session.getDeviceId())
                        .ipAddress(session.getIpAddress())
                        .lastActiveAt(session.getCreatedAt())
                        .createdAt(session.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSession(String userId, String sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
        if (!session.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        userSessionRepository.delete(session);
    }

    private List<String> getUserRoles(String userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId())
                        .map(Role::getName)
                        .orElse("CUSTOMER"))
                .collect(Collectors.toList());
    }
}
