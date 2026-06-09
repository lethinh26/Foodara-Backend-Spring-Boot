package com.db.foodara.driver.repository;

import com.db.foodara.driver.entity.DriverShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverShiftRepository extends JpaRepository<DriverShift, String> {
    Page<DriverShift> findByDriverId(String driverId, Pageable pageable);
}
