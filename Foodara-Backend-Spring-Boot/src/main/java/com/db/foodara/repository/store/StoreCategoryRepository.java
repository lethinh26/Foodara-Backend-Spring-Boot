package com.db.foodara.repository.store;

import com.db.foodara.entity.store.StoreCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, String> {
    boolean existsByName(String name);

    @Query("SELECT c FROM StoreCategory c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<StoreCategory> searchCategories(@Param("q") String query, Pageable pageable);

    Page<StoreCategory> findByIsActive(boolean isActive, Pageable pageable);
}