package com.db.foodara.repository.order;

import com.db.foodara.entity.order.OrderAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment, String> {
    Optional<OrderAssignment> findByOrderIdAndStatus(String orderId, String status);
}