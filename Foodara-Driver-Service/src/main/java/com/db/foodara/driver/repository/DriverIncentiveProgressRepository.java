package com.db.foodara.driver.repository;

import com.db.foodara.driver.entity.DriverIncentiveProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverIncentiveProgressRepository extends JpaRepository<DriverIncentiveProgress, String> {

    Page<DriverIncentiveProgress> findByProgramId(String programId, Pageable pageable);

    List<DriverIncentiveProgress> findByDriverId(String driverId);

    long countByProgramId(String programId);
}
