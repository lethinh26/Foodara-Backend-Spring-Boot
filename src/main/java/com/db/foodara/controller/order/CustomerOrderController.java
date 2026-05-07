package com.db.foodara.controller.order;

import com.db.foodara.dto.request.order.PlaceOrderRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.order.PlaceOrderResponse;
import com.db.foodara.entity.order.Order;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.service.order.CustomerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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


    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Order> cancelOrder(
            Authentication authentication,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        return ApiResponse.success(customerOrderService.cancelOrder(requireUserId(authentication), orderId, reason));
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
