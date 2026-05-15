package com.db.foodara.repository.promotion;

import com.db.foodara.entity.promotion.CampaignParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignParticipantRepository extends JpaRepository<CampaignParticipant, String> {
    List<CampaignParticipant> findByCampaignId(String campaignId);
    long countByCampaignId(String campaignId);
}
