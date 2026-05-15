package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.entity.config.DeliveryFeeConfig;
import com.db.foodara.entity.config.PlatformConfig;
import com.db.foodara.service.admin.AdminConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final AdminConfigService adminConfigService;

    @GetMapping("/platform")
    public ApiResponse<List<PlatformConfig>> getPlatformConfigs() {
        return ApiResponse.success(adminConfigService.getPlatformConfigs());
    }

    @PutMapping("/platform/{key}")
    public ApiResponse<Void> updatePlatformConfig(@PathVariable String key, @RequestBody Map<String, String> request) {
        String adminUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        String configValue = request.get("configValue");
        adminConfigService.updatePlatformConfig(key, configValue, adminUserId);
        return ApiResponse.success("Platform config updated");
    }

    @GetMapping("/delivery-fees")
    public ApiResponse<List<DeliveryFeeConfig>> getDeliveryFeeConfigs() {
        return ApiResponse.success(adminConfigService.getDeliveryFeeConfigs());
    }

    @PutMapping("/delivery-fees/{id}")
    public ApiResponse<Void> updateDeliveryFeeConfig(@PathVariable String id, @RequestBody Map<String, Object> data) {
        adminConfigService.updateDeliveryFeeConfig(id, data);
        return ApiResponse.success("Delivery fee config updated");
    }
}
