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
@RequestMapping("/v1")
public class StoreCategoryController {
    @Autowired
    private StoreCategoryService storeCategoryService;

    @PostMapping("/admin/store-categories")
    public ApiResponse<StoreCategoryResponse> createStoreCategory(@RequestBody @Valid StoreCategoryCreateDto request) {
        return ApiResponse.success(storeCategoryService.createStoreCategory(request));
    }

    @GetMapping({"/admin/store-categories", "/home/categories"})
    public ApiResponse<List<StoreCategoryResponse>> getStoreCategory() {
        return ApiResponse.success(storeCategoryService.getStoreCategory());
    }

    @GetMapping("/admin/store-categories/{storeCategoryId}")
    public ApiResponse<StoreCategoryResponse> getStoreCategory(@PathVariable String storeCategoryId) {
        return ApiResponse.success(storeCategoryService.getStoreCategory(storeCategoryId));
    }

    @PutMapping("/admin/store-categories/{storeCategoryId}")
    public ApiResponse<StoreCategoryResponse> updateStoreCategory(@PathVariable String storeCategoryId,
                                      @RequestBody StoreCategoryUpdateDto request) {
        return ApiResponse.success(storeCategoryService.updateStoreCategory(storeCategoryId, request));
    }

    @DeleteMapping("/admin/store-categories/{storeCategoryId}")
    public ApiResponse<Void> deleteStoreCategory(@PathVariable String storeCategoryId) {
        storeCategoryService.deleteStoreCategory(storeCategoryId);
        return ApiResponse.success("Store category has been deleted");
    }
}