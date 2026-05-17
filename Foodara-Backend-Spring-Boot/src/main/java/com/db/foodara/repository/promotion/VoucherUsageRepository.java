package com.db.foodara.repository.promotion;

import com.db.foodara.entity.promotion.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, String> {

    List<VoucherUsage> findByOrderId(String orderId);

    List<VoucherUsage> findByVoucherIdOrderByUsedAtDesc(String voucherId);

    long countByVoucherId(String voucherId);

    void deleteByOrderId(String orderId);
}
