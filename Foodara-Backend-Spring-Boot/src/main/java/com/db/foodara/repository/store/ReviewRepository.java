package com.db.foodara.repository.store;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.db.foodara.entity.store.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByStoreIdAndStatusOrderByCreatedAtDesc(String storeId, String status);

    Page<Review> findByStoreIdAndStatusOrderByCreatedAtDesc(String storeId, String status, Pageable pageable);

    Optional<Review> findByOrderIdAndUserId(String orderId, String userId);

    boolean existsByOrderIdAndUserId(String orderId, String userId);

    /** Average store rating for a store (only active reviews). */
    @Query("SELECT COALESCE(AVG(CAST(r.storeRating AS double)), 0) FROM Review r " +
            "WHERE r.storeId = :storeId AND r.status = 'active' AND r.storeRating IS NOT NULL")
    Double avgStoreRating(@Param("storeId") String storeId);

    /** Count of active reviews for a store. */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.storeId = :storeId AND r.status = 'active' AND r.storeRating IS NOT NULL")
    long countActiveByStoreId(@Param("storeId") String storeId);

    /** Count reviews by store, status, and rating for breakdown. */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.storeId = :storeId AND r.status = 'active' AND r.storeRating = :rating")
    long countByStoreIdAndStatusAndRating(@Param("storeId") String storeId, @Param("rating") Short rating);

    // Admin queries
    Page<Review> findByStatus(String status, Pageable pageable);
    Page<Review> findByStoreId(String storeId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:rating IS NULL OR r.storeRating = :rating) AND " +
           "(:search IS NULL OR LOWER(r.storeComment) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Review> findByFilters(
            @Param("status") String status,
            @Param("rating") Short rating,
            @Param("search") String search,
            Pageable pageable);
}
