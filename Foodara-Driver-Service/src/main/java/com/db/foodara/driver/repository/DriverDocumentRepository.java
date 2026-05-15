package com.db.foodara.driver.repository;

import com.db.foodara.driver.entity.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, String> {
    List<DriverDocument> findByDriverId(String driverId);
}
