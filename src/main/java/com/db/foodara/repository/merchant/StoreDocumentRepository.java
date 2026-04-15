package com.db.foodara.repository.merchant;

import com.db.foodara.entity.merchant.StoreDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreDocumentRepository extends JpaRepository<StoreDocument, String> {
    List<StoreDocument> findByMerchantId(String merchantId);
    List<StoreDocument> findByStoreId(String storeId);
    Optional<StoreDocument> findByIdAndMerchantId(String id, String merchantId);
}
