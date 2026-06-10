package com.db.foodara.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.payment-service.url:http://localhost:8083}")
    private String paymentServiceUrl;

    /**
     * Build the SePay QR image URL via payment-service.
     * orderNumber doubles as the bank transfer code.
     */
    public String getQrUrl(String orderNumber, long amount) {
        String url = paymentServiceUrl + "/api/v1/internal/payment/qr-url"
                + "?orderNumber=" + orderNumber + "&amount=" + amount;
        try {
            @SuppressWarnings("unchecked")
            var response = restTemplate.getForObject(url, java.util.Map.class);
            if (response != null && response.containsKey("qrUrl")) {
                return (String) response.get("qrUrl");
            }
        } catch (Exception e) {
            log.error("Failed to get QR URL for order {}", orderNumber, e);
        }
        return null;
    }
}
