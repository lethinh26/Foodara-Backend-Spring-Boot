package com.db.foodara.service.order;

import com.db.foodara.dto.request.order.AddCartItemRequest;
import com.db.foodara.dto.request.order.UpdateCartItemRequest;
import com.db.foodara.dto.response.order.CartResponse;
import com.db.foodara.dto.response.order.CartValidationResponse;
import com.db.foodara.entity.order.Cart;
import com.db.foodara.entity.order.CartItem;
import com.db.foodara.entity.order.CartItemOption;
import com.db.foodara.entity.store.*;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.order.CartItemOptionRepository;
import com.db.foodara.repository.order.CartItemRepository;
import com.db.foodara.repository.order.CartRepository;
import com.db.foodara.repository.store.*;
import com.db.foodara.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;
    private final ComboRepository comboRepository;
    private final OptionItemRepository optionItemRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final MenuItemOptionGroupRepository menuItemOptionGroupRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;

    public CartResponse getCart(String userId) {
        ensureUserExists(userId);
        return cartRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .map(this::mapCartResponse)
                .orElseGet(() -> emptyCart(userId));
    }

    @Transactional
    public CartResponse addItem(String userId, AddCartItemRequest request) {
        ensureUserExists(userId);
        validateAddRequest(request);

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if (!Boolean.TRUE.equals(store.getIsActive())) {
            throw new AppException(ErrorCode.CART_ITEM_UNAVAILABLE);
        }

        Cart cart = resolveOrCreateActiveCart(userId, request.getStoreId());
        ResolvedCartSelection selection = resolveSelection(
                request.getStoreId(),
                request.getMenuItemId(),
                request.getComboId(),
                request.getQuantity(),
                normalizeOptionIds(request.getOptionItemIds())
        );

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setMenuItemId(selection.menuItem != null ? selection.menuItem.getId() : null);
        cartItem.setComboId(selection.combo != null ? selection.combo.getId() : null);
        cartItem.setQuantity(request.getQuantity());
        cartItem.setUnitPrice(selection.unitPrice);
        cartItem.setSpecialInstructions(request.getSpecialInstructions());
        cartItem = cartItemRepository.save(cartItem);

        saveCartItemOptions(cartItem, selection.selectedOptions);
        touchCart(cart);
        return mapCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(String userId, String cartItemId, UpdateCartItemRequest request) {
        ensureUserExists(userId);
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new AppException(ErrorCode.CART_INVALID_REQUEST);
        }

        CartItem cartItem = cartItemRepository.findByIdAndCartUserId(cartItemId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        List<String> selectedOptionIds = request.getOptionItemIds();
        if (selectedOptionIds == null) {
            selectedOptionIds = cartItemOptionRepository.findByCartItemIdOrderByCreatedAtAsc(cartItemId).stream()
                    .map(CartItemOption::getOptionItemId)
                    .collect(Collectors.toList());
        }

        ResolvedCartSelection selection = resolveSelection(
                cartItem.getCart().getStoreId(),
                cartItem.getMenuItemId(),
                cartItem.getComboId(),
                request.getQuantity(),
                normalizeOptionIds(selectedOptionIds)
        );

        cartItem.setQuantity(request.getQuantity());
        cartItem.setUnitPrice(selection.unitPrice);
        if (request.getSpecialInstructions() != null) {
            cartItem.setSpecialInstructions(request.getSpecialInstructions());
        }
        cartItem = cartItemRepository.save(cartItem);

        cartItemOptionRepository.deleteByCartItemId(cartItemId);
        saveCartItemOptions(cartItem, selection.selectedOptions);
        touchCart(cartItem.getCart());
        return mapCartResponse(cartItem.getCart());
    }

    @Transactional
    public CartResponse removeItem(String userId, String cartItemId) {
        ensureUserExists(userId);
        CartItem cartItem = cartItemRepository.findByIdAndCartUserId(cartItemId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        Cart cart = cartItem.getCart();
        cartItemOptionRepository.deleteByCartItemId(cartItemId);
        cartItemRepository.delete(cartItem);

        if (cartItemRepository.countByCartId(cart.getId()) == 0) {
            cartRepository.delete(cart);
            return emptyCart(userId);
        }

        touchCart(cart);
        return mapCartResponse(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        ensureUserExists(userId);
        List<Cart> carts = cartRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        if (!carts.isEmpty()) {
            cartRepository.deleteAll(carts);
        }
    }

    public CartValidationResponse validateCart(String userId) {
        ensureUserExists(userId);
        Optional<Cart> cartOptional = cartRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId);
        if (cartOptional.isEmpty()) {
            return CartValidationResponse.builder()
                    .valid(false)
                    .subtotal(BigDecimal.ZERO)
                    .minOrderAmount(BigDecimal.ZERO)
                    .shortfallAmount(BigDecimal.ZERO)
                    .issues(List.of(issue("EMPTY_CART", "Giỏ hàng đang trống", null)))
                    .build();
        }

        Cart cart = cartOptional.get();
        List<CartItem> items = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        List<CartValidationResponse.ValidationIssueResponse> issues = new ArrayList<>();

        BigDecimal subtotal = items.stream()
                .map(item -> safeLineTotal(item.getUnitPrice(), item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Store store = storeRepository.findById(cart.getStoreId()).orElse(null);
        BigDecimal minOrderAmount = BigDecimal.ZERO;
        if (store == null) {
            issues.add(issue("STORE_NOT_FOUND", "Không tìm thấy cửa hàng của giỏ hàng", null));
        } else {
            if (!Boolean.TRUE.equals(store.getIsOpen())) {
                issues.add(issue("STORE_CLOSED", "Cửa hàng hiện đang đóng cửa", null));
            }
            if (!Boolean.TRUE.equals(store.getIsActive())) {
                issues.add(issue("STORE_INACTIVE", "Cửa hàng hiện không hoạt động", null));
            }
            minOrderAmount = defaultAmount(store.getMinOrderAmount());
        }

        List<String> cartItemIds = items.stream().map(CartItem::getId).collect(Collectors.toList());
        Map<String, List<CartItemOption>> optionMap = cartItemIds.isEmpty()
                ? Collections.emptyMap()
                : cartItemOptionRepository.findByCartItemIdIn(cartItemIds).stream()
                .collect(Collectors.groupingBy(option -> option.getCartItem().getId()));

        for (CartItem item : items) {
            try {
                resolveSelection(
                        cart.getStoreId(),
                        item.getMenuItemId(),
                        item.getComboId(),
                        item.getQuantity(),
                        optionMap.getOrDefault(item.getId(), Collections.emptyList()).stream()
                                .map(CartItemOption::getOptionItemId)
                                .collect(Collectors.toList())
                );
            } catch (AppException ex) {
                issues.add(issue(
                        ex.getErrorCode().name(),
                        ex.getErrorCode().getMessage(),
                        item.getId()
                ));
            }
        }

        BigDecimal shortfall = minOrderAmount.subtract(subtotal).max(BigDecimal.ZERO);
        if (shortfall.compareTo(BigDecimal.ZERO) > 0) {
            issues.add(issue("MIN_ORDER_NOT_REACHED", "Đơn hàng chưa đạt giá trị tối thiểu", null));
        }

        return CartValidationResponse.builder()
                .valid(issues.isEmpty())
                .subtotal(scaleAmount(subtotal))
                .minOrderAmount(scaleAmount(minOrderAmount))
                .shortfallAmount(scaleAmount(shortfall))
                .issues(issues)
                .build();
    }

    private void ensureUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateAddRequest(AddCartItemRequest request) {
        boolean hasMenuItem = StringUtils.hasText(request.getMenuItemId());
        boolean hasCombo = StringUtils.hasText(request.getComboId());
        if (hasMenuItem == hasCombo) {
            throw new AppException(ErrorCode.CART_INVALID_REQUEST);
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new AppException(ErrorCode.CART_INVALID_REQUEST);
        }
    }

    private Cart resolveOrCreateActiveCart(String userId, String storeId) {
        List<Cart> carts = cartRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        if (carts.isEmpty()) {
            return createCart(userId, storeId);
        }

        Optional<Cart> sameStore = carts.stream()
                .filter(cart -> storeId.equals(cart.getStoreId()))
                .findFirst();

        if (sameStore.isPresent()) {
            List<Cart> stale = carts.stream()
                    .filter(cart -> !cart.getId().equals(sameStore.get().getId()))
                    .collect(Collectors.toList());
            if (!stale.isEmpty()) {
                cartRepository.deleteAll(stale);
            }
            return sameStore.get();
        }

        cartRepository.deleteAll(carts);
        return createCart(userId, storeId);
    }

    private Cart createCart(String userId, String storeId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStoreId(storeId);
        return cartRepository.save(cart);
    }

    private void touchCart(Cart cart) {
        cartRepository.save(cart);
    }

    private List<String> normalizeOptionIds(List<String> optionItemIds) {
        if (optionItemIds == null || optionItemIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = optionItemIds.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (new HashSet<>(normalized).size() != normalized.size()) {
            throw new AppException(ErrorCode.CART_INVALID_OPTION);
        }
        return normalized;
    }

    private ResolvedCartSelection resolveSelection(
            String storeId,
            String menuItemId,
            String comboId,
            Integer quantity,
            List<String> optionItemIds
    ) {
        MenuItem menuItem = null;
        Combo combo = null;

        if (StringUtils.hasText(menuItemId)) {
            menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));
            if (!storeId.equals(menuItem.getStoreId())) {
                throw new AppException(ErrorCode.CART_STORE_MISMATCH);
            }
            if (!Boolean.TRUE.equals(menuItem.getIsActive()) || !Boolean.TRUE.equals(menuItem.getIsAvailable())) {
                throw new AppException(ErrorCode.CART_ITEM_UNAVAILABLE);
            }
            if (menuItem.getMaxQuantityPerOrder() != null && quantity > menuItem.getMaxQuantityPerOrder()) {
                throw new AppException(ErrorCode.CART_INVALID_REQUEST);
            }
            if (Boolean.TRUE.equals(menuItem.getTrackInventory())
                    && menuItem.getStockQuantity() != null
                    && quantity > menuItem.getStockQuantity()) {
                throw new AppException(ErrorCode.CART_ITEM_UNAVAILABLE);
            }
        }

        if (StringUtils.hasText(comboId)) {
            combo = comboRepository.findById(comboId)
                    .orElseThrow(() -> new AppException(ErrorCode.COMBO_NOT_FOUND));
            if (!storeId.equals(combo.getStoreId())) {
                throw new AppException(ErrorCode.CART_STORE_MISMATCH);
            }
            if (!Boolean.TRUE.equals(combo.getIsActive())) {
                throw new AppException(ErrorCode.CART_ITEM_UNAVAILABLE);
            }
        }

        List<OptionItem> selectedOptions = optionItemIds.isEmpty()
                ? Collections.emptyList()
                : optionItemRepository.findAllById(optionItemIds);

        if (selectedOptions.size() != optionItemIds.size()) {
            throw new AppException(ErrorCode.CART_INVALID_OPTION);
        }

        for (OptionItem option : selectedOptions) {
            if (!Boolean.TRUE.equals(option.getIsAvailable())) {
                throw new AppException(ErrorCode.CART_INVALID_OPTION);
            }
        }

        if (menuItem != null) {
            validateMenuItemOptionCompatibility(menuItem.getId(), selectedOptions);
        } else if (combo != null) {
            validateComboOptionCompatibility(storeId, selectedOptions);
        }

        BigDecimal optionTotal = selectedOptions.stream()
                .map(option -> defaultAmount(option.getPriceAdjustment()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal basePrice = menuItem != null
                ? defaultAmount(menuItem.getBasePrice())
                : defaultAmount(Objects.requireNonNull(combo).getComboPrice());

        return new ResolvedCartSelection(menuItem, combo, selectedOptions, scaleAmount(basePrice.add(optionTotal)));
    }

    private void validateMenuItemOptionCompatibility(String menuItemId, List<OptionItem> selectedOptions) {
        List<MenuItemOptionGroup> links = menuItemOptionGroupRepository.findByMenuItemId(menuItemId);
        Set<String> allowedGroupIds = links.stream()
                .map(MenuItemOptionGroup::getOptionGroupId)
                .collect(Collectors.toSet());

        if (allowedGroupIds.isEmpty()) {
            if (!selectedOptions.isEmpty()) {
                throw new AppException(ErrorCode.CART_INVALID_OPTION);
            }
            return;
        }

        Map<String, Long> selectedCountByGroup = selectedOptions.stream()
                .collect(Collectors.groupingBy(OptionItem::getOptionGroupId, Collectors.counting()));

        for (OptionItem selectedOption : selectedOptions) {
            if (!allowedGroupIds.contains(selectedOption.getOptionGroupId())) {
                throw new AppException(ErrorCode.CART_INVALID_OPTION);
            }
        }

        Map<String, OptionGroup> groupsById = optionGroupRepository.findAllById(allowedGroupIds).stream()
                .collect(Collectors.toMap(OptionGroup::getId, Function.identity()));

        for (OptionGroup optionGroup : groupsById.values()) {
            long selectedCount = selectedCountByGroup.getOrDefault(optionGroup.getId(), 0L);
            int minRequired = Math.max(
                    optionGroup.getMinSelections() != null ? optionGroup.getMinSelections() : 0,
                    Boolean.TRUE.equals(optionGroup.getIsRequired()) ? 1 : 0
            );
            int maxAllowed = optionGroup.getMaxSelections() != null ? optionGroup.getMaxSelections() : Integer.MAX_VALUE;

            if (selectedCount < minRequired || selectedCount > maxAllowed) {
                throw new AppException(ErrorCode.CART_INVALID_OPTION);
            }
        }
    }

    private void validateComboOptionCompatibility(String storeId, List<OptionItem> selectedOptions) {
        if (selectedOptions.isEmpty()) {
            return;
        }

        Set<String> groupIds = selectedOptions.stream()
                .map(OptionItem::getOptionGroupId)
                .collect(Collectors.toSet());
        Map<String, OptionGroup> groupsById = optionGroupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(OptionGroup::getId, Function.identity()));

        for (OptionItem selectedOption : selectedOptions) {
            OptionGroup optionGroup = groupsById.get(selectedOption.getOptionGroupId());
            if (optionGroup == null || !storeId.equals(optionGroup.getStoreId())) {
                throw new AppException(ErrorCode.CART_INVALID_OPTION);
            }
        }
    }

    private void saveCartItemOptions(CartItem cartItem, List<OptionItem> selectedOptions) {
        for (OptionItem optionItem : selectedOptions) {
            CartItemOption cartItemOption = new CartItemOption();
            cartItemOption.setCartItem(cartItem);
            cartItemOption.setOptionItemId(optionItem.getId());
            cartItemOption.setPriceAdjustment(defaultAmount(optionItem.getPriceAdjustment()));
            cartItemOptionRepository.save(cartItemOption);
        }
    }

    private CartResponse mapCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        List<String> cartItemIds = items.stream().map(CartItem::getId).collect(Collectors.toList());

        Map<String, List<CartItemOption>> optionMap = cartItemIds.isEmpty()
                ? Collections.emptyMap()
                : cartItemOptionRepository.findByCartItemIdIn(cartItemIds).stream()
                .collect(Collectors.groupingBy(option -> option.getCartItem().getId()));

        Set<String> menuItemIds = items.stream()
                .map(CartItem::getMenuItemId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, MenuItem> menuItemsById = menuItemIds.isEmpty()
                ? Collections.emptyMap()
                : menuItemRepository.findAllById(menuItemIds).stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        Set<String> comboIds = items.stream()
                .map(CartItem::getComboId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Combo> combosById = comboIds.isEmpty()
                ? Collections.emptyMap()
                : comboRepository.findAllById(comboIds).stream()
                .collect(Collectors.toMap(Combo::getId, Function.identity()));

        Set<String> optionItemIds = optionMap.values().stream()
                .flatMap(Collection::stream)
                .map(CartItemOption::getOptionItemId)
                .collect(Collectors.toSet());
        Map<String, OptionItem> optionItemsById = optionItemIds.isEmpty()
                ? Collections.emptyMap()
                : optionItemRepository.findAllById(optionItemIds).stream()
                .collect(Collectors.toMap(OptionItem::getId, Function.identity()));

        Set<String> optionGroupIds = optionItemsById.values().stream()
                .map(OptionItem::getOptionGroupId)
                .collect(Collectors.toSet());
        Map<String, OptionGroup> optionGroupsById = optionGroupIds.isEmpty()
                ? Collections.emptyMap()
                : optionGroupRepository.findAllById(optionGroupIds).stream()
                .collect(Collectors.toMap(OptionGroup::getId, Function.identity()));

        List<CartResponse.CartItemResponse> itemResponses = items.stream()
                .map(item -> mapCartItemResponse(
                        item,
                        menuItemsById,
                        combosById,
                        optionMap.getOrDefault(item.getId(), Collections.emptyList()),
                        optionItemsById,
                        optionGroupsById
                ))
                .collect(Collectors.toList());

        BigDecimal subtotal = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getQuantity)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        Store store = storeRepository.findById(cart.getStoreId()).orElse(null);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .storeId(cart.getStoreId())
                .storeName(store != null ? store.getName() : null)
                .storeMinOrderAmount(store != null ? defaultAmount(store.getMinOrderAmount()) : BigDecimal.ZERO)
                .isStoreOpen(store != null && Boolean.TRUE.equals(store.getIsOpen()) && Boolean.TRUE.equals(store.getIsActive()))
                .totalItems(totalItems)
                .subtotal(scaleAmount(subtotal))
                .updatedAt(cart.getUpdatedAt())
                .items(itemResponses)
                .build();
    }

    private CartResponse.CartItemResponse mapCartItemResponse(
            CartItem cartItem,
            Map<String, MenuItem> menuItemsById,
            Map<String, Combo> combosById,
            List<CartItemOption> cartOptions,
            Map<String, OptionItem> optionItemsById,
            Map<String, OptionGroup> optionGroupsById
    ) {
        String itemName = null;
        String imageUrl = null;

        if (StringUtils.hasText(cartItem.getMenuItemId())) {
            MenuItem menuItem = menuItemsById.get(cartItem.getMenuItemId());
            if (menuItem != null) {
                itemName = menuItem.getName();
                imageUrl = menuItem.getImageUrl();
            }
        } else if (StringUtils.hasText(cartItem.getComboId())) {
            Combo combo = combosById.get(cartItem.getComboId());
            if (combo != null) {
                itemName = combo.getName();
            }
        }

        BigDecimal lineTotal = safeLineTotal(cartItem.getUnitPrice(), cartItem.getQuantity());
        List<CartResponse.CartItemOptionResponse> optionResponses = cartOptions.stream()
                .map(cartOption -> {
                    OptionItem optionItem = optionItemsById.get(cartOption.getOptionItemId());
                    OptionGroup optionGroup = optionItem != null
                            ? optionGroupsById.get(optionItem.getOptionGroupId())
                            : null;
                    return CartResponse.CartItemOptionResponse.builder()
                            .id(cartOption.getId())
                            .optionItemId(cartOption.getOptionItemId())
                            .optionGroupId(optionItem != null ? optionItem.getOptionGroupId() : null)
                            .optionGroupName(optionGroup != null ? optionGroup.getName() : null)
                            .optionName(optionItem != null ? optionItem.getName() : null)
                            .priceAdjustment(scaleAmount(defaultAmount(cartOption.getPriceAdjustment())))
                            .isSize(optionGroup != null
                                    && optionGroup.getName() != null
                                    && optionGroup.getName().toLowerCase(Locale.ROOT).contains("size"))
                            .build();
                })
                .collect(Collectors.toList());

        return CartResponse.CartItemResponse.builder()
                .id(cartItem.getId())
                .menuItemId(cartItem.getMenuItemId())
                .comboId(cartItem.getComboId())
                .name(itemName)
                .imageUrl(imageUrl)
                .quantity(cartItem.getQuantity())
                .unitPrice(scaleAmount(defaultAmount(cartItem.getUnitPrice())))
                .totalPrice(scaleAmount(lineTotal))
                .specialInstructions(cartItem.getSpecialInstructions())
                .options(optionResponses)
                .build();
    }

    private CartResponse emptyCart(String userId) {
        return CartResponse.builder()
                .userId(userId)
                .storeMinOrderAmount(BigDecimal.ZERO)
                .isStoreOpen(false)
                .totalItems(0)
                .subtotal(BigDecimal.ZERO)
                .items(Collections.emptyList())
                .build();
    }

    private CartValidationResponse.ValidationIssueResponse issue(String code, String message, String cartItemId) {
        return CartValidationResponse.ValidationIssueResponse.builder()
                .code(code)
                .message(message)
                .cartItemId(cartItemId)
                .build();
    }

    private BigDecimal safeLineTotal(BigDecimal unitPrice, Integer quantity) {
        BigDecimal safeUnitPrice = defaultAmount(unitPrice);
        int safeQuantity = quantity != null && quantity > 0 ? quantity : 0;
        return safeUnitPrice.multiply(BigDecimal.valueOf(safeQuantity));
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private BigDecimal scaleAmount(BigDecimal amount) {
        return defaultAmount(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private record ResolvedCartSelection(
            MenuItem menuItem,
            Combo combo,
            List<OptionItem> selectedOptions,
            BigDecimal unitPrice
    ) {
    }
}
