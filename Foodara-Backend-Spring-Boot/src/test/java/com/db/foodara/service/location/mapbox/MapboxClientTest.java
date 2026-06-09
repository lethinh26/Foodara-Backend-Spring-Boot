package com.db.foodara.service.location.mapbox;

import com.db.foodara.dto.internal.mapbox.DirectionsResult;
import com.db.foodara.dto.internal.mapbox.GeocodeResult;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapboxClientTest {

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MapboxClient mapboxClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapboxClient, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(mapboxClient, "accessToken", "test-token");
        ReflectionTestUtils.setField(mapboxClient, "geocodeForwardUrl",
                "https://api.mapbox.com/search/geocode/v6/forward");
        ReflectionTestUtils.setField(mapboxClient, "geocodeReverseUrl",
                "https://api.mapbox.com/search/geocode/v6/reverse");
        ReflectionTestUtils.setField(mapboxClient, "searchboxSuggestUrl",
                "https://api.mapbox.com/search/searchbox/v1/suggest");
        ReflectionTestUtils.setField(mapboxClient, "searchboxRetrieveUrl",
                "https://api.mapbox.com/search/searchbox/v1/retrieve");
        ReflectionTestUtils.setField(mapboxClient, "directionsUrl",
                "https://api.mapbox.com/directions/v5/mapbox");
        ReflectionTestUtils.setField(mapboxClient, "country", "vn");
        ReflectionTestUtils.setField(mapboxClient, "language", "vi");
        ReflectionTestUtils.setField(mapboxClient, "defaultProfile", "driving-traffic");
    }

    @Test
    void geocode_parsesFirstFeature() {
        String body = "{\"features\":[{\"geometry\":{\"coordinates\":[106.6297,10.8231]}," +
                "\"properties\":{\"full_address\":\"227 Nguyen Van Cu, Q5, HCM\"," +
                "\"context\":{\"neighborhood\":{\"name\":\"P4\"}," +
                "\"locality\":{\"name\":\"Q5\"},\"place\":{\"name\":\"HCM\"}}}}]}";
        when(restTemplate.getForObject(any(URI.class), eq(String.class))).thenReturn(body);

        GeocodeResult result = mapboxClient.geocode("227 Nguyen Van Cu", null, null);

        assertThat(result.latitude()).isEqualByComparingTo(new BigDecimal("10.8231"));
        assertThat(result.longitude()).isEqualByComparingTo(new BigDecimal("106.6297"));
        assertThat(result.formattedAddress()).contains("Nguyen Van Cu");
        assertThat(result.district()).isEqualTo("Q5");
        assertThat(result.city()).isEqualTo("HCM");
    }

    @Test
    void geocode_emptyFeatures_throwsGeocodingFailed() {
        when(restTemplate.getForObject(any(URI.class), eq(String.class)))
                .thenReturn("{\"features\":[]}");

        assertThatThrownBy(() -> mapboxClient.geocode("nowhere", null, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GEOCODING_FAILED);
    }

    @Test
    void geocode_rateLimited_mapsToMapboxRateLimited() {
        when(restTemplate.getForObject(any(URI.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS,
                        "rate limit", null, null, null));

        assertThatThrownBy(() -> mapboxClient.geocode("addr", null, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MAPBOX_RATE_LIMITED);
    }

    @Test
    void geocode_otherHttpError_mapsToMapboxUnavailable() {
        when(restTemplate.getForObject(any(URI.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_GATEWAY,
                        "boom", null, null, null));

        assertThatThrownBy(() -> mapboxClient.geocode("addr", null, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MAPBOX_UNAVAILABLE);
    }

    @Test
    void directions_success_parsesDistanceAndDuration() {
        String body = "{\"routes\":[{\"distance\":4500.0,\"duration\":600.0," +
                "\"geometry\":\"abc123\"}]}";
        when(restTemplate.getForObject(any(URI.class), eq(String.class))).thenReturn(body);

        DirectionsResult result = mapboxClient.directions(
                10.0, 106.0, 10.1, 106.1, null);

        assertThat(result.distanceKm()).isEqualByComparingTo(new BigDecimal("4.50"));
        assertThat(result.durationMinutes()).isEqualTo(10);
        assertThat(result.polyline()).isEqualTo("abc123");
    }

    @Test
    void directions_noRoutes_throwsRouteNotFound() {
        when(restTemplate.getForObject(any(URI.class), eq(String.class)))
                .thenReturn("{\"routes\":[]}");

        assertThatThrownBy(() -> mapboxClient.directions(
                10.0, 106.0, 10.1, 106.1, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROUTE_NOT_FOUND);
    }

    @Test
    void noToken_throwsMapboxUnavailable() {
        ReflectionTestUtils.setField(mapboxClient, "accessToken", "");

        assertThatThrownBy(() -> mapboxClient.geocode("addr", null, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MAPBOX_UNAVAILABLE);
    }
}
