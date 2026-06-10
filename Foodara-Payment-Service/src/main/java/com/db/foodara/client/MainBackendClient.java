package com.db.foodara.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainBackendClient {

    private final RestTemplate restTemplate;

    @Value("${app.main-backend.url}")
    private String mainBackendUrl;

    /**
     * Notify main backend that an order has been paid.
     * orderNumber doubles as the SePay transfer code.
     */
    public void updatePaymentStatus(String orderNumber, String status) {
        String url = mainBackendUrl + "/v1/internal/orders/" + orderNumber + "/payment-status";
        log.info("Calling Main Backend to update payment: {} → {}", url, status);
        try {
            restTemplate.put(url, Map.of("status", status));
            log.info("Payment updated for order {}", orderNumber);
        } catch (Exception e) {
            log.error("Failed to update payment for order {}", orderNumber, e);
        }
    }
}
