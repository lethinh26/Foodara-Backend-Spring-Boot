package com.db.foodara.repository.store;

import com.db.foodara.entity.store.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByStoreIdAndStatusOrderByCreatedAtDesc(String storeId, String status);

    // Admin queries
    Page<Review> findByStatus(String status, Pageable pageable);

    Page<Review> findByStoreId(String storeId, Pageable pageable);
}
