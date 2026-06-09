package com.db.foodara.notification.event;

import java.io.Serializable;
import java.math.BigDecimal;

public record PaymentCompletedEvent(
        String orderId,
        String orderNumber,
        String customerId,
        String customerEmail,
        String customerName,
        BigDecimal amount,
        String paymentMethod
) implements Serializable {}
