package com.db.foodara.repository.store;

import com.db.foodara.entity.store.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String>, JpaSpecificationExecutor<MenuItem> {

    List<MenuItem> findByStoreIdAndIsActiveTrue(String storeId);

    Page<MenuItem> findByIsActiveTrueAndIsAvailableTrue(Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND m.isAvailable = true " +
           "AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<MenuItem> searchByNameOrDescription(@Param("query") String query);

    List<MenuItem> findByStoreIdAndIsPopularTrue(String storeId);

    List<MenuItem> findByIsActiveTrueAndIsAvailableTrueAndIsPopularTrue();

    List<MenuItem> findByStoreId(String storeId);

    MenuItem removeByid(String id);
}
