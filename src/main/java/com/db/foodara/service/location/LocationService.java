package com.db.foodara.service.location;

import com.db.foodara.dto.response.location.*;
import com.db.foodara.entity.location.City;
import com.db.foodara.entity.location.District;
import com.db.foodara.entity.location.ServiceZone;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.location.CityRepository;
import com.db.foodara.repository.location.DistrictRepository;
import com.db.foodara.repository.location.ServiceZoneRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ServiceZoneRepository serviceZoneRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.serpapi.key:}")
    private String serpApiKey;

    @Value("${app.serpapi.google-maps-url:https://serpapi.com/search?engine=google_maps}")
    private String serpApiGoogleMapsUrl;

    // ============================================================
    // 1. GET /v1/locations/cities — Danh sách thành phố hỗ trợ
    // ============================================================
    public List<CityResponse> getCities() {
        return cityRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::mapToCityResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 2. GET /v1/locations/cities/{id}/districts — Quận theo TP
    // ============================================================
    public List<DistrictResponse> getDistrictsByCity(String cityId) {
        cityRepository.findById(cityId)
                .orElseThrow(() -> new AppException(ErrorCode.CITY_NOT_FOUND));
        return districtRepository.findByCityIdAndIsActiveTrueOrderByNameAsc(cityId).stream()
                .map(this::mapToDistrictResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 3. GET /v1/locations/check-coverage — Kiểm tra vùng phục vụ
    // ============================================================
    public CoverageCheckResponse checkCoverage(BigDecimal latitude, BigDecimal longitude) {
        validateCoordinates(latitude, longitude);

        List<ServiceZone> activeZones = serviceZoneRepository.findByIsActiveTrue();

        for (ServiceZone zone : activeZones) {
            if (zone.getBoundaryGeojson() != null) {
                String geojsonStr = zone.getBoundaryGeojson().toString();
                if (!geojsonStr.isBlank()) {
                    if (isPointInZone(latitude.doubleValue(), longitude.doubleValue(), geojsonStr)) {
                        City city = zone.getCityId() != null
                                ? cityRepository.findById(zone.getCityId()).orElse(null)
                                : null;
                        return CoverageCheckResponse.builder()
                                .covered(true)
                                .zoneId(zone.getId())
                                .zoneName(zone.getName())
                                .cityId(zone.getCityId())
                                .cityName(city != null ? city.getName() : null)
                                .surgeMultiplier(zone.getSurgeMultiplier())
                                .build();
                    }
                }
            }
        }

        return CoverageCheckResponse.builder()
                .covered(false)
                .build();
    }

    // ============================================================
    // 4. GET /v1/locations/geocode — Địa chỉ → Toạ độ (SerpAPI)
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
    // 5. GET /v1/locations/reverse-geocode — Toạ độ → Địa chỉ
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
    // Ray Casting Algorithm — Point-in-Polygon
    // ============================================================
    private boolean isPointInZone(double lat, double lng, String boundaryGeojson) {
        try {
            JsonNode root = objectMapper.readTree(boundaryGeojson);
            JsonNode coordinates = root.path("coordinates");

            // GeoJSON Polygon: coordinates = [ [ [lng, lat], [lng, lat], ... ] ]
            if (!coordinates.isArray() || coordinates.isEmpty()) return false;

            JsonNode ring = coordinates.get(0); // outer ring
            if (!ring.isArray() || ring.size() < 4) return false; // minimum 4 points (triangle + close)

            return rayCasting(lat, lng, ring);
        } catch (Exception e) {
            log.warn("Failed to parse boundary GeoJSON: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Ray Casting algorithm:
     * Cast a ray from the point horizontally to the right.
     * Count intersections with polygon edges.
     * Odd count = inside, Even count = outside.
     *
     * Note: GeoJSON uses [longitude, latitude] order.
     */
    private boolean rayCasting(double lat, double lng, JsonNode ring) {
        int n = ring.size();
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            // GeoJSON: [lng, lat] — index 0 = longitude, index 1 = latitude
            double yi = ring.get(i).get(1).asDouble(); // latitude of point i
            double xi = ring.get(i).get(0).asDouble(); // longitude of point i
            double yj = ring.get(j).get(1).asDouble(); // latitude of point j
            double xj = ring.get(j).get(0).asDouble(); // longitude of point j

            // Check if the ray from (lat, lng) going right intersects edge (i, j)
            if ((yi > lat) != (yj > lat) &&
                    lng < (xj - xi) * (lat - yi) / (yj - yi) + xi) {
                inside = !inside;
            }
        }

        return inside;
    }

    // ============================================================
    // Validation & Mapping helpers
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
     * Replaces deprecated asText(String defaultValue).
     */
    private String getNodeText(JsonNode node, String defaultValue) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return defaultValue;
        }
        return node.asText();
    }

    private CityResponse mapToCityResponse(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .code(city.getCode())
                .isActive(Boolean.TRUE.equals(city.getIsActive()))
                .build();
    }

    private DistrictResponse mapToDistrictResponse(District district) {
        return DistrictResponse.builder()
                .id(district.getId())
                .cityId(district.getCityId())
                .name(district.getName())
                .code(district.getCode())
                .isActive(Boolean.TRUE.equals(district.getIsActive()))
                .build();
    }
}