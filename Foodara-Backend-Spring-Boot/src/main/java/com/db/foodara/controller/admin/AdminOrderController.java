package com.db.foodara.controller.admin;

import com.db.foodara.dto.request.admin.AssignDriverRequest;
import com.db.foodara.dto.request.admin.UpdateOrderStatusRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.*;
import com.db.foodara.service.admin.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ApiResponse<PageResponse<AdminOrderResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String payment_status) {
        return ApiResponse.success(adminOrderService.getOrders(page, size, search, status, payment_status));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminOrderResponse> getOrderDetail(@PathVariable String id) {
        return ApiResponse.success(adminOrderService.getOrderDetail(id));
    }

    @GetMapping("/{id}/items")
    public ApiResponse<List<AdminOrderItemResponse>> getOrderItems(@PathVariable String id) {
        return ApiResponse.success(adminOrderService.getOrderItems(id));
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<OrderHistoryResponse>> getOrderHistory(@PathVariable String id) {
        return ApiResponse.success(adminOrderService.getOrderHistory(id));
    }

    @GetMapping("/{id}/assignments")
    public ApiResponse<List<OrderAssignmentResponse>> getOrderAssignments(@PathVariable String id) {
        return ApiResponse.success(adminOrderService.getOrderAssignments(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateOrderStatus(@PathVariable String id,
                                                @RequestBody @Valid UpdateOrderStatusRequest request) {
        String adminUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        adminOrderService.updateOrderStatus(id, request, adminUserId);
        return ApiResponse.success("Order status updated");
    }

    @PutMapping("/{id}/assign-driver")
    public ApiResponse<Void> assignDriver(@PathVariable String id,
                                           @RequestBody @Valid AssignDriverRequest request) {
        String adminUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        adminOrderService.assignDriver(id, request, adminUserId);
        return ApiResponse.success("Driver assigned successfully");
    }
}
