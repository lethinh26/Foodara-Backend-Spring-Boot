package com.db.foodara.repository.store;

import com.db.foodara.entity.store.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, String> {
    boolean existsByName(String name);
}