package com.db.foodara.controller.merchant;

import com.db.foodara.dto.request.home.CampaignRequest;
import com.db.foodara.dto.request.promotion.VoucherRequest;
import com.db.foodara.dto.request.store.*;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.promotion.VoucherResponse;
import com.db.foodara.dto.response.store.ComboResponse;
import com.db.foodara.dto.response.store.MenuItemResponse;
import com.db.foodara.dto.response.store.OptionGroupResponse;
import com.db.foodara.dto.response.store.OptionItemResponse;
import com.db.foodara.entity.home.Campaign;
import com.db.foodara.entity.home.CampaignParticipant;
import com.db.foodara.entity.promotion.Voucher;
import com.db.foodara.entity.store.MenuCategory;
import com.db.foodara.entity.store.MenuItem;
import com.db.foodara.entity.store.OptionGroup;
import com.db.foodara.service.home.HomeService;
import com.db.foodara.service.merchant.MerchantMenuService;
import com.db.foodara.service.promotion.VoucherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/merchant")
public class MerchantMenuController {

    @Autowired
    private MerchantMenuService merchantMenuService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private HomeService homeService;

    @GetMapping("/stores/{storeId}/menu-categories")
    public ApiResponse<List<MenuCategory>> getMenuCategoriesByStore(Authentication authentication, @PathVariable("storeId") String storeId) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantMenuService.getCategories(userId, storeId));
    }

    @PostMapping("/stores/{storeId}/menu-categories")
    public ApiResponse<MenuCategory> createMenuCategory(Authentication authentication, @RequestBody @Valid MenuCategoryRequest request) {
        return ApiResponse.success(merchantMenuService.createMenuCategory(authentication.getName(), request));
    }

    @PutMapping("/menu-categories/{id}")
    public ApiResponse<MenuCategory> updateMenuCategory(Authentication authentication, @PathVariable String id, @RequestBody MenuCategoryRequest request) {
        return ApiResponse.success(merchantMenuService.updateMenuCategory(authentication.getName(), id, request));
    }

    @DeleteMapping("/menu-categories/{id}")
    public ApiResponse<MenuCategory> deleteMenuCategory(Authentication authentication, @PathVariable String id) {
        return ApiResponse.success(merchantMenuService.deleteMenuCategory(authentication.getName(), id));
    }

    @GetMapping("/stores/{storeId}/menu-items")
    public ApiResponse<List<MenuItem>> getMenuItems(Authentication authentication, @PathVariable String storeId) {
        return ApiResponse.success(merchantMenuService.getMenuItems(authentication.getName(), storeId));
    }

    @PostMapping("/stores/{storeId}/menu-items")
    public ApiResponse<MenuItemResponse> createMenuItem(Authentication authentication, @RequestBody MenuItemRequest request) {
        return ApiResponse.success(merchantMenuService.createMenuItem(authentication.getName(), request));
    }

    @PutMapping("/menu-items/{id}")
    public ApiResponse<MenuItemResponse> updateMenuItem(Authentication authentication, @PathVariable String id, @RequestBody MenuItemRequest request) {
        return ApiResponse.success(merchantMenuService.updateMenuItem(authentication.getName(), id, request));
    }

    @DeleteMapping("/menu-items/{id}")
    public ApiResponse<MenuItem> deleteMenuItem(Authentication authentication, @PathVariable String id) {
        return ApiResponse.success(merchantMenuService.deleteMenuItem(authentication.getName(), id));
    }

    @PutMapping("/menu-items/{id}/availability")
    public ApiResponse<MenuItemResponse> updateAvailability(Authentication authentication, @PathVariable String id, @RequestParam Boolean isAvailable) {
        return ApiResponse.success(merchantMenuService.updateAvailability(authentication.getName(), id, isAvailable));
    }

    @PutMapping("/menu-items/{id}/stock")
    public ApiResponse<MenuItemResponse> updateStock(Authentication authentication, @PathVariable String id, @RequestParam int stockQuantity) {
        return ApiResponse.success(merchantMenuService.updateAmounStock(authentication.getName(), id, stockQuantity));
    }


    @GetMapping("/stores/{storeId}/option-groups")
    public ApiResponse<List<OptionGroup>> getOptionGroups(Authentication authentication, @PathVariable String storeId) {
        return ApiResponse.success(merchantMenuService.getOptionGroup(authentication.getName(), storeId));
    }

    @PostMapping("/stores/{storeId}/option-groups")
    public ApiResponse<OptionGroupResponse> createOptionGroup(Authentication authentication, @RequestBody OptionalGroupRequest request) {
        return ApiResponse.success(merchantMenuService.createOptionGroup(authentication.getName(), request));
    }

    @PutMapping("/option-groups/{id}")
    public ApiResponse<OptionGroupResponse> updateOptionGroup(Authentication authentication, @PathVariable String id, @RequestBody OptionalGroupRequest request) {
        return ApiResponse.success(merchantMenuService.updateOptionGroup(authentication.getName(), id, request));
    }

    @PostMapping("/option-groups/{id}/items")
    public ApiResponse<List<OptionItemResponse>> createOptionItems(Authentication authentication, @PathVariable String id, @RequestBody List<OptionItemRequest> requests) {
        return ApiResponse.success(merchantMenuService.createOptionItem(authentication.getName(), id, requests));
    }

    @PostMapping("/stores/{storeId}/combos")
    public ApiResponse<ComboResponse> createCombo(
            Authentication authentication,
            @PathVariable String storeId,
            @RequestBody ComboCreateRequest wrapper) {
        return ApiResponse.success(merchantMenuService.createCombo(
                authentication.getName(),
                storeId,
                wrapper.getComboRequest(),
                wrapper.getComboItems()));
    }

    @PutMapping("/combos/{id}")
    public ApiResponse<ComboResponse> updateCombo(
            Authentication authentication,
            @PathVariable String id,
            @RequestBody ComboCreateRequest wrapper) {
        return ApiResponse.success(merchantMenuService.updateCombo(
                authentication.getName(),
                id,
                wrapper.getComboRequest(),
                wrapper.getComboItems()));
    }

    @DeleteMapping("/combos/{id}")
    public ApiResponse<Boolean> removeCombo(Authentication authentication, @PathVariable String id) {
        return ApiResponse.success(merchantMenuService.removeCombo(authentication.getName(), id));
    }

    @GetMapping("/vouchers")
    public ApiResponse<List<Voucher>> getVouchers(Authentication authentication){
        String userId = authentication.getName();
        return ApiResponse.success(voucherService.getVouchersByMerchant(userId));
    }

    @PostMapping("/vouchers/{storeId}")
    public ApiResponse<Voucher> createVouchers(Authentication authentication,
                                               @PathVariable String storeId,
                                               @RequestBody VoucherRequest request){
        String userId = authentication.getName();
        return ApiResponse.success(voucherService.createVoucher(userId, storeId, request));
    }

    @PutMapping("/vouchers/{voucherId}")
    public ApiResponse<Voucher> updateVouchers(Authentication authentication,
                                               @PathVariable String voucherId,
                                               @RequestBody VoucherRequest request){
        String userId = authentication.getName();
        return ApiResponse.success(voucherService.updateVoucher(userId, voucherId, request));
    }

    @DeleteMapping("/vouchers/{voucherId}")
    public ApiResponse<Boolean> deleteVouchers(Authentication authentication,
                                               @PathVariable String voucherId){
        String userId = authentication.getName();
        return ApiResponse.success(voucherService.deleteVouchers(userId, voucherId));
    }

    @PostMapping("/campaigns/join")
    public ApiResponse<Boolean> storeJoinCampaign(Authentication authentication,
                                                  @RequestBody CampaignParticipant request){
        String userId = authentication.getName();
        return ApiResponse.success(homeService.storeJoinCampaign(userId, request));
    }

    @GetMapping("/campaigns/join")
    public ApiResponse<List<CampaignParticipant>> getCampaignJoined(Authentication authentication){
        String userId = authentication.getName();
        return ApiResponse.success(homeService.getCampaignParticipant(userId));
    }
}
