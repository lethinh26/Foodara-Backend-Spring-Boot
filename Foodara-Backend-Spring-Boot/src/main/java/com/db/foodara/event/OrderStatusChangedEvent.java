package com.db.foodara.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public record OrderStatusChangedEvent(
        String orderId,
        String orderNumber,
        String customerId,
        String customerName,
        String customerEmail,
        String storeId,
        String storeName,
        String oldStatus,
        String newStatus,
        String driverId,
        String driverName,
        String driverPhone,
        LocalDateTime timestamp
) implements Serializable {}
