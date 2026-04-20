package com.db.foodara.repository.location;

import com.db.foodara.entity.location.ServiceZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceZoneRepository extends JpaRepository<ServiceZone, String> {
    List<ServiceZone> findByIsActiveTrue();
    List<ServiceZone> findByCityIdAndIsActiveTrue(String cityId);
    Optional<ServiceZone> findByIdAndIsActiveTrue(String id);
}
