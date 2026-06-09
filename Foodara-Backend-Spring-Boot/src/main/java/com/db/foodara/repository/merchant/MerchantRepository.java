package com.db.foodara.repository.merchant;

import com.db.foodara.entity.merchant.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByOwnerId(String ownerId);
    boolean existsByOwnerId(String ownerId);

    // Admin: search merchants
    @Query("SELECT m FROM Merchant m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR m.taxCode LIKE CONCAT('%', :q, '%') " +
            "OR m.businessPhone LIKE CONCAT('%', :q, '%') " +
            "OR LOWER(m.businessEmail) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Merchant> searchMerchants(@Param("q") String query, Pageable pageable);

    Page<Merchant> findByApprovalStatus(String status, Pageable pageable);
}
