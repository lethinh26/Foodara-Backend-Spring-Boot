package com.db.foodara.controller.location;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.location.GeocodeResponse;
import com.db.foodara.service.location.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // GET /api/locations/geocode?address=..
    @GetMapping("/geocode")
    public ApiResponse<GeocodeResponse> geocode(@RequestParam("address") String address) {
        return ApiResponse.success(locationService.geocode(address));
    }

    // GET /api/locations/reverse-geocode?lat=..&lng=..
    @GetMapping("/reverse-geocode")
    public ApiResponse<GeocodeResponse> reverseGeocode(
            @RequestParam("lat") BigDecimal lat,
            @RequestParam("lng") BigDecimal lng) {
        return ApiResponse.success(locationService.reverseGeocode(lat, lng));
    }
}