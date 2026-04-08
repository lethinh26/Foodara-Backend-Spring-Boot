package com.db.foodara.controller.store;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.store.StoreCategoryResponse;
import com.db.foodara.dto.request.store.StoreCategoryCreateDto;
import com.db.foodara.dto.request.store.StoreCategoryUpdateDto;
import com.db.foodara.service.store.StoreCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StoreCategoryController {
    @Autowired
    private StoreCategoryService storeCategoryService;

    @PostMapping("/v1/admin/store-categories")
    public ApiResponse<StoreCategoryResponse> createStoreCategory(@RequestBody @Valid StoreCategoryCreateDto request) {
        return ApiResponse.success(storeCategoryService.createStoreCategory(request));
    }

    @GetMapping({"/v1/admin/store-categories", "/v1/home/categories"})
    public ApiResponse<List<StoreCategoryResponse>> getStoreCategory() {
        return ApiResponse.success(storeCategoryService.getStoreCategory());
    }

    @GetMapping("/v1/admin/store-categories/{storeCategoryId}")
    public ApiResponse<StoreCategoryResponse> getStoreCategory(@PathVariable String storeCategoryId) {
        return ApiResponse.success(storeCategoryService.getStoreCategory(storeCategoryId));
    }

    @PutMapping("/v1/admin/store-categories/{storeCategoryId}")
    public ApiResponse<StoreCategoryResponse> updateStoreCategory(@PathVariable String storeCategoryId,
                                      @RequestBody StoreCategoryUpdateDto request) {
        return ApiResponse.success(storeCategoryService.updateStoreCategory(storeCategoryId, request));
    }

    @DeleteMapping("/v1/admin/store-categories/{storeCategoryId}")
    public ApiResponse<Void> deleteStoreCategory(@PathVariable String storeCategoryId) {
        storeCategoryService.deleteStoreCategory(storeCategoryId);
        return ApiResponse.success("Store category has been deleted");
    }
}