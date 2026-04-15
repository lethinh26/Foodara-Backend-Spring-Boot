package com.db.foodara.repository.home;

import com.db.foodara.entity.home.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, String> {

    @Query("SELECT b FROM Banner b WHERE b.isActive = true " +
           "AND (b.startsAt IS NULL OR b.startsAt <= :now) " +
           "AND (b.endsAt IS NULL OR b.endsAt >= :now) " +
           "ORDER BY b.displayOrder ASC")
    List<Banner> findActiveBanners(@Param("now") LocalDateTime now);

    List<Banner> findByPositionAndIsActiveTrueOrderByDisplayOrderAsc(String position);
}
