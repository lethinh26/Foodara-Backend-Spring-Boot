package com.db.foodara.service.admin;

import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminReviewResponse;
import com.db.foodara.entity.driver.Driver;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.store.Review;
import com.db.foodara.entity.store.ReviewImage;
import com.db.foodara.entity.store.ReviewItem;
import com.db.foodara.entity.user.User;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.driver.DriverRepository;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.store.*;
import com.db.foodara.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final DriverRepository driverRepository;

    private static final Set<String> VALID_REVIEW_STATUSES = Set.of(
            "active", "hidden", "flagged", "deleted"
    );


    public PageResponse<AdminReviewResponse> getReviews(int page, int size, String status, String rating, String search) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage;

        Integer ratingInt = null;
        if (rating != null && !rating.isBlank()) {
            try { ratingInt = Integer.parseInt(rating); } catch (NumberFormatException ignored) {}
        }

        boolean hasFilter = (status != null && !status.isBlank())
                || (ratingInt != null)
                || (search != null && !search.isBlank());

        if (hasFilter) {
            Short ratingShort = ratingInt != null ? ratingInt.shortValue() : null;
            reviewPage = reviewRepository.findByFilters(
                    status != null && !status.isBlank() ? status : null,
                    ratingShort,
                    search != null && !search.isBlank() ? search : null,
                    pageRequest);
        } else {
            reviewPage = reviewRepository.findAll(pageRequest);
        }

        List<AdminReviewResponse> content = reviewPage.getContent().stream()
                .map(this::mapToListResponse)
                .toList();

        return PageResponse.<AdminReviewResponse>builder()
                .content(content)
                .page(reviewPage.getNumber())
                .number(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .build();
    }


    public AdminReviewResponse getReviewDetail(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        return mapToDetailResponse(review);
    }


    @Transactional
    public void updateReviewStatus(String reviewId, String newStatus) {
        if (!VALID_REVIEW_STATUSES.contains(newStatus)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        review.setStatus(newStatus);
        reviewRepository.save(review);

        log.info("Admin updated review {} status to {}", reviewId, newStatus);
    }


    /**
     * List view: enriched with user/store/driver/order names, but NO items/images
     */
    private AdminReviewResponse mapToListResponse(Review r) {
        // Load images (first 3 thumbnails for list view)
        List<AdminReviewResponse.ReviewImageDto> images = reviewImageRepository.findByReviewId(r.getId())
                .stream()
                .limit(3)
                .map(this::mapImageToDto)
                .toList();

        return buildBaseResponse(r)
                .images(images)
                .imageCount((long) images.size())
                .build();
    }

    /**
     * Detail view: includes items + images
     */
    private AdminReviewResponse mapToDetailResponse(Review r) {
        List<AdminReviewResponse.ReviewItemDto> items = reviewItemRepository.findByReviewId(r.getId())
                .stream()
                .map(this::mapItemToDto)
                .toList();

        List<AdminReviewResponse.ReviewImageDto> images = reviewImageRepository.findByReviewId(r.getId())
                .stream()
                .map(this::mapImageToDto)
                .toList();

        return buildBaseResponse(r)
                .items(items)
                .images(images)
                .imageCount((long) images.size())
                .build();
    }

    private AdminReviewResponse.AdminReviewResponseBuilder buildBaseResponse(Review r) {
        // Enrich user name
        String userName = null;
        if (r.getUserId() != null) {
            userName = userRepository.findById(r.getUserId())
                    .map(User::getFullName)
                    .orElse(null);
        }

        // Enrich store name
        String storeName = null;
        if (r.getStoreId() != null) {
            storeName = storeRepository.findById(r.getStoreId())
                    .map(s -> s.getName())
                    .orElse(null);
        }

        // Enrich driver name (driver_id references drivers table, not users)
        String driverName = null;
        if (r.getDriverId() != null) {
            driverName = driverRepository.findById(r.getDriverId())
                    .map(Driver::getFullName)
                    .orElse(null);
        }

        // Enrich order number
        String orderNumber = null;
        if (r.getOrderId() != null) {
            orderNumber = orderRepository.findById(r.getOrderId())
                    .map(Order::getOrderNumber)
                    .orElse(null);
        }

        return AdminReviewResponse.builder()
                .id(r.getId())
                .orderId(r.getOrderId())
                .orderNumber(orderNumber)
                .userId(r.getUserId())
                .userName(userName)
                .storeId(r.getStoreId())
                .storeName(storeName)
                .storeRating(r.getStoreRating())
                .storeComment(r.getStoreComment())
                .driverId(r.getDriverId())
                .driverName(driverName)
                .driverRating(r.getDriverRating())
                .driverComment(r.getDriverComment())
                .isAnonymous(r.getIsAnonymous())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt());
    }

    private AdminReviewResponse.ReviewItemDto mapItemToDto(ReviewItem item) {
        String menuItemName = null;
        if (item.getMenuItemId() != null) {
            menuItemName = menuItemRepository.findById(item.getMenuItemId())
                    .map(m -> m.getName())
                    .orElse(null);
        }

        return AdminReviewResponse.ReviewItemDto.builder()
                .id(item.getId())
                .reviewId(item.getReviewId())
                .menuItemId(item.getMenuItemId())
                .menuItemName(menuItemName)
                .rating(item.getRating())
                .comment(item.getComment())
                .build();
    }

    private AdminReviewResponse.ReviewImageDto mapImageToDto(ReviewImage image) {
        return AdminReviewResponse.ReviewImageDto.builder()
                .id(image.getId())
                .reviewId(image.getReviewId())
                .imageUrl(image.getImageUrl())
                .build();
    }
}
