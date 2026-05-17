package com.db.foodara.service.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.foodara.entity.order.OrderItem;
import com.db.foodara.entity.store.MenuItem;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.order.OrderItemRepository;
import com.db.foodara.repository.store.MenuItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reserves and restores menu-item stock for order placement.
 *
 * <p>Only items with {@code track_inventory = true} are touched. Items where the
 * merchant hasn't enabled inventory tracking are simply skipped — selling them
 * never depletes a counter, so we don't need to (and must not) decrement them.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInventoryService {

    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;


    @Transactional
    public void reserveStockForCart(List<? extends CartLine> cartLines) {
        Map<String, Integer> grouped = groupByMenuItem(cartLines);
        if (grouped.isEmpty()) return;

        // Pre-load every menu item so we can decide which ones actually need a stock
        // decrement. Items without track_inventory are skipped silently.
        Map<String, MenuItem> menuItemsById = menuItemRepository.findAllById(grouped.keySet()).stream()
                .collect(Collectors.toMap(MenuItem::getId, java.util.function.Function.identity()));

        List<String> outOfStockItemNames = new ArrayList<>();
        List<String> unavailableItemNames = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            String menuItemId = entry.getKey();
            int qty = entry.getValue();

            MenuItem menuItem = menuItemsById.get(menuItemId);
            if (menuItem == null) continue;

            // Block ordering items marked unavailable regardless of tracking mode
            if (Boolean.FALSE.equals(menuItem.getIsAvailable())) {
                unavailableItemNames.add(Optional.ofNullable(menuItem.getName()).orElse("Món #" + menuItemId));
                continue;
            }

            if (!Boolean.TRUE.equals(menuItem.getTrackInventory())) {
                // Untracked items don't reserve stock — skip
                continue;
            }

            // Also reject when stock is exactly 0 — even before attempting the atomic decrement,
            // so the error message can reference the exact item name.
            Integer stock = menuItem.getStockQuantity();
            if (stock != null && stock <= 0) {
                outOfStockItemNames.add(Optional.ofNullable(menuItem.getName()).orElse("Món #" + menuItemId));
                continue;
            }

            int updated = menuItemRepository.decrementStockIfEnough(menuItemId, qty);
            if (updated == 0) {
                String displayName = Optional.ofNullable(menuItem.getName()).orElse("Món #" + menuItemId);
                log.warn("Stock reservation failed for menu item {} ({}): need={}, available={}",
                        menuItemId, displayName, qty, stock);
                outOfStockItemNames.add(displayName);
            }
        }

        if (!unavailableItemNames.isEmpty()) {
            throw new AppException(ErrorCode.MENU_ITEM_OUT_OF_STOCK,
                    unavailableItemNames.stream().map(n -> n + " (ngừng bán)").collect(Collectors.joining(", ")));
        }
        if (!outOfStockItemNames.isEmpty()) {
            throw new AppException(ErrorCode.MENU_ITEM_OUT_OF_STOCK,
                    String.join(", ", outOfStockItemNames));
        }
    }


    @Transactional
    public void restoreStockForOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) return;

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) return;

        // Aggregate by menu_item_id so we issue at most one UPDATE per item even when
        // a single order contains the same dish on multiple lines.
        Map<String, Integer> grouped = new HashMap<>();
        for (OrderItem item : items) {
            String menuItemId = item.getMenuItemId();
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            if (menuItemId == null || qty <= 0) continue;
            grouped.merge(menuItemId, qty, Integer::sum);
        }
        if (grouped.isEmpty()) return;

        // Skip the increment for items whose tracking flag was turned off after the order
        Set<String> trackedIds = menuItemRepository.findAllById(grouped.keySet()).stream()
                .filter(mi -> Boolean.TRUE.equals(mi.getTrackInventory()))
                .map(MenuItem::getId)
                .collect(Collectors.toSet());

        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            if (trackedIds.contains(entry.getKey())) {
                menuItemRepository.incrementStock(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<String, Integer> groupByMenuItem(List<? extends CartLine> cartLines) {
        Map<String, Integer> grouped = new HashMap<>();
        if (cartLines == null) return grouped;
        for (CartLine line : cartLines) {
            String menuItemId = line.menuItemId();
            int qty = Math.max(0, line.quantity());
            if (menuItemId == null || qty == 0) continue;
            grouped.merge(menuItemId, qty, Integer::sum);
        }
        return grouped;
    }

    public interface CartLine {
        String menuItemId();
        int quantity();
    }

    /** Convenience factory because the call site builds {@code CartLine}s a lot. */
    public static CartLine line(String menuItemId, int quantity) {
        return new CartLine() {
            @Override public String menuItemId() { return menuItemId; }
            @Override public int quantity() { return quantity; }
        };
    }

    // Suppress unused import false positive (Collections kept for future bulk helpers).
    @SuppressWarnings("unused")
    private static final Object UNUSED = Collections.emptyList();
}
