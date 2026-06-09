package com.db.foodara.controller.order;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.foodara.dto.request.order.RejectOrderRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.merchant.MerchantOrderResponse;
import com.db.foodara.service.order.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * M05–M07 — Merchant store-scoped order controller.
 *
 * NOTE: This is named OrderController for legacy reasons but it is exclusively
 * the merchant order endpoint. Customer-facing order endpoints live in
 * {@code CustomerOrderController}.
 */
@RestController
@RequestMapping("/v1/merchant/stores/{storeId}/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<List<MerchantOrderResponse>> getAllOrder(Authentication authentication,
                                                                 @PathVariable String storeId) {
        return ApiResponse.success(orderService.getOrders(authentication.getName(), storeId));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<MerchantOrderResponse> getOrderDetail(Authentication authentication,
                                                              @PathVariable String storeId,
                                                              @PathVariable String orderId) {
        return ApiResponse.success(orderService.getOrderDetail(authentication.getName(), storeId, orderId));
    }

    @PutMapping("/{orderId}/accept")
    public ApiResponse<MerchantOrderResponse> acceptOrder(Authentication authentication,
                                                          @PathVariable String storeId,
                                                          @PathVariable String orderId) {
        return ApiResponse.success(orderService.acceptOrder(authentication.getName(), storeId, orderId));
    }

    @PutMapping("/{orderId}/reject")
    public ApiResponse<MerchantOrderResponse> rejectOrder(Authentication authentication,
                                                          @PathVariable String storeId,
                                                          @PathVariable String orderId,
                                                          @Valid @RequestBody RejectOrderRequest request) {
        return ApiResponse.success(orderService.rejectOrder(authentication.getName(), storeId, orderId, request));
    }

    @PutMapping("/{orderId}/ready")
    public ApiResponse<MerchantOrderResponse> readyOrder(Authentication authentication,
                                                         @PathVariable String storeId,
                                                         @PathVariable String orderId) {
        return ApiResponse.success(orderService.readyOrder(authentication.getName(), storeId, orderId));
    }

    @PutMapping("/{orderId}/handover")
    public ApiResponse<MerchantOrderResponse> handoverOrder(Authentication authentication,
                                                            @PathVariable String storeId,
                                                            @PathVariable String orderId) {
        return ApiResponse.success(orderService.handoverOrder(authentication.getName(), storeId, orderId));
    }
}
