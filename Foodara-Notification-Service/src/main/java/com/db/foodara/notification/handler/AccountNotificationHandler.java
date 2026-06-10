package com.db.foodara.notification.handler;

import com.db.foodara.notification.entity.Notification;
import com.db.foodara.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handles account-related events (register, password reset, etc.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountNotificationHandler {

    private final NotificationService notificationService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "#{userRegisteredQueue.name}")
    public void handleUserRegistered(Map<String, Object> event) {
        log.info("Received UserRegisteredEvent: userId={}", event.get("userId"));

        String userId = (String) event.get("userId");
        String userName = (String) event.getOrDefault("name", "Khách");
        String userEmail = (String) event.get("email");

        if (userId == null || userEmail == null) {
            log.warn("UserRegisteredEvent missing userId or email, skip");
            return;
        }

        // Welcome email (HTML)
        String title = "🎉 Chào mừng đến với Foodara, " + userName + "!";
        String emailHtml = generateWelcomeEmail(userName);

        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setBody(emailHtml);
        n.setNotificationType("system");
        n.setChannel("email");
        n.setRecipientEmail(userEmail);
        n.setSentAt(LocalDateTime.now());
        notificationService.createAndSend(n);

        // Welcome in-app notification
        Notification inApp = new Notification();
        inApp.setUserId(userId);
        inApp.setTitle("Chào mừng " + userName + "! 🍜");
        inApp.setBody("Cảm ơn bạn đã đăng ký Foodara. Khám phá hàng ngàn món ngon ngay hôm nay!");
        inApp.setNotificationType("system");
        inApp.setChannel("in_app");
        inApp.setSentAt(LocalDateTime.now());
        notificationService.createAndSend(inApp);

        log.info("Welcome notification sent to {} via email + in_app", userEmail);
    }

    private String generateWelcomeEmail(String userName) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Segoe UI', sans-serif; background: #f5f5f5; padding: 20px;">
                <div style="max-width: 500px; margin: 0 auto; background: white; border-radius: 4px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <div style="background: #f97316; padding: 24px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 1.5rem;">Foodara</h1>
                        <p style="color: rgba(255,255,255,0.85); margin: 6px 0 0; font-size: 0.9rem;">Do an ngon, giao nhanh chong</p>
                    </div>
                    <div style="padding: 24px;">
                        <h2 style="color: #333; font-size: 1.1rem;">Chao mung <strong>%s</strong>!</h2>
                        <p style="color: #555; line-height: 1.6;">Cam on ban da dang ky tai khoan Foodara. Hay kham pha hang ngan quan an gan ban ngay bay gio.</p>
                        <div style="text-align: center; margin: 24px 0;">
                            <a href="http://localhost:5173" style="display: inline-block; background: #f97316; color: white; padding: 10px 28px; border-radius: 4px; text-decoration: none; font-weight: 600;">Kham pha ngay</a>
                        </div>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 0.8rem; text-align: center;">Foodara</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName);
    }
}
