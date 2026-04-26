package com.db.foodara.repository.promotion;

import com.db.foodara.entity.promotion.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, String> {

    @Query("SELECT uv FROM UserVoucher uv WHERE uv.userId = :userId AND uv.voucher.id = :voucherId")
    Optional<UserVoucher> findByUserIdAndVoucherId(@Param("userId") String userId, @Param("voucherId") String voucherId);

    @Query("SELECT uv FROM UserVoucher uv JOIN FETCH uv.voucher v WHERE uv.userId = :userId " +
            "AND uv.isUsed = false " +
            "AND (uv.expiresAt IS NULL OR uv.expiresAt >= :now) " +
            "AND v.isActive = true " +
            "AND (v.startsAt IS NULL OR v.startsAt <= :now) " +
            "AND (v.expiresAt IS NULL OR v.expiresAt >= :now)")
    List<UserVoucher> findActiveByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(uv) FROM UserVoucher uv WHERE uv.userId = :userId AND uv.voucher.id = :voucherId AND uv.isUsed = true")
    long countUsedByUserAndVoucher(@Param("userId") String userId, @Param("voucherId") String voucherId);
}
