package com.db.foodara.notification.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPlacedEvent(
        String orderId,
        String orderNumber,
        String customerId,
        String customerName,
        String customerEmail,
        String storeId,
        String storeName,
        BigDecimal totalAmount,
        LocalDateTime placedAt
) implements Serializable {}
