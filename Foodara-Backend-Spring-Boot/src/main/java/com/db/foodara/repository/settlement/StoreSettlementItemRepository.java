package com.db.foodara.repository.settlement;

import com.db.foodara.entity.settlement.StoreSettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreSettlementItemRepository extends JpaRepository<StoreSettlementItem, String> {
    List<StoreSettlementItem> findBySettlementId(String settlementId);
}
