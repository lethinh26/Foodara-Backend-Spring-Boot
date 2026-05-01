package com.db.foodara.service.location;

import com.db.foodara.dto.response.location.GeocodeResponse;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final ObjectMapper objectMapper;

    @Value("${app.serpapi.key:}")
    private String serpApiKey;

    @Value("${app.serpapi.google-maps-url:https://serpapi.com/search?engine=google_maps}")
    private String serpApiGoogleMapsUrl;

    // ============================================================
    // 1. GET /v1/locations/geocode — Địa chỉ → Toạ độ (SerpAPI)
    // ============================================================
    public GeocodeResponse geocode(String address) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            URI uri = UriComponentsBuilder.fromUriString(serpApiGoogleMapsUrl)
                    .queryParam("q", address)
                    .queryParam("api_key", serpApiKey)
                    .build()
                    .toUri();

            String response = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(response);

            JsonNode localResults = root.path("local_results");
            if (localResults.isArray() && !localResults.isEmpty()) {
                JsonNode first = localResults.get(0);
                JsonNode gps = first.path("gps_coordinates");
                return GeocodeResponse.builder()
                        .latitude(new BigDecimal(getNodeText(gps.path("latitude"), "0")))
                        .longitude(new BigDecimal(getNodeText(gps.path("longitude"), "0")))
                        .formattedAddress(getNodeText(first.path("address"), address))
                        .build();
            }

            // Fallback: try place_results
            JsonNode placeResults = root.path("place_results");
            if (!placeResults.isMissingNode()) {
                JsonNode gps = placeResults.path("gps_coordinates");
                return GeocodeResponse.builder()
                        .latitude(new BigDecimal(getNodeText(gps.path("latitude"), "0")))
                        .longitude(new BigDecimal(getNodeText(gps.path("longitude"), "0")))
                        .formattedAddress(getNodeText(placeResults.path("address"), address))
                        .build();
            }

            throw new AppException(ErrorCode.GEOCODING_FAILED);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Geocoding failed for address: {}", address, e);
            throw new AppException(ErrorCode.GEOCODING_FAILED);
        }
    }

    // ============================================================
    // 2. GET /v1/locations/reverse-geocode — Toạ độ → Địa chỉ
    // ============================================================
    public GeocodeResponse reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        validateCoordinates(latitude, longitude);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String query = latitude.toPlainString() + "," + longitude.toPlainString();

            URI uri = UriComponentsBuilder.fromUriString(serpApiGoogleMapsUrl)
                    .queryParam("q", query)
                    .queryParam("api_key", serpApiKey)
                    .build()
                    .toUri();

            String response = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(response);

            // Try place_results first for reverse geocode
            JsonNode placeResults = root.path("place_results");
            if (!placeResults.isMissingNode()) {
                JsonNode gps = placeResults.path("gps_coordinates");
                return GeocodeResponse.builder()
                        .latitude(new BigDecimal(getNodeText(gps.path("latitude"), latitude.toPlainString())))
                        .longitude(new BigDecimal(getNodeText(gps.path("longitude"), longitude.toPlainString())))
                        .formattedAddress(getNodeText(placeResults.path("address"), ""))
                        .build();
            }

            // Fallback: local_results
            JsonNode localResults = root.path("local_results");
            if (localResults.isArray() && !localResults.isEmpty()) {
                JsonNode first = localResults.get(0);
                return GeocodeResponse.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .formattedAddress(getNodeText(first.path("address"), ""))
                        .build();
            }

            // Return coordinates with empty address if API doesn't return results
            return GeocodeResponse.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .formattedAddress("")
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Reverse geocoding failed for lat={}, lng={}", latitude, longitude, e);
            throw new AppException(ErrorCode.GEOCODING_FAILED);
        }
    }

    // ============================================================
    // Validation & helpers
    // ============================================================
    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }
        double lat = latitude.doubleValue();
        double lng = longitude.doubleValue();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }
    }

    /**
     * Helper to get text from JsonNode with a default value.
     */
    private String getNodeText(JsonNode node, String defaultValue) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return defaultValue;
        }
        return node.asText();
    }
}