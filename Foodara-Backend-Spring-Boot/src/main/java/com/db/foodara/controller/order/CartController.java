package com.db.foodara.controller.order;

import com.db.foodara.dto.request.order.AddCartItemRequest;
import com.db.foodara.dto.request.order.UpdateCartItemRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.order.CartResponse;
import com.db.foodara.dto.response.order.CartValidationResponse;
import com.db.foodara.service.order.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** Returns null for anonymous users, userId otherwise. */
    private static String resolveUserId(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    @GetMapping
    public ApiResponse<CartResponse> getCart(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) String guestCartId) {
        return ApiResponse.success(cartService.getCart(resolveUserId(authentication), guestCartId));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) String guestCartId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ApiResponse.success(cartService.addItem(resolveUserId(authentication), guestCartId, request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartResponse> updateItem(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) String guestCartId,
            @PathVariable String id,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ApiResponse.success(cartService.updateItem(resolveUserId(authentication), guestCartId, id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartResponse> removeItem(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) String guestCartId,
            @PathVariable String id) {
        return ApiResponse.success(cartService.removeItem(resolveUserId(authentication), guestCartId, id));
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) String guestCartId) {
        cartService.clearCart(resolveUserId(authentication), guestCartId);
        return ApiResponse.success("Cart cleared");
    }

    @GetMapping("/validate")
    public ApiResponse<CartValidationResponse> validate(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) String guestCartId) {
        return ApiResponse.success(cartService.validateCart(resolveUserId(authentication), guestCartId));
    }
}
