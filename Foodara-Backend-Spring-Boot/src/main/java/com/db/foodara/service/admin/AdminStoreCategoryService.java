package com.db.foodara.service.admin;

import com.db.foodara.dto.request.admin.StoreCategoryRequest;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminStoreCategoryResponse;
import com.db.foodara.entity.store.StoreCategory;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.store.StoreCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStoreCategoryService {

    private final StoreCategoryRepository storeCategoryRepository;

    public PageResponse<AdminStoreCategoryResponse> getCategories(int page, int size, String search, Boolean isActive) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("displayOrder").ascending().and(Sort.by("createdAt").descending()));
        Page<StoreCategory> categoryPage;

        if (search != null && !search.isBlank()) {
            categoryPage = storeCategoryRepository.searchCategories(search.trim(), pageRequest);
        } else if (isActive != null) {
            categoryPage = storeCategoryRepository.findByIsActive(isActive, pageRequest);
        } else {
            categoryPage = storeCategoryRepository.findAll(pageRequest);
        }

        List<AdminStoreCategoryResponse> content = categoryPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AdminStoreCategoryResponse>builder()
                .content(content)
                .page(categoryPage.getNumber())
                .number(categoryPage.getNumber())
                .size(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .last(categoryPage.isLast())
                .build();
    }

    @Transactional
    public AdminStoreCategoryResponse createCategory(StoreCategoryRequest request) {
        if (storeCategoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_KEY); // Store category name exists
        }

        StoreCategory category = new StoreCategory();
        mapRequestToEntity(request, category);
        category = storeCategoryRepository.save(category);

        return mapToResponse(category);
    }

    @Transactional
    public AdminStoreCategoryResponse updateCategory(String id, StoreCategoryRequest request) {
        StoreCategory category = storeCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND)); // Generic not found

        if (!category.getName().equals(request.getName()) && storeCategoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        mapRequestToEntity(request, category);
        category = storeCategoryRepository.save(category);

        return mapToResponse(category);
    }

    @Transactional
    public void deleteCategory(String id) {
        StoreCategory category = storeCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        
        // Note: In a real scenario, check if it's used by any stores before deleting.
        // For now, we allow deletion as per requirement.
        storeCategoryRepository.delete(category);
    }

    // --- Mappers ---

    private void mapRequestToEntity(StoreCategoryRequest req, StoreCategory cat) {
        cat.setName(req.getName());
        cat.setSlug(req.getSlug());
        cat.setIconUrl(req.getIconUrl());
        if (req.getDisplayOrder() != null) cat.setDisplayOrder(req.getDisplayOrder());
        if (req.getIsActive() != null) cat.setActive(req.getIsActive());
    }

    private AdminStoreCategoryResponse mapToResponse(StoreCategory cat) {
        return AdminStoreCategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .iconUrl(cat.getIconUrl())
                .displayOrder(cat.getDisplayOrder())
                .isActive(cat.isActive())
                .createdAt(cat.getCreatedAt())
                .build();
    }
}
