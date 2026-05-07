package com.db.foodara.repository.home;

import com.db.foodara.entity.home.CampaignParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignParticipantRepository extends JpaRepository<CampaignParticipant, String> {
    List<CampaignParticipant> getCampaignParticipantByStoreId(String storeId);
    List<CampaignParticipant> getCampaignParticipantByCampaignId(String campaignId);
}
