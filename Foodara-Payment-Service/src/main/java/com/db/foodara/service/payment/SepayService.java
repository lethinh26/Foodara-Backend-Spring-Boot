package com.db.foodara.service.payment;

import com.db.foodara.config.SepayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepayService {

    private final SepayConfig sepayConfig;

    /**
     * Build the SePay QR image URL.
     * Uses orderNumber as the bank transfer description.
     */
    public String getQrUrl(String orderNumber, java.math.BigDecimal amount) {
        long amt = amount == null ? 0L : amount.longValue();
        return "https://qr.sepay.vn/img"
            + "?bank=" + sepayConfig.getBankCode()
            + "&acc=" + sepayConfig.getAccountNumber()
            + "&template=" + sepayConfig.getQrTemplate()
            + "&des=" + orderNumber
            + "&amount=" + amt;
    }

    /**
     * Verify HMAC-SHA256 signature from X-SePay-Signature header.
     * The signed string is "{timestamp}.{raw_body}".
     */
    public boolean verifySignature(String dataToSign, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("Missing X-SePay-Signature header");
            return false;
        }
        String expected = hmacSha256(dataToSign, sepayConfig.getSecretKey());
        return MessageDigest.isEqual(expected.getBytes(), signatureHeader.getBytes());
    }

    /**
     * Parse SePay transfer content to find Foodara order number.
     * Banks often strip dashes, so we match "FD" + 9 digits (no dashes)
     * and normalize to FD-YYMMDD-NNN format.
     * Example: "FD260610064" → "FD-260610-064"
     */
    public String extractOrderNumber(String transferContent) {
        if (transferContent == null || transferContent.isBlank()) {
            return null;
        }
        // Match FD followed by 9 digits anywhere in the content
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("FD(\\d{9})", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(transferContent.toUpperCase());
        if (matcher.find()) {
            String digits = matcher.group(1); // e.g. "260610064"
            return "FD-" + digits.substring(0, 6) + "-" + digits.substring(6);
        }
        return null;
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
