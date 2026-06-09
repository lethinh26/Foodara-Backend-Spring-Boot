package com.db.foodara.service.home;

import com.db.foodara.dto.response.home.BannerResponse;
import com.db.foodara.dto.response.home.CampaignResponse;
import com.db.foodara.dto.response.store.MenuItemResponse;
import com.db.foodara.dto.response.store.StoreResponse;
import com.db.foodara.entity.home.Banner;
import com.db.foodara.entity.home.Campaign;
import com.db.foodara.entity.store.MenuItem;
import com.db.foodara.entity.store.Store;
import com.db.foodara.repository.home.BannerRepository;
import com.db.foodara.repository.home.CampaignRepository;
import com.db.foodara.repository.store.MenuItemRepository;
import com.db.foodara.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final BannerRepository bannerRepository;
    private final CampaignRepository campaignRepository;
    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;

    // C04: GET /v1/home/banners
    public List<BannerResponse> getBanners() {
        LocalDateTime now = LocalDateTime.now();
        return bannerRepository.findActiveBanners(now).stream()
                .map(this::mapToBannerResponse)
                .collect(Collectors.toList());
    }

    // C04: GET /v1/home/nearby-stores
    public List<StoreResponse> getNearbyStores(BigDecimal lat, BigDecimal lng, int limit) {
        if (lat == null || lng == null) {
            return Collections.emptyList();
        }
        List<Store> stores = storeRepository.findNearbyStores(lat, lng, limit);
        return stores.stream()
                .map(s -> mapToStoreResponse(s, lat, lng))
                .collect(Collectors.toList());
    }

    // C04: GET /v1/home/popular-stores
    public List<StoreResponse> getPopularStores(int limit) {
        List<Store> stores = storeRepository.findPopularStores(PageRequest.of(0, limit));
        return stores.stream()
                .map(this::mapToStoreResponse)
                .collect(Collectors.toList());
    }

    // C04: GET /v1/home/promotions (stores with active campaigns/vouchers)
    public List<StoreResponse> getPromotionStores(int limit) {
        List<Store> stores = storeRepository.findStoresWithMostOrders(PageRequest.of(0, limit));
        return stores.stream()
                .map(this::mapToStoreResponse)
                .collect(Collectors.toList());
    }

    // C04: GET /v1/home/recommendations
    public List<StoreResponse> getRecommendations(int limit) {
        // For now, return popular stores as recommendations
        // In production, this would use user order history
        return getPopularStores(limit);
    }

    // C04: GET /v1/home/flash-deals
    public List<CampaignResponse> getFlashDeals() {
        LocalDateTime now = LocalDateTime.now();
        return campaignRepository.findActiveByType("flash_sale", now).stream()
                .map(this::mapToCampaignResponse)
                .collect(Collectors.toList());
    }

    // C04: GET /v1/home/campaigns/:id
    public CampaignResponse getCampaignById(String id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new com.db.foodara.exception.AppException(
                        com.db.foodara.exception.ErrorCode.INVALID_KEY));
        return mapToCampaignResponse(campaign);
    }

    // C04: GET /v1/home/campaigns
    public List<CampaignResponse> getActiveCampaigns() {
        LocalDateTime now = LocalDateTime.now();
        return campaignRepository.findActiveCampaigns(now).stream()
                .map(this::mapToCampaignResponse)
                .collect(Collectors.toList());
    }

    // --- Private mapping methods ---

    private BannerResponse mapToBannerResponse(Banner b) {
        return BannerResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .imageUrl(b.getImageUrl())
                .targetUrl(b.getTargetUrl())
                .targetType(b.getTargetType())
                .targetId(b.getTargetId())
                .position(b.getPosition())
                .displayOrder(b.getDisplayOrder())
                .isActive(b.getIsActive())
                .startsAt(b.getStartsAt())
                .endsAt(b.getEndsAt())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private CampaignResponse mapToCampaignResponse(Campaign c) {
        return CampaignResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .campaignType(c.getCampaignType())
                .bannerUrl(c.getBannerUrl())
                .isActive(c.getIsActive())
                .startsAt(c.getStartsAt())
                .endsAt(c.getEndsAt())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private StoreResponse mapToStoreResponse(Store s) {
        return mapToStoreResponse(s, null, null);
    }

    private StoreResponse mapToStoreResponse(Store s, BigDecimal userLat, BigDecimal userLng) {
        StoreResponse.StoreResponseBuilder builder = StoreResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .slug(s.getSlug())
                .description(s.getDescription())
                .phone(s.getPhone())
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

        // Calculate distance if user location provided
        if (userLat != null && userLng != null && s.getLatitude() != null && s.getLongitude() != null) {
            BigDecimal distance = calculateDistance(
                    userLat.doubleValue(), userLng.doubleValue(),
                    s.getLatitude().doubleValue(), s.getLongitude().doubleValue());
            builder.distance(distance.setScale(1, RoundingMode.HALF_UP));
            builder.estimatedDeliveryTime(estimateDeliveryTime(distance));
            builder.deliveryFee(calculateDeliveryFee(distance));
        }

        // For demo purposes - these would be calculated from actual promotions
        builder.hasPromotion(s.getTotalOrders() != null && s.getTotalOrders() > 10);
        builder.promotionText("Giảm 15%");
        builder.isNew(false);
        builder.isFeatured(s.getAvgRating() != null && s.getAvgRating().compareTo(BigDecimal.valueOf(4.5)) > 0);

        return builder.build();
    }

    private BigDecimal calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Haversine formula
        final double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(R * c);
    }

    private int estimateDeliveryTime(BigDecimal distanceKm) {
        // Rough estimate: 5km in 15 min + 3 min per additional km
        if (distanceKm == null) return 30;
        double km = distanceKm.doubleValue();
        return (int) Math.max(15, 15 + (km - 2) * 3);
    }

    private BigDecimal calculateDeliveryFee(BigDecimal distanceKm) {
        if (distanceKm == null) return BigDecimal.valueOf(15000);
        double km = distanceKm.doubleValue();
        if (km <= 2) return BigDecimal.valueOf(15000);
        return BigDecimal.valueOf(15000 + (km - 2) * 5000).setScale(0, RoundingMode.HALF_UP);
    }
}