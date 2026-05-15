package com.db.foodara.repository.settlement;

import com.db.foodara.entity.settlement.StoreSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreSettlementRepository extends JpaRepository<StoreSettlement, String> {
    Page<StoreSettlement> findByStatus(String status, Pageable pageable);
    Page<StoreSettlement> findByMerchantId(String merchantId, Pageable pageable);
}
