package com.db.foodara.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.foodara.entity.order.OrderStatusHistory;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {
    /** Newest first — handy for "latest event" widgets. */
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(String orderId);

    /** Oldest first — handy for timeline rendering. */
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(String orderId);
}