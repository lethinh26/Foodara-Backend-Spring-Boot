package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminNotificationResponse;
import com.db.foodara.service.admin.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping
    public ApiResponse<PageResponse<AdminNotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String type) {
        return ApiResponse.success(adminNotificationService.getNotifications(page, size, type));
    }

    @PostMapping("/send")
    public ApiResponse<Void> sendNotification(@RequestBody Map<String, Object> request) {
        adminNotificationService.sendNotification(request);
        return ApiResponse.success("Notification sent");
    }
}
