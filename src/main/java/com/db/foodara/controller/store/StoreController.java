package com.db.foodara.controller.store;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.store.*;
import com.db.foodara.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // C06: GET /v1/stores/:id
    @GetMapping("/{id}")
    public ApiResponse<StoreResponse> getStoreById(@PathVariable String id) {
        return ApiResponse.success(storeService.getStoreById(id));
    }

    // C07: GET /v1/stores/:id/menu-categories
    @GetMapping("/{id}/menu-categories")
    public ApiResponse<List<MenuCategoryResponse>> getMenuCategories(@PathVariable String id) {
        return ApiResponse.success(storeService.getMenuCategories(id));
    }

    // C07: GET /v1/stores/:id/menu-items
    @GetMapping("/{id}/menu-items")
    public ApiResponse<List<MenuItemResponse>> getMenuItems(@PathVariable String id) {
        return ApiResponse.success(storeService.getMenuItems(id));
    }

    // GET /v1/stores/:id/menu-items-detail - includes option groups
    @GetMapping("/{id}/menu-items-detail")
    public ApiResponse<List<MenuItemDetailResponse>> getMenuItemsWithOptions(@PathVariable String id) {
        return ApiResponse.success(storeService.getMenuItemsWithOptions(id));
    }

    // C13: GET /v1/stores/:id/reviews
    @GetMapping("/{id}/reviews")
    public ApiResponse<List<ReviewResponse>> getReviews(@PathVariable String id) {
        return ApiResponse.success(storeService.getReviews(id));
    }
}
