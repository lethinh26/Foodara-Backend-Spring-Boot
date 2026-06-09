package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminCampaignResponse;
import com.db.foodara.dto.response.admin.CampaignParticipantResponse;
import com.db.foodara.service.admin.AdminCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/campaigns")
@RequiredArgsConstructor
public class AdminCampaignController {

    private final AdminCampaignService adminCampaignService;

    @GetMapping
    public ApiResponse<PageResponse<AdminCampaignResponse>> getCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        return ApiResponse.success(adminCampaignService.getCampaigns(page, size, search, isActive));
    }

    @PostMapping
    public ApiResponse<AdminCampaignResponse> createCampaign(@RequestBody Map<String, Object> request) {
        return ApiResponse.success(adminCampaignService.createCampaign(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateCampaign(@PathVariable String id, @RequestBody Map<String, Object> request) {
        adminCampaignService.updateCampaign(id, request);
        return ApiResponse.success("Campaign updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCampaign(@PathVariable String id) {
        adminCampaignService.deleteCampaign(id);
        return ApiResponse.success("Campaign deleted");
    }

    @GetMapping("/{id}/participants")
    public ApiResponse<List<CampaignParticipantResponse>> getParticipants(@PathVariable String id) {
        return ApiResponse.success(adminCampaignService.getParticipants(id));
    }
}
