package com.db.foodara.repository.store;

import com.db.foodara.entity.store.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, String>, JpaSpecificationExecutor<Store> {

    Optional<Store> findBySlug(String slug);

    List<Store> findByIsActiveTrueAndIsOpenTrue();

    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isOpen = true " +
           "AND s.serviceZoneId = :zoneId")
    List<Store> findActiveStoresByZone(@Param("zoneId") String zoneId);

    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isOpen = true " +
           "ORDER BY s.avgRating DESC")
    List<Store> findPopularStores(Pageable pageable);

    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isOpen = true " +
           "AND s.totalOrders > 0 " +
           "ORDER BY s.totalOrders DESC")
    List<Store> findStoresWithMostOrders(Pageable pageable);

    @Query(value = "SELECT * FROM stores s WHERE s.is_active = true AND s.is_open = true " +
           "AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL " +
           "ORDER BY (s.latitude - :lat) * (s.latitude - :lat) + " +
           "(s.longitude - :lng) * (s.longitude - :lng) ASC " +
           "LIMIT :limit",
           nativeQuery = true)
    List<Store> findNearbyStores(
            @Param("lat") BigDecimal latitude,
            @Param("lng") BigDecimal longitude,
            @Param("limit") int limit);

    Page<Store> findByIsActiveTrueAndIsOpenTrue(Pageable pageable);
}
