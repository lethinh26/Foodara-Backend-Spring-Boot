package com.db.foodara.controller.order;

import com.db.foodara.dto.request.order.AddCartItemRequest;
import com.db.foodara.dto.request.order.UpdateCartItemRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.order.CartResponse;
import com.db.foodara.dto.response.order.CartValidationResponse;
import com.db.foodara.service.order.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(Authentication authentication) {
        return ApiResponse.success(cartService.getCart(authentication.getName()));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ApiResponse.success(cartService.addItem(authentication.getName(), request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartResponse> updateItem(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ApiResponse.success(cartService.updateItem(authentication.getName(), id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartResponse> removeItem(Authentication authentication, @PathVariable String id) {
        return ApiResponse.success(cartService.removeItem(authentication.getName(), id));
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ApiResponse.success("Cart cleared");
    }

    @GetMapping("/validate")
    public ApiResponse<CartValidationResponse> validate(Authentication authentication) {
        return ApiResponse.success(cartService.validateCart(authentication.getName()));
    }
}
