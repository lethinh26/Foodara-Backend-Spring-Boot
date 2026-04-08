package com.db.foodara.repository.location;

import com.db.foodara.entity.location.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, String> {
    List<City> findByIsActiveTrueOrderByNameAsc();
    Optional<City> findByCode(String code);
}
