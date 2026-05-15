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
@RequestMapping("/v1/home")
public class StoreCategoryController {
    @Autowired
    private StoreCategoryService storeCategoryService;

    @GetMapping("/categories")
    public ApiResponse<List<StoreCategoryResponse>> getStoreCategory() {
        return ApiResponse.success(storeCategoryService.getStoreCategory());
    }
}