package com.db.foodara.controller.order;

import com.db.foodara.dto.request.order.RejectOrderRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.entity.order.Order;
import com.db.foodara.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/merchant/stores/{storeId}/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/:storeId/orders")
    public ApiResponse<List<Order>> getAllOrder(Authentication authentication,
                                                @PathVariable String storeId){
        String userId = authentication.getName();
        return ApiResponse.success(orderService.getOrders(userId, storeId));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<Order> getOrderDetail(Authentication authentication,
                                             @PathVariable String storeId,
                                             @PathVariable String orderId) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.getOrderDetail(userId, storeId, orderId));
    }

    @PutMapping("/{orderId}/accept")
    public ApiResponse<Order> acceptOrder(Authentication authentication,
                                          @PathVariable String storeId,
                                          @PathVariable String orderId) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.acceptOrder(userId, storeId, orderId));
    }

    @PutMapping("/{orderId}/reject")
    public ApiResponse<Order> rejectOrder(Authentication authentication,
                                          @PathVariable String storeId,
                                          @PathVariable String orderId,
                                          @Valid @RequestBody RejectOrderRequest request) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.rejectOrder(userId, storeId, orderId, request));
    }

    @PutMapping("/{orderId}/preparing")
    public ApiResponse<Order> preparingOrder(Authentication authentication,
                                             @PathVariable String storeId,
                                             @PathVariable String orderId) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.preparingOrder(userId, storeId, orderId));
    }

    @PutMapping("/{orderId}/ready")
    public ApiResponse<Order> readyOrder(Authentication authentication,
                                         @PathVariable String storeId,
                                         @PathVariable String orderId) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.readyOrder(userId, storeId, orderId));
    }

    @PutMapping("/{orderId}/handover")
    public ApiResponse<Order> handoverOrder(Authentication authentication,
                                            @PathVariable String storeId,
                                            @PathVariable String orderId) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.handoverOrder(userId, storeId, orderId));
    }
}
