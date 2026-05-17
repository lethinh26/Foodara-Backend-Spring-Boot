package com.db.foodara.controller.order;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.foodara.dto.request.order.PlaceOrderRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.order.PlaceOrderResponse;
import com.db.foodara.entity.order.Order;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.service.order.CustomerOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    @PostMapping
    public ApiResponse<PlaceOrderResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request
    ) {
        return ApiResponse.success(customerOrderService.placeOrder(requireUserId(authentication), request));
    }


    @GetMapping
    public ApiResponse<List<Order>> getOrders(Authentication authentication) {
        return ApiResponse.success(customerOrderService.getCustomerOrders(requireUserId(authentication)));
    }


    @GetMapping("/{orderId}")
    public ApiResponse<Order> getOrderDetail(
            Authentication authentication,
            @PathVariable String orderId
    ) {
        return ApiResponse.success(customerOrderService.getCustomerOrderDetail(requireUserId(authentication), orderId));
    }

    /** #67 — lightweight status payload for FE polling/realtime overlays. */
    @GetMapping("/{orderId}/status")
    public ApiResponse<Map<String, Object>> getOrderStatus(
            Authentication authentication,
            @PathVariable String orderId
    ) {
        return ApiResponse.success(customerOrderService.getOrderStatus(requireUserId(authentication), orderId));
    }


    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Order> cancelOrder(
            Authentication authentication,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        return ApiResponse.success(customerOrderService.cancelOrder(requireUserId(authentication), orderId, reason));
    }

    /**
     * #72 — reorder: copy every line of a previous order into a fresh cart.
     * Both POST (per spec) and PUT are exposed so frontends that prefer
     * idempotent verbs work without a config change.
     */
    @PostMapping("/{orderId}/reorder")
    public ApiResponse<CustomerOrderService.ReorderResult> reorder(
            Authentication authentication,
            @PathVariable String orderId
    ) {
        return ApiResponse.success(customerOrderService.reorderFromOrder(requireUserId(authentication), orderId));
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
