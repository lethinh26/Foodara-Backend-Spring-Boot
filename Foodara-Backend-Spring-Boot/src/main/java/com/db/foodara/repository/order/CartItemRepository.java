package com.db.foodara.repository.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.foodara.entity.order.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    List<CartItem> findByCartIdOrderByCreatedAtAsc(String cartId);

    Optional<CartItem> findByIdAndCartUserId(String id, String userId);

    long countByCartId(String cartId);
}
