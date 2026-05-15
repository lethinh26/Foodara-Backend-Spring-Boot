package com.db.foodara.controller.admin;

import com.db.foodara.dto.request.admin.StoreCategoryRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminStoreCategoryResponse;
import com.db.foodara.service.admin.AdminStoreCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/store-categories")
@RequiredArgsConstructor
public class AdminStoreCategoryController {

    private final AdminStoreCategoryService categoryService;

    @GetMapping
    public ApiResponse<PageResponse<AdminStoreCategoryResponse>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        return ApiResponse.success(categoryService.getCategories(page, size, search, isActive));
    }

    @PostMapping
    public ApiResponse<AdminStoreCategoryResponse> createCategory(@RequestBody @Valid StoreCategoryRequest request) {
        return ApiResponse.success(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminStoreCategoryResponse> updateCategory(@PathVariable String id,
                                                                  @RequestBody @Valid StoreCategoryRequest request) {
        return ApiResponse.success(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("Store category deleted");
    }
}
