package com.db.foodara.controller.user;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.user.AddressResponse;
import com.db.foodara.dto.response.user.UserProfileResponse;
import com.db.foodara.dto.request.user.AddressRequest;
import com.db.foodara.dto.request.user.UpdateProfileRequest;
import com.db.foodara.dto.response.user.UserCheckMerchantResponse;
import com.db.foodara.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getProfile(Authentication authentication) {
        return ApiResponse.success(userService.getProfile(authentication.getName()));
    }

    // PUT /api/users/me
    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(userService.updateProfile(authentication.getName(), request));
    }

    // PUT /api/users/me/avatar
    @PutMapping("/me/avatar")
    public ApiResponse<UserProfileResponse> updateAvatar(Authentication authentication, @RequestParam("avatarUrl") String avatarUrl) {
        return ApiResponse.success(userService.updateAvatar(authentication.getName(), avatarUrl));
    }

    // GET /api/users/me/addresses
    @GetMapping("/me/addresses")
    public ApiResponse<List<AddressResponse>> getAddresses(Authentication authentication) {
        return ApiResponse.success(userService.getAddresses(authentication.getName()));
    }

    // POST /api/users/me/addresses
    @PostMapping("/me/addresses")
    public ApiResponse<AddressResponse> createAddress(Authentication authentication,
                                                      @RequestBody @Valid AddressRequest request) {
        return ApiResponse.success("Address created", userService.createAddress(authentication.getName(), request));
    }

    // PUT /api/users/me/addresses/{id}
    @PutMapping("/me/addresses/{id}")
    public ApiResponse<AddressResponse> updateAddress(Authentication authentication, @PathVariable String id, @RequestBody @Valid AddressRequest request) {
        return ApiResponse.success(userService.updateAddress(authentication.getName(), id, request));
    }

    // DELETE /api/users/me/addresses/{id}
    @DeleteMapping("/me/addresses/{id}")
    public ApiResponse<Void> deleteAddress(Authentication authentication, @PathVariable String id) {
        userService.deleteAddress(authentication.getName(), id);
        return ApiResponse.success("Address deleted");
    }

    // PUT /api/users/me/addresses/{id}/default
    @PutMapping("/me/addresses/{id}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(Authentication authentication, @PathVariable String id) {
        return ApiResponse.success(userService.setDefaultAddress(authentication.getName(), id));
    }

    @GetMapping("/check-merchant/{email}/{password}")
    public ApiResponse<UserCheckMerchantResponse> getUserCheckMerchant(@PathVariable String email, @PathVariable String password){
        return ApiResponse.success(userService.getUserCheckMerchant(email, password));
    }
}