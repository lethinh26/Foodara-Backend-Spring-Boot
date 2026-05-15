package com.db.foodara.repository.config;

import com.db.foodara.entity.config.DeliveryFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryFeeConfigRepository extends JpaRepository<DeliveryFeeConfig, String> {
}
