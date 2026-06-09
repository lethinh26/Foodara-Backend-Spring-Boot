package com.db.foodara.service.admin;

import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminCampaignResponse;
import com.db.foodara.dto.response.admin.CampaignParticipantResponse;
import com.db.foodara.entity.home.Campaign;
import com.db.foodara.entity.promotion.CampaignParticipant;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.promotion.CampaignParticipantRepository;
import com.db.foodara.repository.home.CampaignRepository;
import com.db.foodara.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignParticipantRepository participantRepository;
    private final StoreRepository storeRepository;

    private static final Set<String> VALID_CAMPAIGN_TYPES = Set.of(
            "promotion", "flash_sale", "free_ship", "seasonal", "custom"
    );

    public PageResponse<AdminCampaignResponse> getCampaigns(int page, int size, String search, Boolean isActive) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Campaign> campaignPage;

        if (search != null && !search.isBlank()) {
            campaignPage = campaignRepository.searchByName(search.trim(), pageRequest);
        } else if (isActive != null) {
            campaignPage = campaignRepository.findByIsActive(isActive, pageRequest);
        } else {
            campaignPage = campaignRepository.findAll(pageRequest);
        }

        List<AdminCampaignResponse> content = campaignPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AdminCampaignResponse>builder()
                .content(content)
                .page(campaignPage.getNumber())
                .number(campaignPage.getNumber())
                .size(campaignPage.getSize())
                .totalElements(campaignPage.getTotalElements())
                .totalPages(campaignPage.getTotalPages())
                .last(campaignPage.isLast())
                .build();
    }

    @Transactional
    public AdminCampaignResponse createCampaign(Map<String, Object> request) {
        Campaign campaign = new Campaign();
        applyCampaignFields(campaign, request);
        campaignRepository.save(campaign);
        return mapToResponse(campaign);
    }

    @Transactional
    public void updateCampaign(String id, Map<String, Object> request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOT_FOUND));
        applyCampaignFields(campaign, request);
        campaignRepository.save(campaign);
    }

    @Transactional
    public void deleteCampaign(String id) {
        if (!campaignRepository.existsById(id)) {
            throw new AppException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }
        campaignRepository.deleteById(id);
    }

    public List<CampaignParticipantResponse> getParticipants(String campaignId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new AppException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }
        return participantRepository.findByCampaignId(campaignId).stream()
                .map(this::mapParticipantToResponse)
                .toList();
    }

    private void applyCampaignFields(Campaign c, Map<String, Object> data) {
        if (data.containsKey("name")) c.setName((String) data.get("name"));
        if (data.containsKey("description")) c.setDescription((String) data.get("description"));
        if (data.containsKey("campaignType")) {
            String type = (String) data.get("campaignType");
            if (!VALID_CAMPAIGN_TYPES.contains(type)) {
                throw new AppException(ErrorCode.INVALID_KEY);
            }
            c.setCampaignType(type);
        }
        if (data.containsKey("bannerUrl")) c.setBannerUrl((String) data.get("bannerUrl"));
        if (data.containsKey("isActive")) c.setIsActive((Boolean) data.get("isActive"));
        if (data.containsKey("startsAt")) c.setStartsAt(toDateTime(data.get("startsAt")));
        if (data.containsKey("endsAt")) c.setEndsAt(toDateTime(data.get("endsAt")));
    }

    private AdminCampaignResponse mapToResponse(Campaign c) {
        long participantCount = participantRepository.countByCampaignId(c.getId());

        return AdminCampaignResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .campaignType(c.getCampaignType())
                .bannerUrl(c.getBannerUrl())
                .isActive(c.getIsActive())
                .startsAt(c.getStartsAt())
                .endsAt(c.getEndsAt())
                .createdAt(c.getCreatedAt())
                .participantCount((int) participantCount)
                .build();
    }

    private CampaignParticipantResponse mapParticipantToResponse(CampaignParticipant p) {
        String storeName = storeRepository.findById(p.getStoreId())
                .map(s -> s.getName())
                .orElse(null);

        return CampaignParticipantResponse.builder()
                .id(p.getId())
                .campaignId(p.getCampaignId())
                .storeId(p.getStoreId())
                .storeName(storeName)
                .status(p.getStatus())
                .joinedAt(p.getJoinedAt())
                .endedAt(p.getEndedAt())
                .build();
    }

    private LocalDateTime toDateTime(Object val) {
        if (val == null) return null;
        return LocalDateTime.parse(val.toString());
    }
}
