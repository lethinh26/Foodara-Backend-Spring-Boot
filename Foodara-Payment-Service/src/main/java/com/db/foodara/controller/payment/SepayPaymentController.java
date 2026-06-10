package com.db.foodara.controller.payment;

import com.db.foodara.client.MainBackendClient;
import com.db.foodara.service.payment.SepayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.Map;

@RestController
@RequestMapping("/v1/payment/sepay")
@RequiredArgsConstructor
@Slf4j
public class SepayPaymentController {

    private final SepayService sepayService;
    private final MainBackendClient mainBackendClient;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestHeader(value = "X-SePay-Signature", required = false) String signatureHeader,
            @RequestHeader(value = "X-SePay-Timestamp", required = false) String timestamp,
            HttpServletRequest servletRequest
    ) {
        try {
            StringBuilder rawBody = new StringBuilder();
            BufferedReader reader = servletRequest.getReader();
            String line;
            while ((line = reader.readLine()) != null) rawBody.append(line);
            String payload = rawBody.toString();

            String dataToSign = (timestamp != null ? timestamp : "") + "." + payload;

            String cleanSignature = signatureHeader;
            if (cleanSignature != null && cleanSignature.startsWith("sha256=")) {
                cleanSignature = cleanSignature.substring(7);
            }

            if (!sepayService.verifySignature(dataToSign, cleanSignature)) {
                log.warn("SePay webhook: invalid signature");
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid signature"));
            }

            JsonNode body = objectMapper.readTree(payload);
            String content = body.has("content") ? body.get("content").asText() : "";
            long transferAmount = body.has("transferAmount") ? body.get("transferAmount").asLong() : 0;

            log.info("SePay webhook: amount={}, content={}", transferAmount, content);

            // Extract order number from content (e.g., FD-260609-830)
            String orderNumber = sepayService.extractOrderNumber(content);

            if (orderNumber == null) {
                log.info("SePay webhook: no order number found in content");
                return ResponseEntity.ok(Map.of("success", true, "message", "No Foodara order number"));
            }

            log.info("SePay webhook: matched order {} — marking as paid", orderNumber);
            mainBackendClient.updatePaymentStatus(orderNumber, "paid");

            return ResponseEntity.ok(Map.of("success", true, "orderNumber", orderNumber));

        } catch (Exception e) {
            log.error("SePay webhook error", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Internal error"));
        }
    }
}
