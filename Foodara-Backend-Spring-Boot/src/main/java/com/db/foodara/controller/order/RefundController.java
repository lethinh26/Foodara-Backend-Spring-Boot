package com.db.foodara.controller.order;

import com.db.foodara.service.order.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    /**
     * Customer chooses refund method after a paid QR order is cancelled.
     * type: "bank" → just acknowledge, "voucher" → create voucher.
     */
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Map<String, Object>> chooseRefund(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body
    ) {
        String type = body.getOrDefault("type", "bank");
        Map<String, Object> result = refundService.processRefund(orderId, type);
        return ResponseEntity.ok(result);
    }
}
