package com.db.foodara.repository.promotion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.db.foodara.entity.promotion.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
            "AND (v.startsAt IS NULL OR v.startsAt <= :now) " +
            "AND (v.expiresAt IS NULL OR v.expiresAt >= :now) " +
            "AND (v.storeId = :storeId OR (v.storeId IS NULL AND (v.voucherType = 'platform' OR v.merchantId = :merchantId)))")
    List<Voucher> findAvailableByStore(@Param("storeId") String storeId,
                                       @Param("merchantId") String merchantId,
                                       @Param("now") LocalDateTime now);

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
            "AND v.voucherType = 'platform' " +
            "AND (v.startsAt IS NULL OR v.startsAt <= :now) " +
            "AND (v.expiresAt IS NULL OR v.expiresAt >= :now)")
    List<Voucher> findActivePlatformVouchers(@Param("now") LocalDateTime now);

    // Merchant queries — vouchers owned by a merchant (store vouchers)
    List<Voucher> findByMerchantIdOrderByCreatedAtDesc(String merchantId);

    boolean existsByCodeIgnoreCase(String code);

    // Admin queries
    Page<Voucher> findByVoucherType(String voucherType, Pageable pageable);

    Page<Voucher> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE " +
            "LOWER(v.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Voucher> searchVouchers(@Param("search") String search, Pageable pageable);


    @Modifying
    @Query("UPDATE Voucher v SET v.usedQuantity = COALESCE(v.usedQuantity, 0) + 1 " +
            "WHERE v.id = :voucherId " +
            "AND (v.totalQuantity IS NULL OR COALESCE(v.usedQuantity, 0) < v.totalQuantity)")
    int incrementUsedQuantity(@Param("voucherId") String voucherId);

    /**
     * Roll back a previous {@link #incrementUsedQuantity(String)} call.
     * Never lets {@code used_quantity} go below zero.
     */
    @Modifying
    @Query("UPDATE Voucher v SET v.usedQuantity = CASE " +
            "WHEN COALESCE(v.usedQuantity, 0) > 0 THEN v.usedQuantity - 1 " +
            "ELSE 0 END " +
            "WHERE v.id = :voucherId")
    int decrementUsedQuantity(@Param("voucherId") String voucherId);
}

