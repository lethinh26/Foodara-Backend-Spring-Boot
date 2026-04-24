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
    Optional<Order> findByIdAndStoreId(String id, String storeId);
    List<Order> findByStoreIdAndStatus(String storeId, String status);
}