package com.db.foodara.service.store;

import com.db.foodara.dto.response.store.*;
import com.db.foodara.entity.store.*;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.store.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final ReviewRepository reviewRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final OptionItemRepository optionItemRepository;
    private final MenuItemOptionGroupRepository menuItemOptionGroupRepository;

    // C06: GET /v1/stores/:id
    public StoreResponse getStoreById(String id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        return mapToStoreResponse(store);
    }

    // C07: GET /v1/stores/:id/menu-categories
    public List<MenuCategoryResponse> getMenuCategories(String storeId) {
        // Verify store exists
        storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        List<MenuCategory> categories = menuCategoryRepository
                .findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId);

        List<MenuItem> allItems = menuItemRepository.findByStoreIdAndIsActiveTrue(storeId);

        return categories.stream()
                .map(cat -> {
                    int itemCount = (int) allItems.stream()
                            .filter(item -> cat.getId().equals(item.getCategoryId()))
                            .count();
                    return mapToMenuCategoryResponse(cat, itemCount);
                })
                .collect(Collectors.toList());
    }

    // C07: GET /v1/stores/:id/menu-items
    public List<MenuItemResponse> getMenuItems(String storeId) {
        // Verify store exists
        storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return menuItemRepository.findByStoreIdAndIsActiveTrue(storeId).stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    // GET /v1/stores/:id/menu-items-detail - includes option groups
    public List<MenuItemDetailResponse> getMenuItemsWithOptions(String storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        List<MenuItem> items = menuItemRepository.findByStoreIdAndIsActiveTrue(storeId);

        // Get all option groups for this store
        List<OptionGroup> allOptionGroups = optionGroupRepository.findByStoreIdOrderByDisplayOrder(storeId);
        Map<String, List<OptionGroup>> groupsByMenuItemId = new HashMap<>();

        if (!allOptionGroups.isEmpty()) {
            List<String> groupIds = allOptionGroups.stream().map(OptionGroup::getId).collect(Collectors.toList());
            List<MenuItemOptionGroup> menuItemOptionGroups = menuItemOptionGroupRepository.findByMenuItemIdIn(
                    items.stream().map(MenuItem::getId).collect(Collectors.toList()));

            // Map option groups to menu items
            Map<String, List<String>> groupIdsByMenuItemId = menuItemOptionGroups.stream()
                    .collect(Collectors.groupingBy(
                            MenuItemOptionGroup::getMenuItemId,
                            Collectors.mapping(MenuItemOptionGroup::getOptionGroupId, Collectors.toList())
                    ));

            for (Map.Entry<String, List<String>> entry : groupIdsByMenuItemId.entrySet()) {
                List<OptionGroup> groups = entry.getValue().stream()
                        .map(id -> allOptionGroups.stream().filter(g -> g.getId().equals(id)).findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                groupsByMenuItemId.put(entry.getKey(), groups);
            }
        }

        // Get all option items for these groups
        final Map<String, List<OptionItem>> optionsByGroupIdFinal;
        Map<String, List<OptionItem>> optionsByGroupId = new HashMap<>();
        if (!allOptionGroups.isEmpty()) {
            List<OptionItem> allOptions = optionItemRepository.findByOptionGroupIdInOrderByDisplayOrder(
                    allOptionGroups.stream().map(OptionGroup::getId).collect(Collectors.toList()));
            optionsByGroupId = allOptions.stream()
                    .collect(Collectors.groupingBy(OptionItem::getOptionGroupId));
        }
        optionsByGroupIdFinal = optionsByGroupId;

        return items.stream()
                .map(item -> {
                    List<OptionGroupResponse> optionGroups = new ArrayList<>();
                    List<OptionGroup> groups = groupsByMenuItemId.getOrDefault(item.getId(), Collections.emptyList());
                    for (OptionGroup group : groups) {
                        List<OptionItemResponse> options = optionsByGroupIdFinal.getOrDefault(group.getId(), Collections.emptyList())
                                .stream()
                                .map(this::mapToOptionItemResponse)
                                .collect(Collectors.toList());

                        optionGroups.add(OptionGroupResponse.builder()
                                .id(group.getId())
                                .storeId(group.getStoreId())
                                .name(group.getName())
                                .isRequired(group.getIsRequired())
                                .minSelections(group.getMinSelections())
                                .maxSelections(group.getMaxSelections())
                                .displayOrder(group.getDisplayOrder())
                                .options(options)
                                .build());
                    }
                    return mapToMenuItemDetailResponse(item, optionGroups);
                })
                .collect(Collectors.toList());
    }

    private OptionItemResponse mapToOptionItemResponse(OptionItem o) {
        return OptionItemResponse.builder()
                .id(o.getId())
                .optionGroupId(o.getOptionGroupId())
                .name(o.getName())
                .priceAdjustment(o.getPriceAdjustment())
                .isAvailable(o.getIsAvailable())
                .isDefault(o.getIsDefault())
                .displayOrder(o.getDisplayOrder())
                .build();
    }

    private MenuItemDetailResponse mapToMenuItemDetailResponse(MenuItem m, List<OptionGroupResponse> optionGroups) {
        return MenuItemDetailResponse.builder()
                .id(m.getId())
                .storeId(m.getStoreId())
                .categoryId(m.getCategoryId())
                .name(m.getName())
                .description(m.getDescription())
                .imageUrl(m.getImageUrl())
                .basePrice(m.getBasePrice())
                .isAvailable(m.getIsAvailable())
                .isActive(m.getIsActive())
                .isPopular(m.getIsPopular())
                .isNew(m.getIsNew())
                .displayOrder(m.getDisplayOrder())
                .avgRating(m.getAvgRating())
                .totalRatings(m.getTotalRatings())
                .totalSold(m.getTotalSold())
                .maxQuantityPerOrder(m.getMaxQuantityPerOrder())
                .createdAt(m.getCreatedAt())
                .optionGroups(optionGroups)
                .build();
    }

    private StoreResponse mapToStoreResponse(Store s) {
        return StoreResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .slug(s.getSlug())
                .description(s.getDescription())
                .phone(s.getPhone())
                .addressLine(s.getAddressLine())
                .ward(s.getWard())
                .districtId(s.getDistrictId())
                .cityId(s.getCityId())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .serviceZoneId(s.getServiceZoneId())
                .isOpen(s.getIsOpen())
                .isActive(s.getIsActive())
                .avgPreparationTime(s.getAvgPreparationTime())
                .minOrderAmount(s.getMinOrderAmount())
                .avgRating(s.getAvgRating())
                .totalRatings(s.getTotalRatings())
                .totalOrders(s.getTotalOrders())
                .coverImageUrl(s.getCoverImageUrl())
                .logoUrl(s.getLogoUrl())
                .createdAt(s.getCreatedAt())
                .hasPromotion(s.getTotalOrders() != null && s.getTotalOrders() > 10)
                .promotionText("Giảm 15%")
                .isNew(false)
                .isFeatured(s.getAvgRating() != null && s.getAvgRating().compareTo(BigDecimal.valueOf(4.5)) > 0)
                .build();
    }

    private MenuCategoryResponse mapToMenuCategoryResponse(MenuCategory c, int itemCount) {
        return MenuCategoryResponse.builder()
                .id(c.getId())
                .storeId(c.getStoreId())
                .name(c.getName())
                .description(c.getDescription())
                .displayOrder(c.getDisplayOrder())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .itemCount(itemCount)
                .build();
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem m) {
        return MenuItemResponse.builder()
                .id(m.getId())
                .storeId(m.getStoreId())
                .categoryId(m.getCategoryId())
                .name(m.getName())
                .description(m.getDescription())
                .imageUrl(m.getImageUrl())
                .basePrice(m.getBasePrice())
                .isAvailable(m.getIsAvailable())
                .isActive(m.getIsActive())
                .isPopular(m.getIsPopular())
                .isNew(m.getIsNew())
                .displayOrder(m.getDisplayOrder())
                .avgRating(m.getAvgRating())
                .totalRatings(m.getTotalRatings())
                .totalSold(m.getTotalSold())
                .maxQuantityPerOrder(m.getMaxQuantityPerOrder())
                .createdAt(m.getCreatedAt())
                .build();
    }

    // C13: GET /v1/stores/:id/reviews
    public List<ReviewResponse> getReviews(String storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return reviewRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, "active").stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToReviewResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .orderId(r.getOrderId())
                .userId(r.getUserId())
                .storeId(r.getStoreId())
                .storeRating(r.getStoreRating())
                .storeComment(r.getStoreComment())
                .driverId(r.getDriverId())
                .driverRating(r.getDriverRating())
                .driverComment(r.getDriverComment())
                .isAnonymous(r.getIsAnonymous())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .customerName(Boolean.TRUE.equals(r.getIsAnonymous()) ? "Ẩn danh" : null)
                .build();
    }
}
