package com.db.foodara.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Generates fun, friendly Vietnamese notification messages using Gemini AI.
 * Falls back to simple concatenation when Gemini is unavailable.
 */
@Slf4j
@Service
public class GeminiService {

    private final String apiKey;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public GeminiService(
            @Value("${app.gemini.api-key:}") String apiKey,
            @Value("${app.gemini.model:gemini-2.5-flash}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Generate a short, fun notification body in Vietnamese.
     */
    public String generateNotification(String code, Map<String, String> vars) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("No Gemini API key configured, using fallback");
            return null;
        }

        String prompt = buildPrompt(code, vars);
        return callGemini(prompt);
    }

    private String buildPrompt(String code, Map<String, String> vars) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý thông báo của app giao đồ ăn Foodara. ");
        sb.append("Viết một thông báo ngắn gọn (1-2 câu) bằng tiếng Việt, ");
        sb.append("giọng vui nhộn, thân thiện, có thể dùng emoji phù hợp. ");

        switch (code) {
            case "order_placed" -> sb.append("Thông báo cho khách: đơn hàng #")
                    .append(vars.getOrDefault("orderNumber", "?"))
                    .append(" tại ").append(vars.getOrDefault("storeName", "quán"))
                    .append(" đã được đặt, tổng ").append(vars.getOrDefault("totalAmount", "0")).append("đ.");
            case "new_order_merchant" -> sb.append("Thông báo cho chủ quán: có đơn mới #")
                    .append(vars.getOrDefault("orderNumber", "?"))
                    .append(" từ khách ").append(vars.getOrDefault("customerName", "?"))
                    .append(", tổng ").append(vars.getOrDefault("totalAmount", "0")).append("đ.");
            case "order_status_changed" -> sb.append("Thông báo cho khách: đơn #")
                    .append(vars.getOrDefault("orderNumber", "?"))
                    .append(" chuyển sang trạng thái \"")
                    .append(vars.getOrDefault("newStatus", "?")).append("\".");
            case "payment_completed" -> sb.append("Thông báo: thanh toán ")
                    .append(vars.getOrDefault("amount", "0")).append("đ qua ")
                    .append(vars.getOrDefault("paymentMethod", "?"))
                    .append(" thành công cho đơn #")
                    .append(vars.getOrDefault("orderNumber", "?")).append(".");
            case "order_cancelled" -> {
                String by = vars.getOrDefault("cancelledBy", "?");
                if ("customer".equals(by)) {
                    sb.append("Thông báo cho chủ quán: khách đã huỷ đơn #")
                            .append(vars.getOrDefault("orderNumber", "?"))
                            .append(".");
                } else {
                    sb.append("Thông báo cho khách: đơn #")
                            .append(vars.getOrDefault("orderNumber", "?"))
                            .append(" tại ").append(vars.getOrDefault("storeName", "quán"))
                            .append(" đã bị huỷ.");
                }
            }
            default -> sb.append("Sự kiện: ").append(code).append(". Thông tin: ").append(vars);
        }

        sb.append(" CHỈ trả về nội dung thông báo, không thêm giải thích.");
        return sb.toString();
    }

    private String callGemini(String prompt) {
        try {
            ObjectNode body = mapper.createObjectNode();
            ArrayNode contents = mapper.createArrayNode();
            ObjectNode content = mapper.createObjectNode();
            ArrayNode parts = mapper.createArrayNode();
            ObjectNode part = mapper.createObjectNode();
            part.put("text", prompt);
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            body.set("contents", contents);

            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                            + model + ":generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Gemini API returned status {}: {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) return null;

            JsonNode textNode = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String text = textNode.asText(null);
            if (text == null || text.isBlank()) return null;

            return text.trim();
        } catch (Exception e) {
            log.warn("Gemini call failed: {}", e.getMessage());
            return null;
        }
    }
}
