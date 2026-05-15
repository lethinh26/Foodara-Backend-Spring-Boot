package com.db.foodara.repository.merchant.document;

import com.db.foodara.entity.merchant.document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    Optional<List<Document>> getDocumentsByMerchantId(String merchantId);

}
