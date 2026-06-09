package com.db.foodara.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.payment-service.url:http://localhost:8083}")
    private String paymentServiceUrl;

    public String createCheckout(String orderId, String orderNumber, BigDecimal amount,
                                 String description, String successUrl, String errorUrl, String cancelUrl) {
        String url = paymentServiceUrl + "/api/v1/internal/payment/create-checkout";
        log.info("Calling Payment Service to create checkout: {}", url);

        CreateCheckoutRequest request = new CreateCheckoutRequest();
        request.setOrderId(orderId);
        request.setOrderNumber(orderNumber);
        request.setAmount(amount);
        request.setDescription(description);
        request.setSuccessUrl(successUrl);
        request.setErrorUrl(errorUrl);
        request.setCancelUrl(cancelUrl);

        try {
            Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("checkout_url")) {
                return response.get("checkout_url");
            }
        } catch (Exception e) {
            log.error("Failed to create checkout via Payment Service", e);
        }
        return null;
    }

    @Data
    public static class CreateCheckoutRequest {
        private String orderId;
        private String orderNumber;
        private BigDecimal amount;
        private String description;
        private String successUrl;
        private String errorUrl;
        private String cancelUrl;
    }
}
