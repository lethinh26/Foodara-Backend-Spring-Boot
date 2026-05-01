package com.db.foodara.service.auth;

import com.db.foodara.dto.response.auth.IpLocationResponse;
import com.db.foodara.dto.response.auth.SessionResponse;
import com.db.foodara.dto.response.auth.TokenResponse;
import com.db.foodara.dto.response.auth.RegisterCheckResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
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
    private final IpLocationService ipLocationService;
    private final UserAgentParser userAgentParser;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;


    @Transactional(readOnly = true)
    public RegisterCheckResponse checkRegister(RegisterRequest request, String targetRole) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return RegisterCheckResponse.builder()
                    .exists(false)
                    .passwordMatched(false)
                    .canLinkRole(false)
                    .targetRole(normalizeRole(targetRole))
                    .roles(List.of())
                    .build();
        }

        boolean passwordMatched = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        List<String> roles = getUserRoles(user.getId());
        String normalizedRole = normalizeRole(targetRole);
        boolean canLinkRole = passwordMatched
                && isLinkableRole(normalizedRole)
                && !roles.contains(normalizedRole)
                && roles.stream().allMatch(this::isLinkableRole);

        return RegisterCheckResponse.builder()
                .exists(true)
                .passwordMatched(passwordMatched)
                .canLinkRole(canLinkRole)
                .targetRole(normalizedRole)
                .roles(roles)
                .build();
    }

    @Transactional
    public TokenResponse linkRole(LinkRoleRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getUsername())
                .orElseGet(() -> userRepository.findByPhone(request.getUsername())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_LOGIN)));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        if ("suspended".equals(user.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        String roleName = normalizeRole(request.getRole());
        if (!isLinkableRole(roleName)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleRepository.save(userRole);
        }

        return issueTokens(user, ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXISTS);
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus("active");
        user.setAvatarUrl(request.getAvatarUrl());

        user = userRepository.save(user);

        var customerRole = roleRepository.findByName("CUSTOMER").orElse(null);
        if (customerRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(customerRole.getId());
            userRoleRepository.save(userRole);
        }

        return issueTokens(user, ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getUsername())
                .orElseGet(() -> userRepository.findByPhone(request.getUsername())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_LOGIN)));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        if ("suspended".equals(user.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        TokenResponse tokenResponse = issueTokens(user, ipAddress, userAgent);

        IpLocationResponse locationInfo = ipLocationService.getLocationByIp(ipAddress);

        log.info("User logged in: userId={}, ip={}, location={}, {}",
            user.getId(), ipAddress, locationInfo.getCity(), locationInfo.getCountry());

        return tokenResponse;
    }

    @Transactional
    public void logout(String userId, String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return;
        }
        userSessionRepository.findByTokenHashAndUserId(refreshToken, userId)
                .ifPresent(userSessionRepository::delete);
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!StringUtils.hasText(refreshToken)
                || !jwtTokenProvider.validateToken(refreshToken)
                || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        UserSession session = userSessionRepository.findByTokenHashAndUserId(refreshToken, userId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSessionRepository.delete(session);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<String> roles = getUserRoles(user.getId());
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, user.getEmail(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        session.setTokenHash(newRefreshToken);
        session.setExpiresAt(calculateRefreshTokenExpiry());
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
                        .ipAddress(session.getIpAddress())
                        .createdAt(session.getCreatedAt())
                        .current(false)
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


    private TokenResponse issueTokens(User user, String ipAddress, String userAgent) {
        List<String> roles = getUserRoles(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        createSession(user.getId(), refreshToken, ipAddress, userAgent);
        return TokenResponse.of(accessToken, refreshToken, accessTokenExpirationMs);
    }

    private String normalizeRole(String role) {
        return role == null ? "CUSTOMER" : role.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isLinkableRole(String role) {
        return "CUSTOMER".equals(role) || "MERCHANT".equals(role);
    }

    private void createSession(String userId, String refreshToken, String ipAddress, String userAgent) {
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setTokenHash(refreshToken);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setExpiresAt(calculateRefreshTokenExpiry());
        userSessionRepository.save(session);
    }

    private LocalDateTime calculateRefreshTokenExpiry() {
        long expirySeconds = Math.max(1L, refreshTokenExpirationMs / 1000L);
        return LocalDateTime.now().plusSeconds(expirySeconds);
    }

    private List<String> getUserRoles(String userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId())
                        .map(Role::getName)
                        .orElse("CUSTOMER"))
                .collect(Collectors.toList());
    }
}
