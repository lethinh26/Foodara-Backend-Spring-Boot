package com.db.foodara.notification.service;

import com.db.foodara.notification.entity.NotificationTemplate;
import com.db.foodara.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final GeminiService geminiService;

    /**
     * Render a notification body by code and channel.
     * Always uses Gemini AI for fun, natural messages. Falls back to hardcoded.
     */
    public String render(String code, String channel, Map<String, String> variables) {
        // Always try Gemini AI first
        String geminiBody = geminiService.generateNotification(code, variables);
        if (geminiBody != null) {
            log.info("Using Gemini-generated message for code={}", code);
            return geminiBody;
        }

        // Fallback to hardcoded
        log.warn("Gemini unavailable for code={}, channel={}. Using fallback.", code, channel);
        return buildFallbackBody(code, variables);
    }

    private String buildFallbackBody(String code, Map<String, String> variables) {
        return switch (code) {
            case "order_placed" -> {
                String store = variables.getOrDefault("storeName", "quán");
                String amount = variables.getOrDefault("totalAmount", "0");
                yield "\u0110\u01a1n h\u00e0ng c\u1ee7a b\u1ea1n t\u1ea1i " + store + " \u0111\u00e3 \u0111\u01b0\u1ee3c \u0111\u1eb7t th\u00e0nh c\u00f4ng v\u1edbi t\u1ed5ng " + amount + "\u0111.";
            }
            case "new_order_merchant" -> {
                String cust = variables.getOrDefault("customerName", "Kh\u00e1ch");
                String amount = variables.getOrDefault("totalAmount", "0");
                yield "\u0110\u01a1n m\u1edbi t\u1eeb " + cust + " - T\u1ed5ng: " + amount + "\u0111.";
            }
            case "order_status_changed" -> {
                String newStatus = variables.getOrDefault("newStatus", "?");
                yield "\u0110\u01a1n h\u00e0ng \u0111\u00e3 chuy\u1ec3n sang tr\u1ea1ng th\u00e1i: " + newStatus + ".";
            }
            case "payment_completed" -> {
                String amount = variables.getOrDefault("amount", "0");
                String method = variables.getOrDefault("paymentMethod", "?");
                yield "Thanh to\u00e1n " + amount + "\u0111 qua " + method + " th\u00e0nh c\u00f4ng.";
            }
            case "order_cancelled" -> {
                String by = variables.getOrDefault("cancelledBy", "?");
                if ("customer".equals(by)) {
                    yield "\u0110\u01a1n #" + variables.getOrDefault("orderNumber", "?") + " \u0111\u00e3 b\u1ecb kh\u00e1ch hu\u1ef7.";
                } else {
                    yield "\u0110\u01a1n #" + variables.getOrDefault("orderNumber", "?") + " \u0111\u00e3 b\u1ecb hu\u1ef7 b\u1edfi qu\u00e1n.";
                }
            }
            default -> {
                StringBuilder sb = new StringBuilder();
                if (variables != null) {
                    for (Map.Entry<String, String> e : variables.entrySet()) {
                        if (!e.getKey().equals("totalAmount") && !e.getKey().equals("amount")) {
                            sb.append(e.getValue()).append(" ");
                        }
                    }
                }
                yield !sb.isEmpty() ? sb.toString().trim() : "C\u1eadp nh\u1eadt m\u1edbi t\u1eeb Foodara.";
            }
        };
    }
}
