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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // We need the user email, but Notification entity doesn't have it.
            // The body should already be a rendered HTML from TemplateService.
            // For now, log - actual email sending requires user email from event context.
            log.warn("[Email] Notification {} - Cannot send email yet: user email not stored in entity. "
                    + "Email dispatch requires user context from the event.",
                    notification.getId());

            // TODO: Extend Notification entity with recipientEmail or pass email from event.
            // helper.setTo(userEmail);
            // helper.setSubject(subject);
            // helper.setText(body, true);
            // mailSender.send(message);
        } catch (MessagingException e) {
            log.error("[Email] Failed to send notification {}: {}", notification.getId(), e.getMessage());
        }
    }
}
