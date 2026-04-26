package com.db.foodara.repository.order;

import com.db.foodara.entity.order.CartItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemOptionRepository extends JpaRepository<CartItemOption, String> {

    List<CartItemOption> findByCartItemIdOrderByCreatedAtAsc(String cartItemId);

    List<CartItemOption> findByCartItemIdIn(List<String> cartItemIds);

    void deleteByCartItemId(String cartItemId);
}
