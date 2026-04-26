package com.db.foodara.repository.order;

import com.db.foodara.entity.order.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {

    Optional<Cart> findFirstByUserIdOrderByUpdatedAtDesc(String userId);

    List<Cart> findByUserIdOrderByUpdatedAtDesc(String userId);
}
