package com.db.foodara.controller.payment;

import com.db.foodara.client.MainBackendClient;
import com.db.foodara.service.payment.SepayService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/payment/sepay")
@RequiredArgsConstructor
@Slf4j
public class SepayPaymentController {

    private final SepayService sepayService;
    private final MainBackendClient mainBackendClient;

    /**
     * IPN (Instant Payment Notification) endpoint.
     * SePay calls this when a payment status changes.
     * Must return HTTP 200 to acknowledge receipt.
     */
    @PostMapping("/ipn")
    public ResponseEntity<Map<String, Object>> handleIpn(
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKey,
            @RequestBody JsonNode body
    ) {
        log.info("Received SePay IPN: {}", body.toString());

        // Verify secret key if configured
        if (!sepayService.verifyIpn(secretKey)) {
            log.warn("SePay IPN verification failed");
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        String notificationType = body.has("notification_type") ? body.get("notification_type").asText() : "";

        if ("ORDER_PAID".equals(notificationType)) {
            JsonNode orderNode = body.get("order");
            if (orderNode != null && orderNode.has("order_invoice_number")) {
                String invoiceNumber = orderNode.get("order_invoice_number").asText();
                log.info("SePay IPN: ORDER_PAID for invoice {}", invoiceNumber);
                try {
                    mainBackendClient.updatePaymentStatus(invoiceNumber, "paid");
                } catch (Exception e) {
                    log.error("Failed to update payment status for invoice {}", invoiceNumber, e);
                }
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Callback endpoint — SePay redirects customer here after payment.
     * This redirects to the frontend order tracking page.
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam String orderId,
            @RequestParam String status
    ) {
        // Redirect to frontend order page with payment status
        // TODO: Ensure this URL can be dynamically configured if needed
        String frontendUrl = "http://localhost:5173/customer/order/" + orderId + "?payment=" + status;
        return ResponseEntity.status(302)
                .header("Location", frontendUrl)
                .build();
    }
}
