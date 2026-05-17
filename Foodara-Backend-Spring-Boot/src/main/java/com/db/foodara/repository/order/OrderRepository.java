package com.db.foodara.repository.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.db.foodara.entity.order.Order;

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

    // Merchant reports — aggregates scoped by store
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.storeId = :storeId AND UPPER(o.status) IN ('COMPLETED','DELIVERED')")
    java.math.BigDecimal sumRevenueByStore(@Param("storeId") String storeId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.storeId = :storeId")
    long countOrdersByStore(@Param("storeId") String storeId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.storeId = :storeId " +
            "AND UPPER(o.status) IN ('COMPLETED','DELIVERED')")
    long countCompletedOrdersByStore(@Param("storeId") String storeId);

    /**
     * Average preparation time in minutes (preparing_at -> ready_at) for completed/delivered orders.
     */
    @Query(value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (o.ready_at - o.preparing_at)) / 60.0), 0) " +
            "FROM orders o WHERE o.store_id = :storeId " +
            "AND o.preparing_at IS NOT NULL AND o.ready_at IS NOT NULL " +
            "AND UPPER(o.status) IN ('COMPLETED','DELIVERED','PICKED_UP','DELIVERING')",
            nativeQuery = true)
    Double avgPreparationMinutesByStore(@Param("storeId") String storeId);

    /**
     * Daily revenue + order count for a store between two dates (inclusive).
     */
    @Query(value = "SELECT TO_CHAR(DATE(COALESCE(o.completed_at, o.delivered_at, o.placed_at, o.created_at)), 'YYYY-MM-DD') AS day, " +
            "COALESCE(SUM(CASE WHEN UPPER(o.status) IN ('COMPLETED','DELIVERED') THEN o.total_amount ELSE 0 END), 0) AS revenue, " +
            "COUNT(*) AS orders " +
            "FROM orders o WHERE o.store_id = :storeId " +
            "AND COALESCE(o.completed_at, o.delivered_at, o.placed_at, o.created_at) >= :start " +
            "AND COALESCE(o.completed_at, o.delivered_at, o.placed_at, o.created_at) < :end " +
            "GROUP BY day ORDER BY day ASC",
            nativeQuery = true)
    List<Object[]> findDailyRevenueByStore(@Param("storeId") String storeId,
                                           @Param("start") java.time.LocalDateTime start,
                                           @Param("end") java.time.LocalDateTime end);

    // Admin queries
    Page<Order> findByStatus(String status, Pageable pageable);
    Page<Order> findByPaymentStatus(String paymentStatus, Pageable pageable);
    Page<Order> findByStatusAndPaymentStatus(String status, String paymentStatus, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
            "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.storeName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Order> searchOrders(@Param("search") String search, Pageable pageable);
}
