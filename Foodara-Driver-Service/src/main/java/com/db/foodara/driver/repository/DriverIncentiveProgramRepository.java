package com.db.foodara.driver.repository;

import com.db.foodara.driver.entity.DriverIncentiveProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DriverIncentiveProgramRepository extends JpaRepository<DriverIncentiveProgram, String> {

    @Query("SELECT p FROM DriverIncentiveProgram p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<DriverIncentiveProgram> searchByName(@Param("search") String search, Pageable pageable);

    Page<DriverIncentiveProgram> findByIsActive(Boolean isActive, Pageable pageable);
}
