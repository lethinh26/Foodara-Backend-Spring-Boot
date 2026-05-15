package com.db.foodara.repository.settlement;

import com.db.foodara.entity.settlement.DriverSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverSettlementRepository extends JpaRepository<DriverSettlement, String> {
    Page<DriverSettlement> findByStatus(String status, Pageable pageable);
    Page<DriverSettlement> findByDriverId(String driverId, Pageable pageable);
}
