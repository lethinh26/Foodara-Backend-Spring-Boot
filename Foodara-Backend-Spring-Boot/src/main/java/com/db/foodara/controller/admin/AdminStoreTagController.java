package com.db.foodara.controller.admin;

import com.db.foodara.dto.request.admin.StoreTagRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminStoreTagResponse;
import com.db.foodara.service.admin.AdminStoreTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/store-tags")
@RequiredArgsConstructor
public class AdminStoreTagController {

    private final AdminStoreTagService tagService;

    @GetMapping
    public ApiResponse<PageResponse<AdminStoreTagResponse>> getTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        return ApiResponse.success(tagService.getTags(page, size, search, isActive));
    }

    @PostMapping
    public ApiResponse<AdminStoreTagResponse> createTag(@RequestBody @Valid StoreTagRequest request) {
        return ApiResponse.success(tagService.createTag(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminStoreTagResponse> updateTag(@PathVariable String id,
                                                        @RequestBody @Valid StoreTagRequest request) {
        return ApiResponse.success(tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ApiResponse.success("Store tag deleted");
    }
}
