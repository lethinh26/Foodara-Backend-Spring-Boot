package com.db.foodara.controller.internal;

import com.db.foodara.service.payment.SepayService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/internal/payment")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final SepayService sepayService;

    @PostMapping("/create-checkout")
    public ResponseEntity<Map<String, String>> createCheckout(@RequestBody CreateCheckoutRequest request) {
        String checkoutUrl = sepayService.createCheckout(
                request.getOrderId(),
                request.getOrderNumber(),
                request.getAmount(),
                request.getDescription(),
                request.getSuccessUrl(),
                request.getErrorUrl(),
                request.getCancelUrl()
        );

        if (checkoutUrl != null) {
            return ResponseEntity.ok(Map.of("checkout_url", checkoutUrl));
        } else {
            return ResponseEntity.badRequest().build();
        }
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
