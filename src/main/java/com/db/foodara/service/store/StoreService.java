package com.db.foodara.service.store;

import com.db.foodara.dto.response.promotion.VoucherBestChoiceResponse;
import com.db.foodara.dto.response.promotion.VoucherPricingResponse;
import com.db.foodara.dto.response.store.*;
import com.db.foodara.entity.store.*;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.StoreOperatingHoursRepository;
import com.db.foodara.repository.store.*;
import com.db.foodara.service.promotion.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;
    private final StoreOperatingHoursRepository storeOperatingHoursRepository;
    private final VoucherService voucherService;

    // C06: GET /v1/stores/:id
    public StoreResponse getStoreById(String id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        return mapToStoreResponse(store);
    }

    // C07: GET /v1/stores/:id/menu-categories
    public List<MenuCategoryResponse> getMenuCategories(String storeId) {
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
    public List<MenuItemResponse> getMenuItems(String storeId, String userId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        Map<String, VoucherBestChoiceResponse> discountCache = new HashMap<>();

        return menuItemRepository.findByStoreIdAndIsActiveTrue(storeId).stream()
                .map(item -> mapToMenuItemResponse(item, resolveBestChoice(userId, storeId, item.getBasePrice(), discountCache)))
                .collect(Collectors.toList());
    }

    // GET /v1/stores/:id/menu-items-detail - includes option groups
    public List<MenuItemDetailResponse> getMenuItemsWithOptions(String storeId, String userId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        List<MenuItem> items = menuItemRepository.findByStoreIdAndIsActiveTrue(storeId);

        List<OptionGroup> allOptionGroups = optionGroupRepository.findByStoreIdOrderByDisplayOrder(storeId);
        Map<String, List<OptionGroup>> groupsByMenuItemId = new HashMap<>();

        if (!allOptionGroups.isEmpty()) {
            List<MenuItemOptionGroup> menuItemOptionGroups = menuItemOptionGroupRepository.findByMenuItemIdIn(
                    items.stream().map(MenuItem::getId).collect(Collectors.toList()));

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

        final Map<String, List<OptionItem>> optionsByGroupIdFinal;
        Map<String, List<OptionItem>> optionsByGroupId = new HashMap<>();
        if (!allOptionGroups.isEmpty()) {
            List<OptionItem> allOptions = optionItemRepository.findByOptionGroupIdInOrderByDisplayOrder(
                    allOptionGroups.stream().map(OptionGroup::getId).collect(Collectors.toList()));
            optionsByGroupId = allOptions.stream()
                    .collect(Collectors.groupingBy(OptionItem::getOptionGroupId));
        }
        optionsByGroupIdFinal = optionsByGroupId;

        Map<String, VoucherBestChoiceResponse> discountCache = new HashMap<>();

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
                    return mapToMenuItemDetailResponse(item, optionGroups, resolveBestChoice(userId, storeId, item.getBasePrice(), discountCache));
                })
                .collect(Collectors.toList());
    }

    // C06 docs alias: GET /v1/stores/:id/menu
    public List<MenuCategoryResponse> getMenu(String storeId) {
        return getMenuCategories(storeId);
    }

    // C07: GET /v1/menu-items/:id
    public MenuItemDetailResponse getMenuItemById(String id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        return mapToMenuItemDetailResponse(item, getOptionGroupsForMenuItem(item), null);
    }

    // C07: GET /v1/menu-items/:id/options
    public List<OptionGroupResponse> getMenuItemOptions(String id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        return getOptionGroupsForMenuItem(item);
    }

    // C06: GET /v1/stores/:id/operating-hours
    public List<OperatingHourResponse> getOperatingHours(String storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return storeOperatingHoursRepository.findByStoreId(storeId).stream()
                .map(this::mapToOperatingHourResponse)
                .collect(Collectors.toList());
    }

    // C06: GET /v1/stores/:id/combos
    public List<ComboResponse> getCombos(String storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        List<Combo> combos = comboRepository.findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId);
        if (combos.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> comboIds = combos.stream().map(Combo::getId).collect(Collectors.toList());
        Map<String, List<ComboItem>> itemsByComboId = comboItemRepository.findByComboIdIn(comboIds).stream()
                .collect(Collectors.groupingBy(ComboItem::getComboId));
        Map<String, MenuItem> menuItemsById = menuItemRepository.findByStoreId(storeId).stream()
                .collect(Collectors.toMap(MenuItem::getId, item -> item, (a, b) -> a));

        return combos.stream()
                .map(combo -> mapToComboResponse(combo, itemsByComboId.getOrDefault(combo.getId(), Collections.emptyList()), menuItemsById))
                .collect(Collectors.toList());
    }

    private List<OptionGroupResponse> getOptionGroupsForMenuItem(MenuItem item) {
        List<MenuItemOptionGroup> links = menuItemOptionGroupRepository.findByMenuItemId(item.getId());
        if (links.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> groupIds = links.stream()
                .map(MenuItemOptionGroup::getOptionGroupId)
                .collect(Collectors.toList());
        Map<String, OptionGroup> groupsById = optionGroupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(OptionGroup::getId, group -> group));
        Map<String, List<OptionItem>> optionsByGroupId = optionItemRepository.findByOptionGroupIdInOrderByDisplayOrder(groupIds).stream()
                .collect(Collectors.groupingBy(OptionItem::getOptionGroupId));

        return links.stream()
                .map(link -> groupsById.get(link.getOptionGroupId()))
                .filter(Objects::nonNull)
                .map(group -> OptionGroupResponse.builder()
                        .id(group.getId())
                        .storeId(group.getStoreId())
                        .name(group.getName())
                        .isRequired(group.getIsRequired())
                        .minSelections(group.getMinSelections())
                        .maxSelections(group.getMaxSelections())
                        .displayOrder(group.getDisplayOrder())
                        .options(optionsByGroupId.getOrDefault(group.getId(), Collections.emptyList()).stream()
                                .map(this::mapToOptionItemResponse)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    private OperatingHourResponse mapToOperatingHourResponse(com.db.foodara.entity.merchant.StoreOperatingHours h) {
        return OperatingHourResponse.builder()
                .id(h.getId())
                .storeId(h.getStoreId())
                .dayOfWeek(h.getDayOfWeek())
                .openTime(h.getOpenTime())
                .closeTime(h.getCloseTime())
                .isClosed(h.getIsClosed())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .build();
    }

    private ComboResponse mapToComboResponse(Combo combo, List<ComboItem> items, Map<String, MenuItem> menuItemsById) {
        List<ComboResponse.ComboItemResponse> itemResponses = items.stream()
                .map(item -> {
                    MenuItem menuItem = menuItemsById.get(item.getMenuItemId());
                    return ComboResponse.ComboItemResponse.builder()
                            .id(item.getId())
                            .menuItemId(item.getMenuItemId())
                            .menuItemName(menuItem != null ? menuItem.getName() : null)
                            .quantity(item.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        return ComboResponse.builder()
                .id(combo.getId())
                .storeId(combo.getStoreId())
                .name(combo.getName())
                .description(combo.getDescription())
                .comboPrice(combo.getComboPrice())
                .originalPrice(combo.getOriginalPrice())
                .isActive(combo.getIsActive())
                .startsAt(combo.getStartsAt())
                .endsAt(combo.getEndsAt())
                .items(itemResponses)
                .build();
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

    private MenuItemDetailResponse mapToMenuItemDetailResponse(MenuItem m, List<OptionGroupResponse> optionGroups, VoucherBestChoiceResponse bestChoice) {
        BigDecimal basePrice = amount(m.getBasePrice());
        BigDecimal discount = totalDiscount(bestChoice);
        return MenuItemDetailResponse.builder()
                .id(m.getId())
                .storeId(m.getStoreId())
                .categoryId(m.getCategoryId())
                .name(m.getName())
                .description(m.getDescription())
                .imageUrl(m.getImageUrl())
                .basePrice(basePrice)
                .discountedPrice(nonNegative(basePrice.subtract(discount)))
                .estimatedDiscountAmount(discount)
                .bestVoucher(bestSingleVoucher(bestChoice))
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
                .promotionText("Giam 15%")
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

    private MenuItemResponse mapToMenuItemResponse(MenuItem m, VoucherBestChoiceResponse bestChoice) {
        BigDecimal basePrice = amount(m.getBasePrice());
        BigDecimal discount = totalDiscount(bestChoice);
        return MenuItemResponse.builder()
                .id(m.getId())
                .storeId(m.getStoreId())
                .categoryId(m.getCategoryId())
                .name(m.getName())
                .description(m.getDescription())
                .imageUrl(m.getImageUrl())
                .basePrice(basePrice)
                .discountedPrice(nonNegative(basePrice.subtract(discount)))
                .estimatedDiscountAmount(discount)
                .bestVoucher(bestSingleVoucher(bestChoice))
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

    private VoucherBestChoiceResponse resolveBestChoice(String userId, String storeId, BigDecimal basePrice, Map<String, VoucherBestChoiceResponse> cache) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        BigDecimal amount = amount(basePrice);
        String cacheKey = amount.toPlainString();
        VoucherBestChoiceResponse existing = cache.get(cacheKey);
        if (existing != null || cache.containsKey(cacheKey)) {
            return existing;
        }
        VoucherBestChoiceResponse computed = voucherService.getBestVoucherForStore(userId, storeId, amount);
        cache.put(cacheKey, computed);
        return computed;
    }

    private BigDecimal totalDiscount(VoucherBestChoiceResponse bestChoice) {
        if (bestChoice == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount(bestChoice.getTotalDiscount());
    }

    private VoucherPricingResponse bestSingleVoucher(VoucherBestChoiceResponse bestChoice) {
        if (bestChoice == null) {
            return null;
        }
        VoucherPricingResponse platform = bestChoice.getPlatformVoucher();
        VoucherPricingResponse store = bestChoice.getStoreVoucher();
        if (platform == null) {
            return store;
        }
        if (store == null) {
            return platform;
        }
        return amount(platform.getPotentialDiscount()).compareTo(amount(store.getPotentialDiscount())) >= 0 ? platform : store;
    }

    private BigDecimal amount(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nonNegative(BigDecimal value) {
        BigDecimal normalized = amount(value);
        return normalized.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : normalized;
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
                .customerName(Boolean.TRUE.equals(r.getIsAnonymous()) ? "An danh" : null)
                .build();
    }
}
