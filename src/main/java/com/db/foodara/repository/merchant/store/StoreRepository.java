package com.db.foodara.repository.merchant.store;

import com.db.foodara.entity.merchant.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    List<Store> getStoresByMerchantId(String merchantId);

    boolean existsByMerchantIdAndId(String merchantId, String id);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
    Optional<Store> getStoreById(String id);
}
