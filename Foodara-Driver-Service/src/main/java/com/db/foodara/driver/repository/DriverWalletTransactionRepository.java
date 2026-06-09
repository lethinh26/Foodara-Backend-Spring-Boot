package com.db.foodara.driver.repository;

import com.db.foodara.driver.entity.DriverWalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverWalletTransactionRepository extends JpaRepository<DriverWalletTransaction, String> {
    Page<DriverWalletTransaction> findByDriverId(String driverId, Pageable pageable);
}
