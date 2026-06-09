package com.db.foodara.driver.repository;

import com.db.foodara.driver.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    Optional<Driver> findByUserId(String userId);

    @Query("SELECT d FROM Driver d WHERE LOWER(d.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR d.phone LIKE CONCAT('%', :q, '%') " +
            "OR LOWER(d.vehiclePlate) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Driver> searchDrivers(@Param("q") String query, Pageable pageable);

    Page<Driver> findByApprovalStatus(String approvalStatus, Pageable pageable);
}
