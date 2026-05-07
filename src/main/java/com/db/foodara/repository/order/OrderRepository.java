package com.db.foodara.repository.order;

import com.db.foodara.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByStoreIdOrderByPlacedAtDesc(String storeId);
    Optional<Order> findById(String id);
    Optional<List<Order>> findByStoreId(String storeId);
    Optional<Order> findByIdAndStoreId(String id, String storeId);
    List<Order> findByStoreIdAndStatus(String storeId, String status);

    List<Order> getOrderByStoreId(String storeId);
    // Customer-facing queries
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    Optional<Order> findByIdAndCustomerId(String id, String customerId);
    Optional<Order> findByOrderNumber(String orderNumber);

    // Payment timeout: find orders pending payment past deadline
    List<Order> findByPaymentStatusAndPlacedAtBefore(String paymentStatus, java.time.LocalDateTime cutoff);
}