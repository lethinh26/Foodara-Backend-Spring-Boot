package com.db.foodara.repository.home;

import com.db.foodara.entity.home.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, String> {

    @Query("SELECT c FROM Campaign c WHERE c.isActive = true " +
           "AND (c.startsAt IS NULL OR c.startsAt <= :now) " +
           "AND (c.endsAt IS NULL OR c.endsAt >= :now) " +
           "ORDER BY c.createdAt DESC")
    List<Campaign> findActiveCampaigns(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Campaign c WHERE c.isActive = true " +
           "AND c.campaignType = :type " +
           "AND (c.startsAt IS NULL OR c.startsAt <= :now) " +
           "AND (c.endsAt IS NULL OR c.endsAt >= :now)")
    List<Campaign> findActiveByType(@Param("type") String type, @Param("now") LocalDateTime now);
}
