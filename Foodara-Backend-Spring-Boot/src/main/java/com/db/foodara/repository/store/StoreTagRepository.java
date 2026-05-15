package com.db.foodara.repository.store;

import com.db.foodara.entity.store.StoreTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreTagRepository extends JpaRepository<StoreTag, String> {
    boolean existsByName(String name);

    @Query("SELECT t FROM StoreTag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(t.tagType) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<StoreTag> searchTags(@Param("q") String query, Pageable pageable);

    Page<StoreTag> findByIsActive(boolean isActive, Pageable pageable);
}
