package com.db.foodara.service.store;

import com.db.foodara.dto.request.store.CreateReviewRequest;
import com.db.foodara.dto.response.store.CustomerReviewResponse;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.store.Review;
import com.db.foodara.entity.store.ReviewItem;
import com.db.foodara.entity.store.Store;
import com.db.foodara.entity.user.User;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.store.MenuItemRepository;
import com.db.foodara.repository.store.ReviewItemRepository;
import com.db.foodara.repository.store.ReviewRepository;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    private static final Set<String> REVIEWABLE_STATUSES = Set.of("delivered", "completed");

    @Transactional
    public CustomerReviewResponse createReview(String userId, CreateReviewRequest request) {
        // Validate order belongs to user
        Order order = orderRepository.findByIdAndCustomerId(request.getOrderId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Only delivered/completed orders can be reviewed
        if (!REVIEWABLE_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        // Check duplicate
        if (reviewRepository.existsByOrderIdAndUserId(request.getOrderId(), userId)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // Create review
        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setUserId(userId);
        review.setStoreId(order.getStoreId());
        review.setStoreRating(request.getStoreRating() != null ? request.getStoreRating().shortValue() : null);
        review.setStoreComment(request.getStoreComment());
        review.setDriverId(order.getDriverId());
        review.setDriverRating(request.getDriverRating() != null ? request.getDriverRating().shortValue() : null);
        review.setDriverComment(request.getDriverComment());
        review.setIsAnonymous(Boolean.TRUE.equals(request.getIsAnonymous()));

        Review saved = reviewRepository.save(review);

        // Save review items (per-menu-item ratings)
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (CreateReviewRequest.ReviewItemRequest itemReq : request.getItems()) {
                if (itemReq.getMenuItemId() == null || itemReq.getRating() == null) continue;
                ReviewItem ri = new ReviewItem();
                ri.setReviewId(saved.getId());
                ri.setMenuItemId(itemReq.getMenuItemId());
                ri.setRating(itemReq.getRating().shortValue());
                ri.setComment(itemReq.getComment());
                reviewItemRepository.save(ri);
            }
        }

        // Update store avg_rating and total_ratings
        recalculateStoreRating(order.getStoreId());

        return mapToResponse(saved, userId);
    }

    public CustomerReviewResponse getReview(String userId, String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return mapToResponse(review, userId);
    }

    /** Check if a user has already reviewed a specific order. */
    public CustomerReviewResponse getReviewByOrder(String userId, String orderId) {
        Review review = reviewRepository.findByOrderIdAndUserId(orderId, userId).orElse(null);
        if (review == null) return null;
        return mapToResponse(review, userId);
    }

    /**
     * Public listing: reviews for a store visible to customers (status = active).
     */
    public List<CustomerReviewResponse> getStoreReviews(String storeId) {
        List<Review> reviews = reviewRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, "active");
        return reviews.stream().map(r -> mapToPublicResponse(r)).collect(Collectors.toList());
    }

    // ============ Internal helpers ============

    private void recalculateStoreRating(String storeId) {
        try {
            Double avg = reviewRepository.avgStoreRating(storeId);
            long count = reviewRepository.countActiveByStoreId(storeId);
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                store.setAvgRating(BigDecimal.valueOf(avg != null ? avg : 0).setScale(1, RoundingMode.HALF_UP));
                store.setTotalRatings((int) count);
                storeRepository.save(store);
            }
        } catch (Exception e) {
            log.warn("Failed to recalculate store rating for {}: {}", storeId, e.getMessage());
        }
    }

    private CustomerReviewResponse mapToResponse(Review r, String userId) {
        List<ReviewItem> items = reviewItemRepository.findByReviewId(r.getId());
        Map<String, String> menuItemNames = loadMenuItemNames(items);

        return CustomerReviewResponse.builder()
                .id(r.getId())
                .orderId(r.getOrderId())
                .storeId(r.getStoreId())
                .storeRating(r.getStoreRating() != null ? r.getStoreRating().intValue() : null)
                .storeComment(r.getStoreComment())
                .driverRating(r.getDriverRating() != null ? r.getDriverRating().intValue() : null)
                .driverComment(r.getDriverComment())
                .isAnonymous(r.getIsAnonymous())
                .status(r.getStatus())
                .items(items.stream().map(ri -> CustomerReviewResponse.ItemResponse.builder()
                        .menuItemId(ri.getMenuItemId())
                        .menuItemName(menuItemNames.getOrDefault(ri.getMenuItemId(), ""))
                        .rating(ri.getRating() != null ? ri.getRating().intValue() : null)
                        .comment(ri.getComment())
                        .build()).toList())
                .createdAt(r.getCreatedAt())
                .build();
    }

    /** Public view: include customer name/avatar, hide userId. */
    private CustomerReviewResponse mapToPublicResponse(Review r) {
        String customerName = "Ẩn danh";
        String customerAvatar = null;
        if (!Boolean.TRUE.equals(r.getIsAnonymous()) && r.getUserId() != null) {
            User user = userRepository.findById(r.getUserId()).orElse(null);
            if (user != null) {
                customerName = user.getFullName() != null ? user.getFullName() : "Khách hàng";
                customerAvatar = user.getAvatarUrl();
            }
        }

        return CustomerReviewResponse.builder()
                .id(r.getId())
                .storeRating(r.getStoreRating() != null ? r.getStoreRating().intValue() : null)
                .storeComment(r.getStoreComment())
                .customerName(customerName)
                .customerAvatar(customerAvatar)
                .isAnonymous(r.getIsAnonymous())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private Map<String, String> loadMenuItemNames(List<ReviewItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyMap();
        Set<String> ids = items.stream()
                .map(ReviewItem::getMenuItemId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        if (ids.isEmpty()) return Collections.emptyMap();
        return menuItemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        com.db.foodara.entity.store.MenuItem::getId,
                        m -> m.getName() != null ? m.getName() : "",
                        (a, b) -> a));
    }
}
