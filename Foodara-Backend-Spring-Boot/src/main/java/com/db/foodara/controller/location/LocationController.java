package com.db.foodara.controller.location;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.location.DirectionsResponse;
import com.db.foodara.dto.response.location.GeocodeResponse;
import com.db.foodara.dto.response.location.SuggestResponse;
import com.db.foodara.service.location.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/geocode")
    public ApiResponse<GeocodeResponse> geocode(@RequestParam("address") String address) {
        return ApiResponse.success(locationService.geocode(address));
    }

    @GetMapping("/reverse-geocode")
    public ApiResponse<GeocodeResponse> reverseGeocode(
            @RequestParam("lat") BigDecimal lat,
            @RequestParam("lng") BigDecimal lng) {
        return ApiResponse.success(locationService.reverseGeocode(lat, lng));
    }

    @GetMapping("/suggest")
    public ApiResponse<List<SuggestResponse>> suggest(
            @RequestParam("q") String query,
            @RequestParam("sessionToken") String sessionToken,
            @RequestParam(value = "proximityLat", required = false) BigDecimal proximityLat,
            @RequestParam(value = "proximityLng", required = false) BigDecimal proximityLng) {
        return ApiResponse.success(locationService.suggest(query, sessionToken, proximityLat, proximityLng));
    }

    @GetMapping("/retrieve")
    public ApiResponse<GeocodeResponse> retrieve(
            @RequestParam("id") String id,
            @RequestParam("sessionToken") String sessionToken) {
        return ApiResponse.success(locationService.retrieve(id, sessionToken));
    }

    @GetMapping("/directions")
    public ApiResponse<DirectionsResponse> directions(
            @RequestParam("fromLat") BigDecimal fromLat,
            @RequestParam("fromLng") BigDecimal fromLng,
            @RequestParam("toLat") BigDecimal toLat,
            @RequestParam("toLng") BigDecimal toLng) {
        return ApiResponse.success(locationService.directions(fromLat, fromLng, toLat, toLng));
    }
}
