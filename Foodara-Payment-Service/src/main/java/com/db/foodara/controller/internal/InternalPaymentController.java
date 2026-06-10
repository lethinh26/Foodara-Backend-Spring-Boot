package com.db.foodara.controller.internal;

import com.db.foodara.service.payment.SepayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/internal/payment")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final SepayService sepayService;

    /**
     * Build the SePay QR image URL for a given order number + amount.
     */
    @GetMapping("/qr-url")
    public ResponseEntity<Map<String, String>> getQrUrl(
            @RequestParam String orderNumber,
            @RequestParam(required = false, defaultValue = "0") long amount) {
        String url = sepayService.getQrUrl(orderNumber, java.math.BigDecimal.valueOf(amount));
        return ResponseEntity.ok(Map.of(
            "qrUrl", url,
            "orderNumber", orderNumber
        ));
    }
}
