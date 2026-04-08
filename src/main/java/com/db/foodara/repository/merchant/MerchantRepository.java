package com.db.foodara.repository.merchant;

import com.db.foodara.entity.merchant.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findMerchantByBusinessEmail(String businessEmail);
    boolean existsMerchantByBusinessEmail(String businessEmail);
    Optional<Merchant> findMerchantById(String Id);
    boolean existsMerchantByBusinessPhone(String businessPhone);
    boolean existsMerchantById(String id);
}
