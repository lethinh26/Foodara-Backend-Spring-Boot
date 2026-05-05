package com.db.foodara.service.payment;

import com.db.foodara.config.SepayConfig;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepayService {

    private final SepayConfig sepayConfig;
    private final ObjectMapper objectMapper;

        public String createCheckout(String orderId, String orderNumber, BigDecimal amount,
            String description, String successUrl, String errorUrl, String cancelUrl) {
        try {
            Map<String, Object> checkoutData = new LinkedHashMap<>();
            checkoutData.put("currency", "VND");
            checkoutData.put("order_invoice_number", orderNumber);
            checkoutData.put("order_amount", amount.intValue());
            checkoutData.put("operation", "PURCHASE");
            checkoutData.put("order_description", description);
            checkoutData.put("success_url", successUrl);
            checkoutData.put("error_url", errorUrl);
            checkoutData.put("cancel_url", cancelUrl);

            String dataToSign = buildSignatureString(checkoutData);
            String signature = hmacSha256(dataToSign, sepayConfig.getSecretKey());
            checkoutData.put("signature", signature);

            String requestBody = objectMapper.writeValueAsString(checkoutData);

            // Basic Auth header
            String auth = sepayConfig.getMerchantId() + ":" + sepayConfig.getSecretKey();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sepayConfig.getCheckoutUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", basicAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode responseNode = objectMapper.readTree(response.body());
                if (responseNode.has("checkout_url")) {
                    return responseNode.get("checkout_url").asText();
                }
                if (responseNode.has("data") && responseNode.get("data").has("checkout_url")) {
                    return responseNode.get("data").get("checkout_url").asText();
                }
                log.warn("SePay response does not contain checkout_url: {}", response.body());
                return null;
            } else {
                log.error("SePay checkout failed: status={}, body={}", response.statusCode(), response.body());
                throw new AppException(ErrorCode.PAYMENT_FAILED);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("SePay checkout error", e);
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * Verify IPN webhook authenticity using secret key header
     */
    public boolean verifyIpn(String secretKeyHeader) {
        if (secretKeyHeader == null || secretKeyHeader.isBlank()) {
            return true; // No auth configured on SePay side
        }
        return sepayConfig.getSecretKey().equals(secretKeyHeader);
    }

    private String buildSignatureString(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append(data.getOrDefault("currency", ""));
        sb.append(data.getOrDefault("order_invoice_number", ""));
        sb.append(data.getOrDefault("order_amount", ""));
        sb.append(data.getOrDefault("operation", ""));
        return sb.toString();
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}
