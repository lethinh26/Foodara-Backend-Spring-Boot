package com.db.foodara.repository.merchant;

import com.db.foodara.entity.merchant.StoreBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreBankAccountRepository extends JpaRepository<StoreBankAccount, String> {
    List<StoreBankAccount> findByMerchantId(String merchantId);
    Optional<StoreBankAccount> findByIdAndMerchantId(String id, String merchantId);
    boolean existsByMerchantIdAndIsDefault(String merchantId, Boolean isDefault);
}
