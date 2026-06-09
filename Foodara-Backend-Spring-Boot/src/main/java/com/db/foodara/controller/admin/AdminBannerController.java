package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.admin.AdminBannerResponse;
import com.db.foodara.service.admin.AdminBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final AdminBannerService adminBannerService;

    @GetMapping
    public ApiResponse<List<AdminBannerResponse>> getBanners() {
        return ApiResponse.success(adminBannerService.getBanners());
    }

    @PostMapping
    public ApiResponse<AdminBannerResponse> createBanner(@RequestBody Map<String, Object> request) {
        return ApiResponse.success(adminBannerService.createBanner(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateBanner(@PathVariable String id, @RequestBody Map<String, Object> request) {
        adminBannerService.updateBanner(id, request);
        return ApiResponse.success("Banner updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBanner(@PathVariable String id) {
        adminBannerService.deleteBanner(id);
        return ApiResponse.success("Banner deleted");
    }
}
