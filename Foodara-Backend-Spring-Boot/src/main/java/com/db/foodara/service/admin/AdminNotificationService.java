package com.db.foodara.service.admin;

import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminNotificationResponse;
import com.db.foodara.entity.notification.Notification;
import com.db.foodara.entity.user.User;
import com.db.foodara.repository.notification.NotificationRepository;
import com.db.foodara.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public PageResponse<AdminNotificationResponse> getNotifications(int page, int size, String type) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifPage;

        if (type != null && !type.isBlank()) {
            notifPage = notificationRepository.findByNotificationType(type, pageRequest);
        } else {
            notifPage = notificationRepository.findAll(pageRequest);
        }

        List<AdminNotificationResponse> content = notifPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AdminNotificationResponse>builder()
                .content(content)
                .page(notifPage.getNumber())
                .number(notifPage.getNumber())
                .size(notifPage.getSize())
                .totalElements(notifPage.getTotalElements())
                .totalPages(notifPage.getTotalPages())
                .last(notifPage.isLast())
                .build();
    }

    @Transactional
    public void sendNotification(Map<String, Object> request) {
        String targetType = (String) request.get("targetType");
        String title = (String) request.get("title");
        String body = (String) request.get("body");
        String imageUrl = (String) request.get("imageUrl");
        String notificationType = (String) request.get("notificationType");
        String referenceType = (String) request.get("referenceType");
        String referenceId = (String) request.get("referenceId");
        String channel = (String) request.get("channel");
        String targetValue = (String) request.get("targetValue");

        List<User> targetUsers;

        switch (targetType != null ? targetType : "all") {
            case "user" -> {
                if (targetValue != null) {
                    targetUsers = userRepository.findById(targetValue)
                            .map(List::of)
                            .orElse(List.of());
                } else {
                    targetUsers = List.of();
                }
            }
            case "role" -> {
                // Find users by role name
                if (targetValue != null) {
                    targetUsers = userRepository.findByRoleName(targetValue);
                } else {
                    targetUsers = List.of();
                }
            }
            default -> // "all"
                targetUsers = userRepository.findAll();
        }

        for (User user : targetUsers) {
            Notification notif = new Notification();
            notif.setUserId(user.getId());
            notif.setTitle(title);
            notif.setBody(body);
            notif.setImageUrl(imageUrl);
            notif.setNotificationType(notificationType != null ? notificationType : "system");
            notif.setReferenceType(referenceType);
            notif.setReferenceId(referenceId);
            notif.setChannel(channel != null ? channel : "in_app");
            notificationRepository.save(notif);
        }

        log.info("Admin sent notification '{}' to {} ({} users)", title, targetType, targetUsers.size());
    }

    private AdminNotificationResponse mapToResponse(Notification n) {
        String userName = userRepository.findById(n.getUserId())
                .map(User::getFullName)
                .orElse(null);

        return AdminNotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .userName(userName)
                .title(n.getTitle())
                .body(n.getBody())
                .imageUrl(n.getImageUrl())
                .notificationType(n.getNotificationType())
                .referenceType(n.getReferenceType())
                .referenceId(n.getReferenceId())
                .channel(n.getChannel())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .sentAt(n.getSentAt())
                .expiresAt(n.getExpiresAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
