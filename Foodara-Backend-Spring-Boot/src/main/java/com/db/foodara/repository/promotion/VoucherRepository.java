package com.db.foodara.repository.promotion;

import com.db.foodara.entity.promotion.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
            "AND (v.startsAt IS NULL OR v.startsAt <= :now) " +
            "AND (v.expiresAt IS NULL OR v.expiresAt >= :now) " +
            "AND (v.storeId = :storeId OR (v.storeId IS NULL AND (v.voucherType = 'platform' OR v.merchantId = :merchantId)))")
    List<Voucher> findAvailableByStore(@Param("storeId") String storeId,
                                       @Param("merchantId") String merchantId,
                                       @Param("now") LocalDateTime now);

    // Admin queries
    Page<Voucher> findByVoucherType(String voucherType, Pageable pageable);

    Page<Voucher> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE " +
            "LOWER(v.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Voucher> searchVouchers(@Param("search") String search, Pageable pageable);
}

