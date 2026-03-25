package com.db.foodara.service.store;

import com.db.foodara.dto.request.store.StoreCategoryCreateDto;
import com.db.foodara.dto.request.store.StoreCategoryUpdateDto;
import com.db.foodara.entity.store.StoreCategory;
import com.db.foodara.repository.store.StoreCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoreCategoryService {
    @Autowired
    private StoreCategoryRepository storeCategoryRepository;

    public StoreCategory createStoreCategory(StoreCategoryCreateDto request) {
        if (storeCategoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Store category existed");
        }

        StoreCategory storeCategory = new StoreCategory();
        storeCategory.setName(request.getName());
        storeCategory.setSlug(request.getSlug());
        storeCategory.setIconUrl(request.getIconUrl());
        storeCategory.setDisplayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder());
        storeCategory.setActive(request.getIsActive() == null || request.getIsActive());
        storeCategory.setCreatedAt(LocalDateTime.now());

        return storeCategoryRepository.save(storeCategory);
    }

    public List<StoreCategory> getStoreCategory() {
        return storeCategoryRepository.findAll();
    }

    public StoreCategory getStoreCategory(String storeCategoryId) {
        return storeCategoryRepository.findById(storeCategoryId)
                .orElseThrow(() -> new RuntimeException("Store category not found"));
    }

    public StoreCategory updateStoreCategory(String storeCategoryId, StoreCategoryUpdateDto request) {
        StoreCategory storeCategory = storeCategoryRepository.findById(storeCategoryId)
                .orElseThrow(() -> new RuntimeException("Store category not found"));

        if (request.getName() != null
                && !request.getName().equals(storeCategory.getName())
                && storeCategoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Store category existed");
        }

        storeCategory.setName(request.getName());
        storeCategory.setSlug(request.getSlug());
        storeCategory.setIconUrl(request.getIconUrl());
        storeCategory.setDisplayOrder(request.getDisplayOrder());
        storeCategory.setActive(request.getIsActive());

        return storeCategoryRepository.save(storeCategory);
    }

    public void deleteStoreCategory(String storeCategoryId) {
        storeCategoryRepository.deleteById(storeCategoryId);
    }
}