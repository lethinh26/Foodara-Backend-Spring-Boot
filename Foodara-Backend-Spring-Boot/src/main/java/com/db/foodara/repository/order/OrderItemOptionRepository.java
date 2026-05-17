package com.db.foodara.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.foodara.entity.order.OrderItemOption;

@Repository
public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, String> {

    List<OrderItemOption> findByOrderItem_IdOrderByCreatedAtAsc(String orderItemId);

    List<OrderItemOption> findByOrderItem_IdIn(List<String> orderItemIds);


    @org.springframework.data.jpa.repository.Query(
            "SELECT o FROM OrderItemOption o WHERE o.orderItem.order.id = :orderId ORDER BY o.createdAt ASC")
    List<OrderItemOption> findByOrderId(@org.springframework.data.repository.query.Param("orderId") String orderId);
}
