package com.db.foodara.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Uses Gemini AI to generate smart notification content based on order context.
 * Falls back to template if Gemini is unavailable or not configured.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiNotificationService {

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String model;

    /**
     * Generate personalized notification content using Gemini.
     */
    public String generateContent(String eventType, String context) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Gemini API key not configured, skipping AI generation for {}", eventType);
            return null;
        }

        // TODO: Integrate Google AI SDK (com.google.genai) to call Gemini.
        // For now, return null so TemplateService falls back to static templates.
        log.info("[Gemini] Would generate {} content with model {}", eventType, model);
        return null;
    }
}
