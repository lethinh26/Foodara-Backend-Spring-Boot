package com.db.foodara.service.merchant;

import com.db.foodara.dto.request.store.*;
import com.db.foodara.dto.response.store.*;
import com.db.foodara.entity.store.*;
import com.db.foodara.entity.store.MenuItem;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.store.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class MerchantMenuService {
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OptionGroupRepository optionGroupRepository;

    @Autowired
    private OptionItemRepository optionItemRepository;

    @Autowired
    private ComboRepository comboRepository;

    @Autowired
    private ComboItemRepository comboItemRepository;

    // 97 GET	/api/merchant/stores/:storeId/menu-categories	Danh sách danh mục do lay cua store nen phai xac thuc
    public List<MenuCategory> getCategories(String userId, String storeId){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        storeRepository.findStoreById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return menuCategoryRepository.getMenuCategoriesByStoreId(storeId);
    }

    // 98	POST	/api/merchant/stores/:storeId/menu-categories	Tạo danh mục
    @Transactional
    public MenuCategory createMenuCategory(String userId, MenuCategoryRequest request){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        MenuCategory menuCategory = new MenuCategory();

        storeRepository.findStoreById(request.getStoreId()).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        menuCategoryRepository.findMenuCategoryByStoreIdAndName(request.getStoreId(), request.getName()).orElseThrow(
                () -> new AppException(ErrorCode.MENU_CATEGORY_NAME_EXISTED)
        );

        menuCategory.setStoreId(request.getStoreId());
        menuCategory.setName(request.getName());
        menuCategory.setDescription(request.getDescription());
        menuCategory.setDisplayOrder(request.getDisplayOrder());
        menuCategory.setIsActive(request.getIsActive());
        menuCategory.setAvailableFrom(request.getAvailableFrom());
        menuCategory.setAvailableTo(request.getAvailableTo());

        return menuCategoryRepository.save(menuCategory);
    }

    // 99	PUT	/api/merchant/menu-categories/:id	Sửa danh mục
    @Transactional
    public MenuCategory updateMenuCategory(String userId, String menuCategoryId, MenuCategoryRequest request){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        storeRepository.findStoreById(request.getStoreId()).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if(menuCategoryRepository.findMenuCategoryByStoreIdAndName(request.getStoreId(), request.getName()).isPresent()){
            throw new AppException(ErrorCode.MENU_CATEGORY_NAME_EXISTED);
        }
        MenuCategory menuCategory = menuCategoryRepository.findMenuCategoryById(menuCategoryId).orElseThrow(() -> new AppException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        menuCategory.setName(request.getName());
        menuCategory.setDescription(request.getDescription());
        menuCategory.setDisplayOrder(request.getDisplayOrder());
        menuCategory.setIsActive(request.getIsActive());
        menuCategory.setAvailableFrom(request.getAvailableFrom());
        menuCategory.setAvailableTo(request.getAvailableTo());

        return menuCategoryRepository.save(menuCategory);
    }

    // 100	DELETE	/api/merchant/menu-categories/:id	Xoá danh mục
    @Transactional
    public MenuCategory deleteMenuCategory(String userId, String menuCategoryId){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        return menuCategoryRepository.removeById(menuCategoryId);
    }

    // 101	GET	/api/merchant/stores/:storeId/menu-items	Danh sách món
    public List<MenuItem> getMenuItems(String userId, String storeId){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        return menuItemRepository.findByStoreId(storeId);
    }

    // 102	POST	/api/merchant/stores/:storeId/menu-items	Tạo món mới
    public MenuItemResponse createMenuItem(String userId, MenuItemRequest request){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        storeRepository.findStoreById(request.getStoreId()).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        menuCategoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new AppException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        if(request.getName() == null){
            throw new AppException(ErrorCode.MENU_ITEM_NAME_INVALID);
        }

        MenuItem menuItem = new MenuItem();

        menuItem.setStoreId(request.getStoreId());
        menuItem.setCategoryId(request.getCategoryId());
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setBasePrice(request.getBasePrice());
        menuItem.setIsActive(request.getIsActive());
        menuItem.setTrackInventory(request.getTrackInventory());
        menuItem.setStockQuantity(request.getStockQuantity());
        menuItem.setMaxQuantityPerOrder(request.getMaxQuantityPerOrder());
        menuItem.setDailyLimit(request.getDailyLimit());
        menuItem.setIsPopular(request.getIsPopular());
        menuItem.setIsNew(request.getIsNew());
        menuItem.setDisplayOrder(request.getDisplayOrder());

        return toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    // 103  PUT    /api/merchant/menu-items/:id   Sửa món
    public MenuItemResponse updateMenuItem(String userId, String itemId, MenuItemRequest request) {
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));

        if (request.getCategoryId() != null && !request.getCategoryId().equals(menuItem.getCategoryId())) {
            menuCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
            menuItem.setCategoryId(request.getCategoryId());
        }

        if (request.getName() != null) menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setImageUrl(request.getImageUrl());

        if (request.getBasePrice() != null) {
            menuItem.setBasePrice(request.getBasePrice());
        }

        if (request.getIsAvailable() != null) menuItem.setIsAvailable(request.getIsAvailable());
        if (request.getIsActive() != null) menuItem.setIsActive(request.getIsActive());
        if (request.getTrackInventory() != null) menuItem.setTrackInventory(request.getTrackInventory());
        if (request.getStockQuantity() != null) menuItem.setStockQuantity(request.getStockQuantity());
        if (request.getMaxQuantityPerOrder() != null) menuItem.setMaxQuantityPerOrder(request.getMaxQuantityPerOrder());
        if (request.getDailyLimit() != null) menuItem.setDailyLimit(request.getDailyLimit());
        if (request.getIsPopular() != null) menuItem.setIsPopular(request.getIsPopular());
        if (request.getIsNew() != null) menuItem.setIsNew(request.getIsNew());
        if (request.getDisplayOrder() != null) menuItem.setDisplayOrder(request.getDisplayOrder());

        return toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    // 104	DELETE	/api/merchant/menu-items/:id	Xoá món
    @Transactional
    public MenuItem deleteMenuItem(String userId, String itemId){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        return menuItemRepository.removeByid(itemId);
    }

    //105	PUT	/api/merchant/menu-items/:id/availability	Bật/tắt món (hết hàng)
    @Transactional
    public MenuItemResponse updateAvailability(String userId, String itemId, Boolean isAvailable) {
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        menuItem.setIsAvailable(isAvailable); // con/het hang
        return toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    //106	PUT	/api/merchant/menu-items/:id/stock	Cập nhật tồn kho
    @Transactional
    public MenuItemResponse updateAmounStock(String userId, String itemId, int stockQuantity) {
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
        menuItem.setStockQuantity(stockQuantity); // con/het hang
        return toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    //107	GET	/api/merchant/stores/:storeId/option-groups	Danh sách nhóm tuỳ chọn
    public List<OptionGroup> getOptionGroup(String userId, String storeId){
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        storeRepository.findStoreById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return optionGroupRepository.findByStoreIdOrderByDisplayOrder(storeId);
    }

    //108	POST	/api/merchant/stores/:storeId/option-groups	Tạo nhóm tuỳ chọn
    @Transactional
    public OptionGroupResponse createOptionGroup(String userId, OptionalGroupRequest request){
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        storeRepository.findStoreById(request.getStoreId()).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        OptionGroup optionGroup = new OptionGroup();
        optionGroup.setStoreId(request.getStoreId());
        optionGroup.setName(request.getName());
        optionGroup.setMinSelections(request.getMinSelections());
        optionGroup.setMaxSelections(request.getMaxSelections());
        optionGroup.setDisplayOrder(request.getDisplayOrder());

        optionGroupRepository.save(optionGroup);
        return toOptionGroupResponse(optionGroup);
    }

    //109	PUT	/api/merchant/option-groups/:id	Sửa nhóm tuỳ chọn
    @Transactional
    public OptionGroupResponse updateOptionGroup(String userId, String optionGroupId, OptionalGroupRequest request) {
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        storeRepository.findStoreById(request.getStoreId()).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        OptionGroup optionGroup = optionGroupRepository.findById(optionGroupId).orElseThrow(() -> new AppException(ErrorCode.OPTION_GROUP_NOT_FOUND));
        optionGroup.setStoreId(request.getStoreId());
        optionGroup.setName(request.getName());
        optionGroup.setMinSelections(request.getMinSelections());
        optionGroup.setMaxSelections(request.getMaxSelections());
        optionGroup.setDisplayOrder(request.getDisplayOrder());

        optionGroupRepository.save(optionGroup);
        return toOptionGroupResponse(optionGroup);
    }

    //110	POST	/api/merchant/option-groups/:id/items	Thêm option item
    @Transactional
    public List<OptionItemResponse> createOptionItem(String userId, String optionGroupId, List<OptionItemRequest> requests){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        optionGroupRepository.findById(optionGroupId).orElseThrow(() -> new AppException(ErrorCode.OPTION_GROUP_NOT_FOUND));

        List<OptionItemResponse> optionItemResponses = new ArrayList<>();
        for(OptionItemRequest request: requests) {
            OptionItem optionItem = new OptionItem();

            optionItem.setOptionGroupId(optionGroupId);
            optionItem.setName(request.getName());
            optionItem.setPriceAdjustment(request.getPriceAdjustment());
            optionItem.setIsAvailable(request.getIsAvailable());
            optionItem.setIsDefault(request.getIsDefault());
            optionItem.setDisplayOrder(request.getDisplayOrder());

            optionItemRepository.save(optionItem);
            optionItemResponses.add(toOptionItemResponse(optionItem));
        }

        return optionItemResponses;
    }

    //111	POST	/api/merchant/stores/:storeId/combos Tạo combo
    @Transactional
    public ComboResponse createCombo(String userId, String storeId, ComboRequest comboRequest, List<ComboItemRequest> listItemRequest){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        storeRepository.findStoreById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        Combo combo = new Combo();
        combo.setStoreId(storeId); // Lấy từ tham số hàm
        combo.setName(comboRequest.getName());
        combo.setDescription(comboRequest.getDescription());
        combo.setComboPrice(comboRequest.getComboPrice());
        combo.setIsActive(comboRequest.getIsActive() != null ? comboRequest.getIsActive() : true);
        combo.setStartsAt(comboRequest.getStartsAt());
        combo.setEndsAt(comboRequest.getEndsAt());

        combo.setOriginalPrice(comboRequest.getOriginalPrice());

        Combo savedCombo = comboRepository.save(combo);

        if (listItemRequest == null || listItemRequest.isEmpty()) {
            throw new AppException(ErrorCode.COMBO_ITEMS_REQUIRED); // Bạn nên định nghĩa thêm lỗi này
        }

        List<ComboItem> comboItems = listItemRequest.stream()
                .map(itemReq -> {
                    ComboItem comboItem = new ComboItem();
                    comboItem.setComboId(savedCombo.getId()); // Gán ID của Combo vừa lưu
                    comboItem.setMenuItemId(itemReq.getMenuItemId());
                    comboItem.setQuantity(itemReq.getQuantity());
                    return comboItem;
                }).toList();

        List<ComboItem> savedItems = comboItemRepository.saveAll(comboItems);

        return toComboResponse(savedCombo, savedItems);
    }



    //112	PUT	/api/merchant/combos/:id	Sửa combo
    @Transactional
    public ComboResponse updateCombo(String userId, String comboId, ComboRequest comboRequest, List<ComboItemRequest> comboItemRequests) {
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new AppException(ErrorCode.COMBO_NOT_FOUND));

        if (comboRequest.getName() != null) combo.setName(comboRequest.getName());
        if (comboRequest.getDescription() != null) combo.setDescription(comboRequest.getDescription());
        if (comboRequest.getComboPrice() != null) combo.setComboPrice(comboRequest.getComboPrice());
        if (comboRequest.getOriginalPrice() != null) combo.setOriginalPrice(comboRequest.getOriginalPrice());
        if (comboRequest.getIsActive() != null) combo.setIsActive(comboRequest.getIsActive());
        if (comboRequest.getStartsAt() != null) combo.setStartsAt(comboRequest.getStartsAt());
        if (comboRequest.getEndsAt() != null) combo.setEndsAt(comboRequest.getEndsAt());

        Combo updatedCombo = comboRepository.save(combo);

        if (comboItemRequests != null && !comboItemRequests.isEmpty()) {
            comboItemRepository.removeById(comboId);
            List<ComboItem> newItems = comboItemRequests.stream()
                    .map(itemReq -> {
                        ComboItem ci = new ComboItem();
                        ci.setComboId(comboId);
                        ci.setMenuItemId(itemReq.getMenuItemId());
                        ci.setQuantity(itemReq.getQuantity());
                        return ci;
                    }).toList();

            List<ComboItem> savedItems = comboItemRepository.saveAll(newItems);

            return toComboResponse(updatedCombo, savedItems);
        }

        List<ComboItem> currentItems = comboItemRepository.findByComboId(comboId);
        return toComboResponse(updatedCombo, currentItems);
    }


    //113	DELETE	/api/merchant/combos/:id	Xoá combo
    @Transactional
    public boolean removeCombo(String userId, String comboId) {
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new AppException(ErrorCode.COMBO_NOT_FOUND));

        comboItemRepository.removeByComboId(comboId);
        comboRepository.removeById(comboId);
        return true;
    }



    public MenuCategoryResponse toMenuCategoryResponse(MenuCategory entity) {
        if (entity == null) {
            return null;
        }
        return MenuCategoryResponse.builder()
                .id(entity.getId())
                .storeId(entity.getStoreId())
                .name(entity.getName())
                .description(entity.getDescription())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public MenuItemResponse toMenuItemResponse(MenuItem entity) {
        if (entity == null) {
            return null;
        }

        return MenuItemResponse.builder()
                .id(entity.getId())
                .storeId(entity.getStoreId())
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .basePrice(entity.getBasePrice())
                .isAvailable(entity.getIsAvailable())
                .isActive(entity.getIsActive())
                .isPopular(entity.getIsPopular())
                .isNew(entity.getIsNew())
                .displayOrder(entity.getDisplayOrder())
                .avgRating(entity.getAvgRating())
                .totalRatings(entity.getTotalRatings())
                .totalSold(entity.getTotalSold())
                .maxQuantityPerOrder(entity.getMaxQuantityPerOrder())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public OptionItemResponse toOptionItemResponse(OptionItem entity) {
        if (entity == null) {
            return null;
        }

        return OptionItemResponse.builder()
                .id(entity.getId())
                .optionGroupId(entity.getOptionGroupId())
                .name(entity.getName())
                .priceAdjustment(entity.getPriceAdjustment())
                .isAvailable(entity.getIsAvailable())
                .isDefault(entity.getIsDefault())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    public OptionGroupResponse toOptionGroupResponse(OptionGroup entity) {
        if (entity == null) return null;

        return OptionGroupResponse.builder()
                .id(entity.getId())
                .storeId(entity.getStoreId())
                .name(entity.getName())
                .isRequired(entity.getIsRequired())
                .minSelections(entity.getMinSelections())
                .maxSelections(entity.getMaxSelections())
                .displayOrder(entity.getDisplayOrder())
                // Lưu ý: Trường 'options' thường được lấy từ một bảng khác (OptionItem)
                // Nếu bạn chưa có dữ liệu này, hãy để nó là danh sách rỗng để tránh lỗi Null
                .options(new ArrayList<>())
                .build();
    }

    public ComboResponse toComboResponse(Combo combo, List<ComboItem> items) {
        List<ComboResponse.ComboItemResponse> itemResponses = items.stream()
                .map(item -> ComboResponse.ComboItemResponse.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return ComboResponse.builder()
                .id(combo.getId())
                .storeId(combo.getStoreId())
                .name(combo.getName())
                .description(combo.getDescription())
                .comboPrice(combo.getComboPrice())
                .originalPrice(combo.getOriginalPrice())
                .isActive(combo.getIsActive())
                .startsAt(combo.getStartsAt())
                .endsAt(combo.getEndsAt())
                .items(itemResponses)
                .build();
    }
}
