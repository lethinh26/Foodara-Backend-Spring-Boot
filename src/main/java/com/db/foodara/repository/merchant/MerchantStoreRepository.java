package com.db.foodara.repository.merchant;

import com.db.foodara.entity.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantStoreRepository extends JpaRepository<Store, String> {
    List<Store> findByMerchantId(String merchantId);
    Optional<Store> findByIdAndMerchantId(String id, String merchantId);
    boolean existsByMerchantIdAndName(String merchantId, String name);
}
