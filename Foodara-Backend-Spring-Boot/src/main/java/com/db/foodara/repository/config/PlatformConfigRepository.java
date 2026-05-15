package com.db.foodara.repository.config;

import com.db.foodara.entity.config.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, String> {
    Optional<PlatformConfig> findByConfigKey(String configKey);
}
