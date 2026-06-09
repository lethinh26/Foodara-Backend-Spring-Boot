package com.db.foodara.repository.promotion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.foodara.entity.promotion.CampaignParticipant;

@Repository
public interface CampaignParticipantRepository extends JpaRepository<CampaignParticipant, String> {
    List<CampaignParticipant> findByCampaignId(String campaignId);
    long countByCampaignId(String campaignId);

    // Merchant queries
    List<CampaignParticipant> findByStoreIdInOrderByJoinedAtDesc(List<String> storeIds);

    boolean existsByCampaignIdAndStoreId(String campaignId, String storeId);
}
