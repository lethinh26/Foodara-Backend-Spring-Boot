package com.db.foodara.notification.service;

import com.db.foodara.notification.entity.Notification;
import com.db.foodara.notification.dto.request.SendNotificationRequest;
import com.db.foodara.notification.dto.response.NotificationResponse;
import com.db.foodara.notification.dto.response.PageResponse;
import com.db.foodara.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatchService dispatchService;

    public PageResponse<NotificationResponse> getNotifications(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> pageResult = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        List<NotificationResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<NotificationResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(String notificationId, String userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setIsRead(true);
                n.setReadAt(java.time.LocalDateTime.now());
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void delete(String notificationId, String userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                notificationRepository.delete(n);
            }
        });
    }

    @Transactional
    public NotificationResponse createAndSend(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        dispatchService.dispatch(saved);
        return mapToResponse(saved);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
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
