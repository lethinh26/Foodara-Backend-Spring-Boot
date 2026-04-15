package com.db.foodara.controller.home;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.home.SearchHistoryResponse;
import com.db.foodara.dto.response.store.MenuItemResponse;
import com.db.foodara.dto.response.store.StoreResponse;
import com.db.foodara.service.home.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // GET /v1/search/stores
    @GetMapping("/stores")
    public ApiResponse<List<StoreResponse>> searchStores(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxDeliveryFee,
            @RequestParam(required = false) Boolean hasPromotion,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(
                searchService.searchStores(query, categoryId, minRating, maxDeliveryFee,
                        hasPromotion, sortBy, lat, lng, page, limit));
    }

    // GET /v1/search/items
    @GetMapping("/items")
    public ApiResponse<List<MenuItemResponse>> searchItems(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(searchService.searchItems(query, page, limit));
    }

    // GET /v1/search/suggestions
    @GetMapping("/suggestions")
    public ApiResponse<List<String>> getSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(searchService.getSuggestions(query, limit));
    }

    // GET /v1/search/history
    @GetMapping("/history")
    public ApiResponse<List<SearchHistoryResponse>> getSearchHistory(Authentication authentication) {
        String userId = authentication.getName();
        return ApiResponse.success(searchService.getSearchHistory(userId));
    }

    // DELETE /v1/search/history
    @DeleteMapping("/history")
    public ApiResponse<Void> clearSearchHistory(Authentication authentication) {
        String userId = authentication.getName();
        searchService.clearSearchHistory(userId);
        return ApiResponse.success("Search history cleared");
    }
}