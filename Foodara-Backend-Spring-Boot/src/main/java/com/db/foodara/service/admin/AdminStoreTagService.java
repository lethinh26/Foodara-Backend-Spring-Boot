package com.db.foodara.service.admin;

import com.db.foodara.dto.request.admin.StoreTagRequest;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminStoreTagResponse;
import com.db.foodara.entity.store.StoreTag;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.store.StoreTagRepository;
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
public class AdminStoreTagService {

    private final StoreTagRepository storeTagRepository;

    public PageResponse<AdminStoreTagResponse> getTags(int page, int size, String search, Boolean isActive) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("displayOrder").ascending().and(Sort.by("createdAt").descending()));
        Page<StoreTag> tagPage;

        if (search != null && !search.isBlank()) {
            tagPage = storeTagRepository.searchTags(search.trim(), pageRequest);
        } else if (isActive != null) {
            tagPage = storeTagRepository.findByIsActive(isActive, pageRequest);
        } else {
            tagPage = storeTagRepository.findAll(pageRequest);
        }

        List<AdminStoreTagResponse> content = tagPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AdminStoreTagResponse>builder()
                .content(content)
                .page(tagPage.getNumber())
                .number(tagPage.getNumber())
                .size(tagPage.getSize())
                .totalElements(tagPage.getTotalElements())
                .totalPages(tagPage.getTotalPages())
                .last(tagPage.isLast())
                .build();
    }

    @Transactional
    public AdminStoreTagResponse createTag(StoreTagRequest request) {
        if (storeTagRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        StoreTag tag = new StoreTag();
        mapRequestToEntity(request, tag);
        tag = storeTagRepository.save(tag);

        return mapToResponse(tag);
    }

    @Transactional
    public AdminStoreTagResponse updateTag(String id, StoreTagRequest request) {
        StoreTag tag = storeTagRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        if (!tag.getName().equals(request.getName()) && storeTagRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        mapRequestToEntity(request, tag);
        tag = storeTagRepository.save(tag);

        return mapToResponse(tag);
    }

    @Transactional
    public void deleteTag(String id) {
        StoreTag tag = storeTagRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        
        storeTagRepository.delete(tag);
    }

    // --- Mappers ---

    private void mapRequestToEntity(StoreTagRequest req, StoreTag tag) {
        tag.setName(req.getName());
        tag.setSlug(req.getSlug());
        tag.setTagType(req.getTagType());
        tag.setIconUrl(req.getIconUrl());
        tag.setColorHex(req.getColorHex());
        if (req.getDisplayOrder() != null) tag.setDisplayOrder(req.getDisplayOrder());
        if (req.getIsActive() != null) tag.setIsActive(req.getIsActive());
    }

    private AdminStoreTagResponse mapToResponse(StoreTag tag) {
        return AdminStoreTagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .tagType(tag.getTagType())
                .iconUrl(tag.getIconUrl())
                .colorHex(tag.getColorHex())
                .displayOrder(tag.getDisplayOrder())
                .isActive(tag.getIsActive())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
