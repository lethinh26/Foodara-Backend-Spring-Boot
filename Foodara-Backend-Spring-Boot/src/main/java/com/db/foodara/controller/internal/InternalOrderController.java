package com.db.foodara.controller.internal;

import com.db.foodara.service.order.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/internal/orders")
@RequiredArgsConstructor
@Slf4j
public class InternalOrderController {

    private final CustomerOrderService customerOrderService;

    @PutMapping("/{invoiceNumber}/payment-status")
    public ResponseEntity<Void> updatePaymentStatus(
            @PathVariable String invoiceNumber,
            @RequestBody Map<String, String> payload
    ) {
        String status = payload.get("status");
        log.info("Received internal request to update payment status for invoice {} to {}", invoiceNumber, status);
        customerOrderService.updatePaymentStatus(invoiceNumber, status);
        return ResponseEntity.ok().build();
    }
}
