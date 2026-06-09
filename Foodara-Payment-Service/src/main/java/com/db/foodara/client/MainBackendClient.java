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

    public void updatePaymentStatus(String invoiceNumber, String status) {
        String url = mainBackendUrl + "/v1/internal/orders/" + invoiceNumber + "/payment-status";
        log.info("Calling Main Backend to update payment status: {}", url);
        try {
            restTemplate.put(url, Map.of("status", status));
            log.info("Successfully updated payment status for invoice: {}", invoiceNumber);
        } catch (Exception e) {
            log.error("Failed to update payment status via internal API for invoice {}", invoiceNumber, e);
        }
    }
}
