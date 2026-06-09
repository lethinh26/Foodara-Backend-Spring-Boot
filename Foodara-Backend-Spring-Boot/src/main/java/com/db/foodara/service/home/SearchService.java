package com.db.foodara.service.home;

import com.db.foodara.dto.response.home.SearchHistoryResponse;
import com.db.foodara.dto.response.store.MenuItemResponse;
import com.db.foodara.dto.response.store.StoreResponse;
import com.db.foodara.entity.home.SearchHistory;
import com.db.foodara.entity.store.MenuItem;
import com.db.foodara.entity.store.Store;
import com.db.foodara.repository.home.SearchHistoryRepository;
import com.db.foodara.repository.store.MenuItemRepository;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.repository.store.StoreStoreCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final StoreStoreCategoryRepository storeStoreCategoryRepository;

    // C05: GET /v1/search/stores
    public List<StoreResponse> searchStores(String query, String categoryId,
                                            BigDecimal minRating, BigDecimal maxDeliveryFee,
                                            Boolean hasPromotion, String sortBy,
                                            BigDecimal userLat, BigDecimal userLng,
                                            int page, int limit) {
        // For now, simple implementation
        // In production, would use JpaSpecificationExecutor for complex filtering
        List<Store> stores;

        // Get store IDs that match the category filter
        final List<String> storeIdsInCategory;
        if (categoryId != null && !categoryId.isBlank()) {
            storeIdsInCategory = storeStoreCategoryRepository.findStoreIdsByCategoryId(categoryId);
        } else {
            storeIdsInCategory = List.of();
        }

        if (query != null && !query.isBlank()) {
            stores = storeRepository.findAll().stream()
                    .filter(s -> s.getIsActive() && s.getIsOpen())
                    .filter(s -> s.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            stores = storeRepository.findByIsActiveTrueAndIsOpenTrue();
        }

        // Apply category filter
        if (categoryId != null && !categoryId.isBlank()) {
            final List<String> finalStoreIds = storeIdsInCategory;
            stores = stores.stream()
                    .filter(s -> finalStoreIds.contains(s.getId()))
                    .collect(Collectors.toList());
        }

        // Apply filters
        if (minRating != null) {
            stores = stores.stream()
                    .filter(s -> s.getAvgRating() != null && s.getAvgRating().compareTo(minRating) >= 0)
                    .collect(Collectors.toList());
        }

        if (hasPromotion != null && hasPromotion) {
            stores = stores.stream()
                    .filter(s -> s.getTotalOrders() != null && s.getTotalOrders() > 10)
                    .collect(Collectors.toList());
        }

        // Apply sorting
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "rating":
                    stores.sort((a, b) -> {
                        BigDecimal ra = a.getAvgRating() != null ? a.getAvgRating() : BigDecimal.ZERO;
                        BigDecimal rb = b.getAvgRating() != null ? b.getAvgRating() : BigDecimal.ZERO;
                        return rb.compareTo(ra);
                    });
                    break;
                case "popular":
                case "total_orders":
                    stores.sort((a, b) -> {
                        Integer na = a.getTotalOrders() != null ? a.getTotalOrders() : 0;
                        Integer nb = b.getTotalOrders() != null ? b.getTotalOrders() : 0;
                        return nb.compareTo(na);
                    });
                    break;
                case "distance":
                    if (userLat != null && userLng != null) {
                        stores.sort((a, b) -> {
                            Double da = calculateDistance(userLat.doubleValue(), userLng.doubleValue(),
                                    a.getLatitude() != null ? a.getLatitude().doubleValue() : 0,
                                    a.getLongitude() != null ? a.getLongitude().doubleValue() : 0);
                            Double db = calculateDistance(userLat.doubleValue(), userLng.doubleValue(),
                                    b.getLatitude() != null ? b.getLatitude().doubleValue() : 0,
                                    b.getLongitude() != null ? b.getLongitude().doubleValue() : 0);
                            return da.compareTo(db);
                        });
                    }
                    break;
            }
        }

        // Pagination
        int start = page * limit;
        if (start >= stores.size()) {
            return List.of();
        }
        int end = Math.min(start + limit, stores.size());
        stores = stores.subList(start, end);

        return stores.stream()
                .map(s -> mapToStoreResponse(s, userLat, userLng))
                .collect(Collectors.toList());
    }

    // C05: GET /v1/search/items
    public List<MenuItemResponse> searchItems(String query, int page, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        List<MenuItem> items = menuItemRepository.searchByNameOrDescription(query);
        int start = page * limit;
        if (start >= items.size()) {
            return List.of();
        }
        int end = Math.min(start + limit, items.size());
        return items.subList(start, end).stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    // C05: GET /v1/search/suggestions
    public List<String> getSuggestions(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        // Get unique store names that match
        List<String> storeNames = storeRepository.findByIsActiveTrueAndIsOpenTrue().stream()
                .filter(s -> s.getName().toLowerCase().contains(query.toLowerCase()))
                .map(Store::getName)
                .limit(limit / 2)
                .collect(Collectors.toList());

        // Get unique menu item names that match
        List<String> itemNames = menuItemRepository.searchByNameOrDescription(query).stream()
                .filter(i -> i.getIsActive() && Boolean.TRUE.equals(i.getIsAvailable()))
                .map(MenuItem::getName)
                .limit(limit / 2)
                .collect(Collectors.toList());

        // Combine and limit
        return (storeNames.size() + itemNames.size() <= limit)
                ? storeNames : storeNames.subList(0, Math.min(storeNames.size(), limit - itemNames.size()));
    }

    // C05: GET /v1/search/history
    public List<SearchHistoryResponse> getSearchHistory(String userId) {
        return searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToSearchHistoryResponse)
                .collect(Collectors.toList());
    }

    // C05: DELETE /v1/search/history
    @Transactional
    public void clearSearchHistory(String userId) {
        searchHistoryRepository.deleteByUserId(userId);
    }

    // C05: POST /v1/search/history (save search)
    @Transactional
    public SearchHistoryResponse saveSearchHistory(String userId, String query, String searchType) {
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setSearchQuery(query);
        history.setSearchType(searchType);
        SearchHistory saved = searchHistoryRepository.save(history);
        return mapToSearchHistoryResponse(saved);
    }

    // --- Private mapping methods ---

    private StoreResponse mapToStoreResponse(Store s, BigDecimal userLat, BigDecimal userLng) {
        StoreResponse.StoreResponseBuilder builder = StoreResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .slug(s.getSlug())
                .description(s.getDescription())
                .addressLine(s.getAddressLine())
                .ward(s.getWard())
                .districtName(s.getDistrictName())
                .cityName(s.getCityName())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .isOpen(s.getIsOpen())
                .isActive(s.getIsActive())
                .avgPreparationTime(s.getAvgPreparationTime())
                .minOrderAmount(s.getMinOrderAmount())
                .avgRating(s.getAvgRating())
                .totalRatings(s.getTotalRatings())
                .totalOrders(s.getTotalOrders())
                .coverImageUrl(s.getCoverImageUrl())
                .logoUrl(s.getLogoUrl())
                .createdAt(s.getCreatedAt());

        if (userLat != null && userLng != null && s.getLatitude() != null && s.getLongitude() != null) {
            BigDecimal distance = BigDecimal.valueOf(calculateDistance(
                    userLat.doubleValue(), userLng.doubleValue(),
                    s.getLatitude().doubleValue(), s.getLongitude().doubleValue()));
            builder.distance(distance);
        }

        builder.hasPromotion(s.getTotalOrders() != null && s.getTotalOrders() > 10);
        builder.isNew(false);
        builder.isFeatured(s.getAvgRating() != null && s.getAvgRating().compareTo(BigDecimal.valueOf(4.5)) > 0);

        return builder.build();
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
                .createdAt(m.getCreatedAt())
                .build();
    }

    private SearchHistoryResponse mapToSearchHistoryResponse(SearchHistory h) {
        return SearchHistoryResponse.builder()
                .id(h.getId())
                .userId(h.getUserId())
                .searchQuery(h.getSearchQuery())
                .searchType(h.getSearchType())
                .resultCount(h.getResultCount())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}