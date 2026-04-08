package com.db.foodara.repository.location;

import com.db.foodara.entity.location.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, String> {
    List<District> findByCityIdAndIsActiveTrueOrderByNameAsc(String cityId);
    List<District> findByCityId(String cityId);
}
