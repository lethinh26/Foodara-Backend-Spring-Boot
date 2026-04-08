package com.db.foodara.service.store;

import com.db.foodara.dto.request.store.StoreCategoryCreateDto;
import com.db.foodara.dto.request.store.StoreCategoryUpdateDto;
import com.db.foodara.entity.store.StoreCategory;
import com.db.foodara.repository.store.StoreCategoryRepository;
import com.db.foodara.dto.response.store.StoreCategoryResponse;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class StoreCategoryService {
    @Autowired
    private StoreCategoryRepository storeCategoryRepository;

    public StoreCategoryResponse createStoreCategory(StoreCategoryCreateDto request) {
        if (storeCategoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.STORE_CATEGORY_EXISTED);
        }

        StoreCategory storeCategory = new StoreCategory();
        storeCategory.setName(request.getName());
        storeCategory.setSlug(request.getSlug());
        storeCategory.setIconUrl(request.getIconUrl());
        storeCategory.setDisplayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder());
        storeCategory.setActive(request.getIsActive() == null || request.getIsActive());
        storeCategory.setCreatedAt(LocalDateTime.now());


        StoreCategory saved = storeCategoryRepository.save(storeCategory);
        return mapToResponse(saved);
    }

    public List<StoreCategoryResponse> getStoreCategory() {
        return storeCategoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public StoreCategoryResponse getStoreCategory(String storeCategoryId) {
        StoreCategory storeCategory = storeCategoryRepository.findById(storeCategoryId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_CATEGORY_NOT_FOUND));
        return mapToResponse(storeCategory);
    }

    public StoreCategoryResponse updateStoreCategory(String storeCategoryId, StoreCategoryUpdateDto request) {
        StoreCategory storeCategory = storeCategoryRepository.findById(storeCategoryId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_CATEGORY_NOT_FOUND));

        if (request.getName() != null
                && !request.getName().equals(storeCategory.getName())
                && storeCategoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.STORE_CATEGORY_EXISTED);
        }

        storeCategory.setName(request.getName());
        storeCategory.setSlug(request.getSlug());
        storeCategory.setIconUrl(request.getIconUrl());
        storeCategory.setDisplayOrder(request.getDisplayOrder());
        storeCategory.setActive(request.getIsActive());

        StoreCategory updated = storeCategoryRepository.save(storeCategory);
        return mapToResponse(updated);
    }

    public void deleteStoreCategory(String storeCategoryId) {
        storeCategoryRepository.deleteById(storeCategoryId);
    }

    private StoreCategoryResponse mapToResponse(StoreCategory c) {
        return StoreCategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .iconUrl(c.getIconUrl())
                .displayOrder(c.getDisplayOrder())
                .isActive(c.isActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}