package com.db.foodara.notification.controller;

import com.db.foodara.notification.dto.request.CreateTemplateRequest;
import com.db.foodara.notification.dto.response.NotificationResponse;
import com.db.foodara.notification.dto.response.PageResponse;
import com.db.foodara.notification.entity.NotificationTemplate;
import com.db.foodara.notification.repository.NotificationRepository;
import com.db.foodara.notification.repository.NotificationTemplateRepository;
import com.db.foodara.notification.service.NotificationService;
import com.db.foodara.notification.entity.Notification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final NotificationTemplateRepository templateRepository;

    // ---------- Notifications ----------

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifPage = type != null && !type.isBlank()
                ? notificationRepository.findByNotificationType(type, pageRequest)
                : notificationRepository.findAll(pageRequest);

        List<NotificationResponse> content = notifPage.getContent().stream()
                .map(n -> NotificationResponse.builder()
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
                        .build())
                .toList();

        return ResponseEntity.ok(PageResponse.<NotificationResponse>builder()
                .content(content)
                .page(notifPage.getNumber())
                .size(notifPage.getSize())
                .totalElements(notifPage.getTotalElements())
                .totalPages(notifPage.getTotalPages())
                .last(notifPage.isLast())
                .build());
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendToUsers(@RequestBody Map<String, Object> request) {
        String title = (String) request.get("title");
        String body = (String) request.get("body");
        String imageUrl = (String) request.get("imageUrl");
        String notificationType = (String) request.get("notificationType");
        String channel = (String) request.get("channel");
        String userId = (String) request.get("userId");

        if (userId != null) {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle(title);
            n.setBody(body);
            n.setImageUrl(imageUrl);
            n.setNotificationType(notificationType != null ? notificationType : "system");
            n.setChannel(channel != null ? channel : "in_app");
            n.setSentAt(LocalDateTime.now());
            notificationService.createAndSend(n);
        }

        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    // ---------- Templates ----------

    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplate>> getTemplates() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    @PostMapping("/templates")
    public ResponseEntity<NotificationTemplate> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        NotificationTemplate template = new NotificationTemplate();
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setChannel(request.getChannel());
        template.setSubject(request.getSubject());
        template.setBodyTemplate(request.getBodyTemplate());
        template.setVariables(request.getVariables());
        template.setIsActive(true);
        return ResponseEntity.ok(templateRepository.save(template));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @PathVariable String id, @Valid @RequestBody CreateTemplateRequest request) {
        return templateRepository.findById(id).map(t -> {
            t.setCode(request.getCode());
            t.setName(request.getName());
            t.setChannel(request.getChannel());
            t.setSubject(request.getSubject());
            t.setBodyTemplate(request.getBodyTemplate());
            t.setVariables(request.getVariables());
            return ResponseEntity.ok(templateRepository.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Map<String, String>> deleteTemplate(@PathVariable String id) {
        templateRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
    /**
     * Push promo/marketing notification to specific user or broadcast.
     * Supports email channel with recipientEmail.
     */
    @PostMapping("/promo")
    public ResponseEntity<Map<String, Object>> sendPromo(@RequestBody Map<String, Object> request) {
        String title = (String) request.get("title");
        String body = (String) request.get("body");
        String channel = (String) request.getOrDefault("channel", "in_app,email");
        String recipientEmail = (String) request.get("recipientEmail");
        String userId = (String) request.get("userId");

        if (title == null || body == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "title and body required"));
        }

        Notification n = new Notification();
        n.setUserId(userId != null ? userId : "broadcast");
        n.setTitle(title);
        n.setBody(body);
        n.setNotificationType("promotion");
        n.setChannel(channel);
        n.setRecipientEmail(recipientEmail);
        n.setSentAt(LocalDateTime.now());
        notificationService.createAndSend(n);

        return ResponseEntity.ok(Map.of(
                "status", "sent",
                "userId", userId != null ? userId : "broadcast",
                "channel", channel
        ));
    }
}
