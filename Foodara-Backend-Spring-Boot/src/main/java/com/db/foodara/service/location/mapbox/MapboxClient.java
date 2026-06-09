package com.db.foodara.service.location.mapbox;

import com.db.foodara.config.CacheConfig;
import com.db.foodara.dto.internal.mapbox.DirectionsResult;
import com.db.foodara.dto.internal.mapbox.GeocodeResult;
import com.db.foodara.dto.internal.mapbox.SuggestItem;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class MapboxClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mapbox.access-token:}")
    private String accessToken;

    @Value("${app.mapbox.geocode-forward-url}")
    private String geocodeForwardUrl;

    @Value("${app.mapbox.geocode-reverse-url}")
    private String geocodeReverseUrl;

    @Value("${app.mapbox.searchbox-suggest-url}")
    private String searchboxSuggestUrl;

    @Value("${app.mapbox.searchbox-retrieve-url}")
    private String searchboxRetrieveUrl;

    @Value("${app.mapbox.directions-url}")
    private String directionsUrl;

    @Value("${app.mapbox.country:vn}")
    private String country;

    @Value("${app.mapbox.language:vi}")
    private String language;

    @Value("${app.mapbox.profile:driving-traffic}")
    private String defaultProfile;

    @Cacheable(cacheNames = CacheConfig.CACHE_MAPBOX_GEOCODE,
            key = "'fwd:' + #address + ':' + (#proximityLng != null ? #proximityLng : '') + ',' + (#proximityLat != null ? #proximityLat : '')")
    public GeocodeResult geocode(String address, Double proximityLng, Double proximityLat) {
        ensureToken();
        URI uri = UriComponentsBuilder.fromUriString(geocodeForwardUrl)
                .queryParam("q", address)
                .queryParam("country", country)
                .queryParam("language", language)
                .queryParam("limit", 5)
                .queryParam("access_token", accessToken)
                .queryParamIfPresent("proximity", buildProximity(proximityLng, proximityLat))
                .build()
                .encode()
                .toUri();
        JsonNode root = call(uri, "geocode.forward");
        return parseFirstFeature(root)
                .orElseThrow(() -> new AppException(ErrorCode.GEOCODING_FAILED));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_MAPBOX_GEOCODE,
            key = "'rev:' + #lat + ',' + #lng")
    public GeocodeResult reverseGeocode(BigDecimal lat, BigDecimal lng) {
        ensureToken();
        URI uri = UriComponentsBuilder.fromUriString(geocodeReverseUrl)
                .queryParam("longitude", lng.toPlainString())
                .queryParam("latitude", lat.toPlainString())
                .queryParam("language", language)
                .queryParam("limit", 1)
                .queryParam("access_token", accessToken)
                .build()
                .encode()
                .toUri();
        JsonNode root = call(uri, "geocode.reverse");
        return parseFirstFeature(root)
                .orElseThrow(() -> new AppException(ErrorCode.GEOCODING_FAILED));
    }

    public List<SuggestItem> suggest(String query, String sessionToken,
                                     Double proximityLng, Double proximityLat) {
        ensureToken();
        URI uri = UriComponentsBuilder.fromUriString(searchboxSuggestUrl)
                .queryParam("q", query)
                .queryParam("country", country)
                .queryParam("language", language)
                .queryParam("limit", 5)
                .queryParam("session_token", sessionToken)
                .queryParam("access_token", accessToken)
                .queryParamIfPresent("proximity", buildProximity(proximityLng, proximityLat))
                .build()
                .encode()
                .toUri();
        JsonNode root = call(uri, "searchbox.suggest");
        List<SuggestItem> items = new ArrayList<>();
        for (JsonNode node : root.path("suggestions")) {
            items.add(new SuggestItem(
                    text(node.path("mapbox_id")),
                    text(node.path("name")),
                    text(node.path("full_address"))
            ));
        }
        return items;
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_MAPBOX_GEOCODE,
            key = "'retrieve:' + #mapboxId")
    public GeocodeResult retrieve(String mapboxId, String sessionToken) {
        ensureToken();
        URI uri = UriComponentsBuilder.fromUriString(searchboxRetrieveUrl + "/" + mapboxId)
                .queryParam("language", language)
                .queryParam("session_token", sessionToken)
                .queryParam("access_token", accessToken)
                .build()
                .encode()
                .toUri();
        JsonNode root = call(uri, "searchbox.retrieve");
        return parseFirstFeature(root)
                .orElseThrow(() -> new AppException(ErrorCode.GEOCODING_FAILED));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_MAPBOX_DIRECTIONS,
            key = "#fromLat + ',' + #fromLng + '->' + #toLat + ',' + #toLng + ':' + (#profile != null ? #profile : '')")
    public DirectionsResult directions(double fromLat, double fromLng,
                                       double toLat, double toLng,
                                       String profile) {
        ensureToken();
        String useProfile = StringUtils.hasText(profile) ? profile : defaultProfile;
        String coords = String.format(Locale.US, "%f,%f;%f,%f", fromLng, fromLat, toLng, toLat);
        URI uri = UriComponentsBuilder.fromUriString(directionsUrl + "/" + useProfile + "/" + coords)
                .queryParam("alternatives", false)
                .queryParam("geometries", "polyline")
                .queryParam("overview", "full")
                .queryParam("language", language)
                .queryParam("access_token", accessToken)
                .build()
                .encode()
                .toUri();
        JsonNode root = call(uri, "directions");
        JsonNode routes = root.path("routes");
        if (!routes.isArray() || routes.isEmpty()) {
            throw new AppException(ErrorCode.ROUTE_NOT_FOUND);
        }
        JsonNode route = routes.get(0);
        BigDecimal km = BigDecimal.valueOf(route.path("distance").asDouble(0d) / 1000d)
                .setScale(2, RoundingMode.HALF_UP);
        int minutes = (int) Math.ceil(route.path("duration").asDouble(0d) / 60d);
        return new DirectionsResult(km, minutes, text(route.path("geometry")));
    }

    public boolean hasAccessToken() {
        return StringUtils.hasText(accessToken);
    }

    private void ensureToken() {
        if (!StringUtils.hasText(accessToken)) {
            throw new AppException(ErrorCode.MAPBOX_UNAVAILABLE);
        }
    }

    private JsonNode call(URI uri, String op) {
        try {
            String body = restTemplate.getForObject(uri, String.class);
            if (body == null) {
                throw new AppException(ErrorCode.MAPBOX_UNAVAILABLE);
            }
            return objectMapper.readTree(body);
        } catch (RestClientResponseException ex) {
            HttpStatusCode status = ex.getStatusCode();
            log.warn("Mapbox {} HTTP {}", op, status);
            if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new AppException(ErrorCode.MAPBOX_RATE_LIMITED);
            }
            throw new AppException(ErrorCode.MAPBOX_UNAVAILABLE);
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Mapbox {} failed", op, ex);
            throw new AppException(ErrorCode.MAPBOX_UNAVAILABLE);
        }
    }

    private java.util.Optional<GeocodeResult> parseFirstFeature(JsonNode root) {
        JsonNode features = root.path("features");
        if (!features.isArray() || features.isEmpty()) {
            return java.util.Optional.empty();
        }
        JsonNode feat = features.get(0);
        JsonNode coords = feat.path("geometry").path("coordinates");
        if (!coords.isArray() || coords.size() < 2) {
            return java.util.Optional.empty();
        }
        BigDecimal lng = new BigDecimal(coords.get(0).asText("0"));
        BigDecimal lat = new BigDecimal(coords.get(1).asText("0"));
        JsonNode props = feat.path("properties");
        String full = firstNonBlank(
                text(props.path("full_address")),
                text(props.path("place_formatted")),
                text(props.path("name"))
        );
        JsonNode ctx = props.path("context");
        return java.util.Optional.of(new GeocodeResult(
                lat, lng, full,
                text(ctx.path("neighborhood").path("name")),
                text(ctx.path("locality").path("name")),
                pickCity(ctx)
        ));
    }

    private String pickCity(JsonNode ctx) {
        String place = text(ctx.path("place").path("name"));
        if (StringUtils.hasText(place)) return place;
        String region = text(ctx.path("region").path("name"));
        return region;
    }

    private java.util.Optional<String> buildProximity(Double lng, Double lat) {
        if (lng == null || lat == null) return java.util.Optional.empty();
        return java.util.Optional.of(String.format(Locale.US, "%f,%f", lng, lat));
    }

    private String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (StringUtils.hasText(v)) return v;
        }
        return "";
    }

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return "";
        return node.asText("");
    }
}
