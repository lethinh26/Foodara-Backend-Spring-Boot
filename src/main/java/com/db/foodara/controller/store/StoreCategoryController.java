package com.db.foodara.controller.store;

import com.db.foodara.dto.request.store.StoreCategoryCreateDto;
import com.db.foodara.dto.request.store.StoreCategoryUpdateDto;
import com.db.foodara.entity.store.StoreCategory;
import com.db.foodara.service.store.StoreCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/store-categories")
public class StoreCategoryController {
    @Autowired
    private StoreCategoryService storeCategoryService;

    @PostMapping
    StoreCategory createStoreCategory(@RequestBody @Valid StoreCategoryCreateDto request) {
        return storeCategoryService.createStoreCategory(request);
    }

    @GetMapping
    List<StoreCategory> getStoreCategory() {
        return storeCategoryService.getStoreCategory();
    }

    @GetMapping("/{storeCategoryId}")
    StoreCategory getStoreCategory(@PathVariable String storeCategoryId) {
        return storeCategoryService.getStoreCategory(storeCategoryId);
    }

    @PutMapping("/{storeCategoryId}")
    StoreCategory updateStoreCategory(@PathVariable String storeCategoryId,
                                      @RequestBody StoreCategoryUpdateDto request) {
        return storeCategoryService.updateStoreCategory(storeCategoryId, request);
    }

    @DeleteMapping("/{storeCategoryId}")
    String deleteStoreCategory(@PathVariable String storeCategoryId) {
        storeCategoryService.deleteStoreCategory(storeCategoryId);
        return "Store category has ben deleted";
    }
}