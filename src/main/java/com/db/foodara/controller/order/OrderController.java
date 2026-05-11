package com.db.foodara.controller.order;

import com.db.foodara.dto.request.order.RejectOrderRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.order.OrderResponseDTO;
import com.db.foodara.entity.order.Order;
import com.db.foodara.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/merchant/stores/{storeId}/orders")
@PreAuthorize("hasRole('MERCHANT')")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public ApiResponse<List<OrderResponseDTO>> getAllOrder(Authentication authentication,
                                                @PathVariable String storeId){
        String userId = authentication.getName();
        return ApiResponse.success(orderService.getOrders(userId, storeId));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponseDTO> getOrderDetail(Authentication authentication,
                                                        @PathVariable String storeId,
                                                        @PathVariable String orderId) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.getOrderDetail(userId, storeId, orderId));
    }

    @PutMapping("/{orderId}/accept")
    public ApiResponse<OrderResponseDTO> acceptOrder(Authentication authentication,
                                          @PathVariable String storeId,
                                          @PathVariable String orderId) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.acceptOrder(userId, storeId, orderId));
    }

    @PutMapping("/{orderId}/completed")
    public ApiResponse<OrderResponseDTO> completeOrder(Authentication authentication,
                                                     @PathVariable String storeId,
                                                     @PathVariable String orderId) {
        String userId = authentication.getName();
        return ApiResponse.success(orderService.completedOrder(userId, storeId, orderId));
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
