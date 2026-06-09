package com.db.foodara.driver.repository;

import com.db.foodara.driver.entity.DriverBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverBankAccountRepository extends JpaRepository<DriverBankAccount, String> {
    List<DriverBankAccount> findByDriverId(String driverId);
}
