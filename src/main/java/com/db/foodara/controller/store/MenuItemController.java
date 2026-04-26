package com.db.foodara.controller.store;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.store.MenuItemDetailResponse;
import com.db.foodara.dto.response.store.OptionGroupResponse;
import com.db.foodara.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final StoreService storeService;

    @GetMapping("/{id}")
    public ApiResponse<MenuItemDetailResponse> getMenuItemById(@PathVariable String id) {
        return ApiResponse.success(storeService.getMenuItemById(id));
    }

    @GetMapping("/{id}/options")
    public ApiResponse<List<OptionGroupResponse>> getMenuItemOptions(@PathVariable String id) {
        return ApiResponse.success(storeService.getMenuItemOptions(id));
    }
}
