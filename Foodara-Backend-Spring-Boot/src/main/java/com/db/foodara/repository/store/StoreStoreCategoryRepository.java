package com.db.foodara.repository.store;

import com.db.foodara.entity.store.StoreStoreCategory;
import com.db.foodara.entity.store.StoreStoreCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreStoreCategoryRepository extends JpaRepository<StoreStoreCategory, StoreStoreCategoryId> {

    @Query(value = "SELECT store_id FROM store_store_categories WHERE category_id = :categoryId", nativeQuery = true)
    List<String> findStoreIdsByCategoryId(@Param("categoryId") String categoryId);
}
