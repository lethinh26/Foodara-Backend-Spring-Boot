package com.db.foodara.notification.channel;

import com.db.foodara.notification.entity.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Email channel using Spring Mail (SMTP).
 * Sends HTML emails with MIME multipart support.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChannel implements NotificationChannel {

    private final JavaMailSender mailSender;

    @Override
    public String name() {
        return "email";
    }

    @Override
    public void send(Notification notification) {
        String recipientEmail = notification.getRecipientEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("[Email] Notification {} — no recipient email, skip", notification.getId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject(notification.getTitle());
            helper.setText(notification.getBody(), true); // true = HTML

            mailSender.send(message);
            log.info("[Email] Sent to {} — {}", recipientEmail, notification.getTitle());
        } catch (MessagingException e) {
            log.error("[Email] Failed to send to {}: {}", recipientEmail, e.getMessage());
        }
    }
}
