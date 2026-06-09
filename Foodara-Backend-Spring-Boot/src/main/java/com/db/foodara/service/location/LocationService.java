package com.db.foodara.service.location;

import com.db.foodara.dto.internal.mapbox.DirectionsResult;
import com.db.foodara.dto.internal.mapbox.GeocodeResult;
import com.db.foodara.dto.internal.mapbox.SuggestItem;
import com.db.foodara.dto.response.location.DirectionsResponse;
import com.db.foodara.dto.response.location.GeocodeResponse;
import com.db.foodara.dto.response.location.SuggestResponse;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.service.location.mapbox.MapboxClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final MapboxClient mapboxClient;

    public GeocodeResponse geocode(String address) {
        if (!StringUtils.hasText(address)) {
            throw new AppException(ErrorCode.GEOCODING_FAILED);
        }
        GeocodeResult result = mapboxClient.geocode(address, null, null);
        return toResponse(result);
    }

    public GeocodeResponse reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        validateCoordinates(latitude, longitude);
        GeocodeResult result = mapboxClient.reverseGeocode(latitude, longitude);
        return toResponse(result);
    }

    public List<SuggestResponse> suggest(String query, String sessionToken,
                                         BigDecimal proximityLat, BigDecimal proximityLng) {
        if (!StringUtils.hasText(query) || !StringUtils.hasText(sessionToken)) {
            throw new AppException(ErrorCode.GEOCODING_FAILED);
        }
        Double pLng = proximityLng != null ? proximityLng.doubleValue() : null;
        Double pLat = proximityLat != null ? proximityLat.doubleValue() : null;
        List<SuggestItem> items = mapboxClient.suggest(query, sessionToken, pLng, pLat);
        return items.stream()
                .map(it -> SuggestResponse.builder()
                        .id(it.mapboxId())
                        .name(it.name())
                        .fullAddress(it.fullAddress())
                        .build())
                .toList();
    }

    public GeocodeResponse retrieve(String mapboxId, String sessionToken) {
        if (!StringUtils.hasText(mapboxId) || !StringUtils.hasText(sessionToken)) {
            throw new AppException(ErrorCode.GEOCODING_FAILED);
        }
        return toResponse(mapboxClient.retrieve(mapboxId, sessionToken));
    }

    public DirectionsResponse directions(BigDecimal fromLat, BigDecimal fromLng,
                                         BigDecimal toLat, BigDecimal toLng) {
        validateCoordinates(fromLat, fromLng);
        validateCoordinates(toLat, toLng);
        DirectionsResult result = mapboxClient.directions(
                fromLat.doubleValue(), fromLng.doubleValue(),
                toLat.doubleValue(), toLng.doubleValue(),
                null);
        return DirectionsResponse.builder()
                .distanceKm(result.distanceKm())
                .durationMinutes(result.durationMinutes())
                .polyline(result.polyline())
                .build();
    }

    private GeocodeResponse toResponse(GeocodeResult result) {
        return GeocodeResponse.builder()
                .latitude(result.latitude())
                .longitude(result.longitude())
                .formattedAddress(result.formattedAddress())
                .addressLine(result.formattedAddress())
                .ward(result.ward())
                .districtName(result.district())
                .cityName(result.city())
                .build();
    }

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
}

