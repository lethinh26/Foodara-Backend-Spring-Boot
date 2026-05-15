package com.db.foodara.repository.store;

import com.db.foodara.entity.store.ReviewItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewItemRepository extends JpaRepository<ReviewItem, String> {
    List<ReviewItem> findByReviewId(String reviewId);
}
