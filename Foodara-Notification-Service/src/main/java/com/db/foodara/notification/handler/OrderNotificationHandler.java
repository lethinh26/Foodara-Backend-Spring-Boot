package com.db.foodara.notification.handler;

import com.db.foodara.notification.entity.Notification;
import com.db.foodara.notification.service.NotificationService;
import com.db.foodara.notification.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationHandler {

    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final com.db.foodara.notification.service.WebSocketNotificationService webSocketService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "#{orderPlacedQueue.name}")
    public void handleOrderPlaced(Map<String, Object> event) {
        log.info("Received OrderPlacedEvent: order={}", event.get("orderNumber"));

        String orderId = (String) event.get("orderId");
        String orderNumber = (String) event.get("orderNumber");
        String customerId = (String) event.get("customerId");
        String customerName = (String) event.getOrDefault("customerName", "Khách");
        String customerEmail = (String) event.get("customerEmail");
        String storeId = (String) event.get("storeId");
        String storeName = (String) event.get("storeName");
        BigDecimal totalAmount = toBigDecimal(event.get("totalAmount"));

        // 1. Notify merchant via WebSocket (store-specific topic)
        Map<String, Object> newOrderPayload = Map.of(
            "orderId", orderId != null ? orderId : "",
            "orderNumber", orderNumber != null ? orderNumber : "",
            "customerName", customerName,
            "storeName", storeName != null ? storeName : "",
            "totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO,
            "placedAt", event.getOrDefault("placedAt", LocalDateTime.now().toString()).toString()
        );
        if (storeId != null) {
            webSocketService.sendNewOrderToMerchant(storeId, newOrderPayload);
        }

        // 2. Notify customer (in-app + email)
        String customerTitle = "\u0110\u01a1n h\u00e0ng #" + orderNumber + " \u0111\u00e3 \u0111\u01b0\u1ee3c \u0111\u1eb7t th\u00e0nh c\u00f4ng";
        String customerBody = templateService.render("order_placed", "in_app",
                Map.of("orderNumber", orderNumber != null ? orderNumber : "",
                       "storeName", storeName != null ? storeName : "",
                       "totalAmount", totalAmount != null ? totalAmount.toString() : "0"));

        if (customerId != null) {
            sendToUser(customerId, customerTitle, customerBody, "order", "order", orderId, "in_app", customerEmail);
        }

        // 3. Notify merchant (in-app) — persistence so bell shows
        if (storeId != null) {
            String merchantTitle = "\u0110\u01a1n m\u1edbi #" + orderNumber + " t\u1eeb " + customerName;
            String merchantBody = templateService.render("new_order_merchant", "in_app",
                    Map.of("orderNumber", orderNumber != null ? orderNumber : "",
                           "customerName", customerName,
                           "totalAmount", totalAmount != null ? totalAmount.toString() : "0"));
            sendToStore(storeId, merchantTitle, merchantBody, "order", "order", orderId, "in_app");
        }
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "#{orderStatusQueue.name}")
    public void handleOrderStatusChanged(Map<String, Object> event) {
        log.info("Received OrderStatusChangedEvent: order={}, newStatus={}",
                event.get("orderNumber"), event.get("newStatus"));

        String orderId = (String) event.get("orderId");
        String orderNumber = (String) event.get("orderNumber");
        String customerId = (String) event.get("customerId");
        String storeId = (String) event.get("storeId");
        String storeName = (String) event.get("storeName");
        String oldStatus = (String) event.get("oldStatus");
        String newStatus = (String) event.get("newStatus");
        String driverName = (String) event.getOrDefault("driverName", "-");
        String driverPhone = (String) event.getOrDefault("driverPhone", "-");

        String title = "\u0110\u01a1n h\u00e0ng #" + orderNumber + " - " + translateStatus(newStatus);
        String body = templateService.render("order_status_changed", "in_app",
                Map.of("orderNumber", orderNumber != null ? orderNumber : "",
                       "storeName", storeName != null ? storeName : "",
                       "oldStatus", translateStatus(oldStatus),
                       "newStatus", translateStatus(newStatus),
                       "driverName", driverName,
                       "driverPhone", driverPhone));

        if (customerId != null) {
            // DELIVERED: send special email
            if ("delivered".equalsIgnoreCase(newStatus) && event.get("customerEmail") != null) {
                String deliveredTitle = "🎉 Đơn hàng #" + orderNumber + " đã giao thành công!";
                String emailHtml = generateDeliveredEmail(
                        orderNumber, storeName,
                        (String) event.getOrDefault("customerName", "Khách"),
                        (String) event.get("customerEmail"));
                sendToUser(customerId, deliveredTitle, emailHtml, "order_status", "order", orderId, "email", (String) event.get("customerEmail"));
                // Also send in-app notification
                sendToUser(customerId, title, body, "order_status", "order", orderId, "in_app");
            } else {
                sendToUser(customerId, title, body, "order_status", "order", orderId, "in_app");
            }
        }

        // Also notify merchant of status change
        if (storeId != null) {
            String merchantTitle = "\u0110\u01a1n #" + orderNumber + " \u0111\u00e3 chuy\u1ec3n sang \"" + translateStatus(newStatus) + "\"";
            sendToStore(storeId, merchantTitle, body, "order_status", "order", orderId, "in_app");
        }
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "#{paymentCompletedQueue.name}")
    public void handlePaymentCompleted(Map<String, Object> event) {
        log.info("Received PaymentCompletedEvent: order={}", event.get("orderNumber"));

        String orderId = (String) event.get("orderId");
        String orderNumber = (String) event.get("orderNumber");
        String customerId = (String) event.get("customerId");
        String storeId = (String) event.get("storeId");
        BigDecimal amount = toBigDecimal(event.get("amount"));
        String paymentMethod = (String) event.getOrDefault("paymentMethod", "QR");

        String title = "Thanh to\u00e1n \u0111\u01a1n #" + orderNumber + " th\u00e0nh c\u00f4ng";
        String body = templateService.render("payment_completed", "in_app",
                Map.of("orderNumber", orderNumber != null ? orderNumber : "",
                       "amount", amount != null ? amount.toString() : "0",
                       "paymentMethod", paymentMethod));

        if (customerId != null) {
            sendToUser(customerId, title, body, "payment", "order", orderId, "in_app");
        }
        if (storeId != null) {
            sendToStore(storeId, title, body, "payment", "order", orderId, "in_app");
        }
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "#{orderCancelledQueue.name}")
    public void handleOrderCancelled(Map<String, Object> event) {
        log.info("Received OrderCancelledEvent: order={}, cancelledBy={}",
                event.get("orderNumber"), event.get("cancelledBy"));

        String orderId = (String) event.get("orderId");
        String orderNumber = (String) event.get("orderNumber");
        String customerId = (String) event.get("customerId");
        String storeId = (String) event.get("storeId");
        String storeName = (String) event.get("storeName");
        String cancelledBy = (String) event.getOrDefault("cancelledBy", "unknown");

        // Customer cancelled → notify merchant only (customer already knows)
        if ("customer".equals(cancelledBy)) {
            if (storeId != null) {
                String title = "\u0110\u01a1n #" + orderNumber + " \u0111\u00e3 b\u1ecb kh\u00e1ch hu\u1ef7";
                String body = templateService.render("order_cancelled", "in_app",
                        Map.of("orderNumber", orderNumber != null ? orderNumber : "",
                               "customerName", (String) event.getOrDefault("customerName", "Kh\u00e1ch"),
                               "cancelledBy", cancelledBy));
                sendToStore(storeId, title, body, "order", "order", orderId, "in_app");
            }
            return;
        }

        // Merchant cancelled → notify customer only
        if (customerId != null) {
            String title = "\u0110\u01a1n h\u00e0ng #" + orderNumber + " \u0111\u00e3 b\u1ecb hu\u1ef7 b\u1edfi qu\u00e1n";
            String body = templateService.render("order_cancelled", "in_app",
                    Map.of("orderNumber", orderNumber != null ? orderNumber : "",
                           "storeName", storeName != null ? storeName : "",
                           "cancelledBy", cancelledBy));
            sendToUser(customerId, title, body, "order", "order", orderId, "in_app");
        }
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void sendToUser(String userId, String title, String body, String type, String refType, String refId, String channel) {
        sendToUser(userId, title, body, type, refType, refId, channel, null);
    }

    private void sendToUser(String userId, String title, String body, String type, String refType, String refId, String channel, String recipientEmail) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setBody(body);
        n.setNotificationType(type);
        n.setReferenceType(refType);
        n.setReferenceId(refId);
        n.setChannel(channel);
        n.setRecipientEmail(recipientEmail);
        n.setSentAt(LocalDateTime.now());
        notificationService.createAndSend(n);
    }

    private void sendToStore(String storeId, String title, String body, String type, String refType, String refId, String channel) {
        Notification n = new Notification();
        n.setUserId(storeId);
        n.setTitle(title);
        n.setBody(body);
        n.setNotificationType(type);
        n.setReferenceType(refType);
        n.setReferenceId(refId);
        n.setChannel(channel);
        n.setSentAt(LocalDateTime.now());
        notificationService.createAndSend(n);
    }

    private String generateDeliveredEmail(String orderNumber, String storeName, String customerName, String customerEmail) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Segoe UI', sans-serif; background: #f5f5f5; padding: 20px;">
                <div style="max-width: 500px; margin: 0 auto; background: white; border-radius: 4px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <div style="background: #f97316; padding: 24px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 1.3rem;">Don hang da giao thanh cong</h1>
                    </div>
                    <div style="padding: 24px;">
                        <p style="font-size: 1rem; color: #333;">Xin chao <strong>%s</strong>,</p>
                        <p style="color: #555;">Don hang <strong>#%s</strong> tu <strong>%s</strong> da duoc giao den ban.</p>
                        <p style="color: #555;">Cam on ban da dat hang tai Foodara!</p>
                        <div style="background: #fff7ed; border-radius: 4px; padding: 14px; margin: 20px 0; text-align: center;">
                            <p style="margin: 0; color: #f97316; font-weight: 600;">Danh gia mon an de nhan uu dai!</p>
                            <a href="http://localhost:5173/orders/%s" style="display: inline-block; margin-top: 10px; background: #f97316; color: white; padding: 8px 20px; border-radius: 4px; text-decoration: none; font-weight: 600;">Danh gia ngay</a>
                        </div>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 0.8rem; text-align: center;">Foodara</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(customerName, orderNumber, storeName != null ? storeName : "quan", orderNumber);
    }

    private String translateStatus(String status) {
        return switch (status != null ? status.toLowerCase() : "") {
            case "pending" -> "Ch\u1edd x\u00e1c nh\u1eadn";
            case "confirmed" -> "\u0110\u00e3 x\u00e1c nh\u1eadn";
            case "ready_for_pickup" -> "S\u1eb5n s\u00e0ng l\u1ea5y h\u00e0ng";
            case "driver_assigned" -> "T\u00e0i x\u1ebf \u0111\u00e3 nh\u1eadn";
            case "driver_at_store" -> "T\u00e0i x\u1ebf \u0111\u1ebfn qu\u00e1n";
            case "picked_up" -> "\u0110\u00e3 l\u1ea5y h\u00e0ng";
            case "delivering" -> "\u0110ang giao";
            case "delivered" -> "\u0110\u00e3 giao";
            case "cancelled" -> "\u0110\u00e3 hu\u1ef7";
            case "failed" -> "Th\u1ea5t b\u1ea1i";
            default -> status;
        };
    }
}

