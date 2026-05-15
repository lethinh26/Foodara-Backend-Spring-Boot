package com.db.foodara.repository.order;

import com.db.foodara.entity.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByStoreIdOrderByPlacedAtDesc(String storeId);
    Optional<Order> findById(String id);
    Optional<Order> findByIdAndStoreId(String id, String storeId);
    List<Order> findByStoreIdAndStatus(String storeId, String status);

    // Customer-facing queries
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    Optional<Order> findByIdAndCustomerId(String id, String customerId);
    Optional<Order> findByOrderNumber(String orderNumber);

    // Payment timeout: find orders pending payment past deadline
    List<Order> findByPaymentStatusAndPlacedAtBefore(String paymentStatus, java.time.LocalDateTime cutoff);

    // Admin queries
    Page<Order> findByStatus(String status, Pageable pageable);
    Page<Order> findByPaymentStatus(String paymentStatus, Pageable pageable);
    Page<Order> findByStatusAndPaymentStatus(String status, String paymentStatus, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
            "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.storeName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Order> searchOrders(@Param("search") String search, Pageable pageable);
}