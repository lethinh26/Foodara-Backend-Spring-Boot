package com.db.foodara.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_MAPBOX_GEOCODE = "mapbox.geocode";
    public static final String CACHE_MAPBOX_DIRECTIONS = "mapbox.directions";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new CaffeineCache(CACHE_MAPBOX_GEOCODE,
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofHours(24))
                                .maximumSize(10_000)
                                .build()),
                new CaffeineCache(CACHE_MAPBOX_DIRECTIONS,
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofMinutes(5))
                                .maximumSize(5_000)
                                .build())
        ));
        return manager;
    }
}
