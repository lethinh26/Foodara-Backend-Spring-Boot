package com.db.foodara.controller.home;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.home.BannerResponse;
import com.db.foodara.dto.response.home.CampaignResponse;
import com.db.foodara.dto.response.store.StoreResponse;
import com.db.foodara.service.home.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    // GET /v1/home/banners
    @GetMapping("/banners")
    public ApiResponse<List<BannerResponse>> getBanners() {
        return ApiResponse.success(homeService.getBanners());
    }

    // GET /v1/home/nearby-stores
    @GetMapping("/nearby-stores")
    public ApiResponse<List<StoreResponse>> getNearbyStores(
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(homeService.getNearbyStores(lat, lng, limit));
    }

    // GET /v1/home/popular-stores
    @GetMapping("/popular-stores")
    public ApiResponse<List<StoreResponse>> getPopularStores(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(homeService.getPopularStores(limit));
    }

    // GET /v1/home/promotions
    @GetMapping("/promotions")
    public ApiResponse<List<StoreResponse>> getPromotionStores(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(homeService.getPromotionStores(limit));
    }

    // GET /v1/home/recommendations
    @GetMapping("/recommendations")
    public ApiResponse<List<StoreResponse>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(homeService.getRecommendations(limit));
    }

    // GET /v1/home/flash-deals
    @GetMapping("/flash-deals")
    public ApiResponse<List<CampaignResponse>> getFlashDeals() {
        return ApiResponse.success(homeService.getFlashDeals());
    }

    // GET /v1/home/campaigns
    @GetMapping("/campaigns")
    public ApiResponse<List<CampaignResponse>> getCampaigns() {
        return ApiResponse.success(homeService.getActiveCampaigns());
    }

    // GET /v1/home/campaigns/:id
    @GetMapping("/campaigns/{id}")
    public ApiResponse<CampaignResponse> getCampaignById(@PathVariable String id) {
        return ApiResponse.success(homeService.getCampaignById(id));
    }
}