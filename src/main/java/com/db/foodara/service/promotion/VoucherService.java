package com.db.foodara.service.promotion;

import com.db.foodara.dto.request.promotion.VoucherApplyRequest;
import com.db.foodara.dto.request.promotion.VoucherRemoveRequest;
import com.db.foodara.dto.response.promotion.VoucherBestChoiceResponse;
import com.db.foodara.dto.response.promotion.VoucherCartPricingResponse;
import com.db.foodara.dto.response.promotion.VoucherPricingResponse;
import com.db.foodara.dto.response.promotion.VoucherResponse;
import com.db.foodara.entity.order.Cart;
import com.db.foodara.entity.order.CartItem;
import com.db.foodara.entity.promotion.UserVoucher;
import com.db.foodara.entity.promotion.Voucher;
import com.db.foodara.entity.store.Store;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.order.CartItemRepository;
import com.db.foodara.repository.order.CartRepository;
import com.db.foodara.repository.promotion.UserVoucherRepository;
import com.db.foodara.repository.promotion.VoucherRepository;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public List<VoucherResponse> getStoreVouchers(String storeId, String userId, BigDecimal subtotal) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        Set<String> collectedIds = getCollectedVoucherIds(userId, now);

        return voucherRepository.findAvailableByStore(storeId, store.getMerchantId(), now).stream()
                .map(voucher -> mapVoucherResponse(voucher, collectedIds.contains(voucher.getId()), subtotal, null))
                .sorted(Comparator.comparing(VoucherResponse::getPotentialDiscount, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    public List<VoucherResponse> getMyVouchers(String userId, String storeId, BigDecimal subtotal) {
        ensureUserExists(userId);

        LocalDateTime now = LocalDateTime.now();
        String merchantId = null;
        if (StringUtils.hasText(storeId)) {
            merchantId = storeRepository.findById(storeId)
                    .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND))
                    .getMerchantId();
        }

        final String scopeMerchantId = merchantId;
        return userVoucherRepository.findActiveByUserId(userId, now).stream()
                .filter(uv -> isVoucherInScope(uv.getVoucher(), storeId, scopeMerchantId))
                .map(uv -> mapVoucherResponse(uv.getVoucher(), true, subtotal, uv))
                .sorted(Comparator.comparing(VoucherResponse::getPotentialDiscount, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    public VoucherResponse collectVoucher(String userId, String voucherId) {
        ensureUserExists(userId);

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        if (!isVoucherActive(voucher, now)) {
            throw new AppException(ErrorCode.VOUCHER_NOT_ACTIVE);
        }

        Optional<UserVoucher> existing = userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId);
        if (existing.isPresent()) {
            return mapVoucherResponse(voucher, true, null, existing.get());
        }

        Integer totalQty = voucher.getTotalQuantity();
        int usedQty = voucher.getUsedQuantity() != null ? voucher.getUsedQuantity() : 0;
        if (totalQty != null && usedQty >= totalQty) {
            throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
        }

        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUserId(userId);
        userVoucher.setVoucher(voucher);
        userVoucher.setExpiresAt(voucher.getExpiresAt());
        userVoucherRepository.save(userVoucher);

        return mapVoucherResponse(voucher, true, null, userVoucher);
    }

    public VoucherBestChoiceResponse getBestVoucherForStore(String userId, String storeId, BigDecimal subtotal) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(storeId)) {
            return VoucherBestChoiceResponse.builder().totalDiscount(BigDecimal.ZERO).build();
        }

        BigDecimal safeSubtotal = normalizeAmount(subtotal);
        if (safeSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return VoucherBestChoiceResponse.builder().totalDiscount(BigDecimal.ZERO).build();
        }

        List<Voucher> candidates = getEligibleCollectedVouchers(userId, storeId, safeSubtotal, LocalDateTime.now());
        return calculateBestChoice(candidates, safeSubtotal);
    }

    public VoucherCartPricingResponse getAvailableForCart(String userId, String storeId) {
        ensureUserExists(userId);
        Cart cart = cartRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        BigDecimal subtotal = calculateCartSubtotal(cart);
        List<Voucher> eligible = getEligibleCollectedVouchers(userId, storeId, subtotal, LocalDateTime.now());
        VoucherBestChoiceResponse bestChoice = calculateBestChoice(eligible, subtotal);

        return buildPricingResponse(
                cart.getStoreId(),
                subtotal,
                bestChoice.getPlatformVoucher(),
                null,
                bestChoice.getStoreVoucher(),
                bestChoice,
                mapAvailableVouchers(eligible, subtotal, userId)
        );
    }

    public VoucherCartPricingResponse applyVouchersForCart(String userId, VoucherApplyRequest request) {
        ensureUserExists(userId);
        Cart cart = cartRepository.findByUserIdAndStoreId(userId, request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        BigDecimal subtotal = calculateCartSubtotal(cart);
        List<Voucher> eligible = getEligibleCollectedVouchers(userId, request.getStoreId(), subtotal, LocalDateTime.now());
        Map<String, Voucher> eligibleById = eligible.stream().collect(Collectors.toMap(Voucher::getId, Function.identity()));
        Map<String, Voucher> eligibleByCode = toEligibleByCode(eligible);

        Voucher platform = resolveAppliedVoucher(
                request.getPlatformVoucherId(),
                request.getPlatformCode(),
                "platform",
                eligibleById,
                eligibleByCode
        );
        Voucher platformShip = resolveAppliedVoucher(
                request.getPlatformShipVoucherId(),
                request.getPlatformShipCode(),
                "platform",
                eligibleById,
                eligibleByCode
        );
        Voucher store = resolveAppliedVoucher(
                request.getStoreVoucherId(),
                request.getStoreCode(),
                "store",
                eligibleById,
                eligibleByCode
        );

        // Validate: platform discount voucher must NOT be free_ship type
        if (platform != null && "free_ship".equalsIgnoreCase(platform.getDiscountType())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_ELIGIBLE);
        }
        // Validate: platform ship voucher must BE free_ship type
        if (platformShip != null && !"free_ship".equalsIgnoreCase(platformShip.getDiscountType())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_ELIGIBLE);
        }

        // Validate stackability for each pair
        validateStackablePair(platform, store);
        validateStackablePair(platformShip, store);

        VoucherBestChoiceResponse bestChoice = calculateBestChoice(eligible, subtotal);
        return buildPricingResponse(
                cart.getStoreId(),
                subtotal,
                toPricingResponse(platform, subtotal),
                toPricingResponse(platformShip, subtotal),
                toPricingResponse(store, subtotal),
                bestChoice,
                mapAvailableVouchers(eligible, subtotal, userId)
        );
    }

    public VoucherCartPricingResponse removeVouchersForCart(String userId, VoucherRemoveRequest request) {
        ensureUserExists(userId);
        Cart cart = cartRepository.findByUserIdAndStoreId(userId, request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        BigDecimal subtotal = calculateCartSubtotal(cart);
        List<Voucher> eligible = getEligibleCollectedVouchers(userId, request.getStoreId(), subtotal, LocalDateTime.now());
        Map<String, Voucher> eligibleById = eligible.stream().collect(Collectors.toMap(Voucher::getId, Function.identity()));
        Map<String, Voucher> eligibleByCode = toEligibleByCode(eligible);
        VoucherBestChoiceResponse bestChoice = calculateBestChoice(eligible, subtotal);

        Voucher selectedPlatformVoucher = resolveAppliedVoucher(
                request.getPlatformVoucherId(),
                request.getPlatformCode(),
                "platform",
                eligibleById,
                eligibleByCode
        );
        Voucher selectedStoreVoucher = resolveAppliedVoucher(
                request.getStoreVoucherId(),
                request.getStoreCode(),
                "store",
                eligibleById,
                eligibleByCode
        );

        RemovalType removalType = resolveRemovalType(request);
        if (removalType.removePlatform()) {
            selectedPlatformVoucher = null;
        }
        if (removalType.removeStore()) {
            selectedStoreVoucher = null;
        }

        if (!removalType.removePlatform() && selectedPlatformVoucher == null) {
            selectedPlatformVoucher = resolvePricingVoucher(bestChoice.getPlatformVoucher(), eligibleById);
        }
        if (!removalType.removeStore() && selectedStoreVoucher == null) {
            selectedStoreVoucher = resolvePricingVoucher(bestChoice.getStoreVoucher(), eligibleById);
        }
        validateStackablePair(selectedPlatformVoucher, selectedStoreVoucher);

        VoucherPricingResponse selectedPlatform = toPricingResponse(selectedPlatformVoucher, subtotal);
        VoucherPricingResponse selectedStore = toPricingResponse(selectedStoreVoucher, subtotal);

        return buildPricingResponse(
                cart.getStoreId(),
                subtotal,
                selectedPlatform,
                null,
                selectedStore,
                bestChoice,
                mapAvailableVouchers(eligible, subtotal, userId)
        );
    }

    private VoucherCartPricingResponse buildPricingResponse(
            String storeId,
            BigDecimal subtotal,
            VoucherPricingResponse appliedPlatform,
            VoucherPricingResponse appliedPlatformShip,
            VoucherPricingResponse appliedStore,
            VoucherBestChoiceResponse bestChoice,
            List<VoucherResponse> available
    ) {
        BigDecimal totalDiscount = normalizeAmount(appliedPlatform != null ? appliedPlatform.getPotentialDiscount() : BigDecimal.ZERO)
                .add(normalizeAmount(appliedPlatformShip != null ? appliedPlatformShip.getPotentialDiscount() : BigDecimal.ZERO))
                .add(normalizeAmount(appliedStore != null ? appliedStore.getPotentialDiscount() : BigDecimal.ZERO));
        if (totalDiscount.compareTo(subtotal) > 0) {
            totalDiscount = subtotal;
        }

        return VoucherCartPricingResponse.builder()
                .storeId(storeId)
                .subtotal(scale(subtotal))
                .totalDiscount(scale(totalDiscount))
                .subtotalAfterVoucher(scale(subtotal.subtract(totalDiscount).max(BigDecimal.ZERO)))
                .appliedPlatformVoucher(appliedPlatform)
                .appliedPlatformShipVoucher(appliedPlatformShip)
                .appliedStoreVoucher(appliedStore)
                .bestPlatformVoucher(bestChoice.getPlatformVoucher())
                .bestStoreVoucher(bestChoice.getStoreVoucher())
                .availableVouchers(available)
                .canApply(subtotal.compareTo(BigDecimal.ZERO) > 0)
                .message("Success")
                .build();
    }

    private List<VoucherResponse> mapAvailableVouchers(List<Voucher> eligible, BigDecimal subtotal, String userId) {
        LocalDateTime now = LocalDateTime.now();
        Set<String> collectedIds = getCollectedVoucherIds(userId, now);
        return eligible.stream()
                .map(voucher -> mapVoucherResponse(voucher, collectedIds.contains(voucher.getId()), subtotal, null))
                .sorted(Comparator.comparing(VoucherResponse::getPotentialDiscount, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    private VoucherBestChoiceResponse calculateBestChoice(List<Voucher> candidates, BigDecimal subtotal) {
        VoucherCandidate bestPlatform = findBestCandidate(candidates, "platform", subtotal);
        VoucherCandidate bestStore = findBestCandidate(candidates, "store", subtotal);

        BigDecimal platformOnlyDiscount = discountOf(bestPlatform);
        BigDecimal storeOnlyDiscount = discountOf(bestStore);
        boolean canStackPair = isStackablePair(
                bestPlatform != null ? bestPlatform.voucher() : null,
                bestStore != null ? bestStore.voucher() : null
        );
        BigDecimal stackedDiscount = canStackPair
                ? platformOnlyDiscount.add(storeOnlyDiscount)
                : BigDecimal.valueOf(-1);

        VoucherCandidate selectedPlatform;
        VoucherCandidate selectedStore;
        BigDecimal selectedDiscount;

        if (canStackPair
                && stackedDiscount.compareTo(platformOnlyDiscount) >= 0
                && stackedDiscount.compareTo(storeOnlyDiscount) >= 0) {
            selectedPlatform = bestPlatform;
            selectedStore = bestStore;
            selectedDiscount = stackedDiscount;
        } else if (platformOnlyDiscount.compareTo(storeOnlyDiscount) >= 0 && platformOnlyDiscount.compareTo(BigDecimal.ZERO) > 0) {
            selectedPlatform = bestPlatform;
            selectedStore = null;
            selectedDiscount = platformOnlyDiscount;
        } else if (storeOnlyDiscount.compareTo(BigDecimal.ZERO) > 0) {
            selectedPlatform = null;
            selectedStore = bestStore;
            selectedDiscount = storeOnlyDiscount;
        } else {
            selectedPlatform = null;
            selectedStore = null;
            selectedDiscount = BigDecimal.ZERO;
        }

        if (selectedDiscount.compareTo(subtotal) > 0) {
            selectedDiscount = subtotal;
        }

        return VoucherBestChoiceResponse.builder()
                .platformVoucher(selectedPlatform != null ? selectedPlatform.pricing() : null)
                .storeVoucher(selectedStore != null ? selectedStore.pricing() : null)
                .totalDiscount(scale(selectedDiscount))
                .build();
    }

    private List<Voucher> getEligibleCollectedVouchers(String userId, String storeId, BigDecimal subtotal, LocalDateTime now) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        Map<String, UserVoucher> collectedByVoucherId = userVoucherRepository.findActiveByUserId(userId, now).stream()
                .collect(Collectors.toMap(uv -> uv.getVoucher().getId(), Function.identity(), (a, b) -> a));

        return voucherRepository.findAvailableByStore(storeId, store.getMerchantId(), now).stream()
                .filter(voucher -> collectedByVoucherId.containsKey(voucher.getId()))
                .filter(voucher -> isVoucherEligible(voucher, userId, subtotal, now))
                .collect(Collectors.toList());
    }

    private Voucher resolveAppliedVoucher(
            String voucherId,
            String voucherCode,
            String expectedType,
            Map<String, Voucher> eligibleById,
            Map<String, Voucher> eligibleByCode
    ) {
        Voucher byCode = null;
        if (StringUtils.hasText(voucherCode)) {
            byCode = eligibleByCode.get(voucherCode.trim().toUpperCase(Locale.ROOT));
            if (byCode == null) {
                throw new AppException(ErrorCode.VOUCHER_NOT_ELIGIBLE);
            }
        }

        Voucher byId = null;
        if (StringUtils.hasText(voucherId)) {
            byId = eligibleById.get(voucherId.trim());
            if (byId == null) {
                throw new AppException(ErrorCode.VOUCHER_NOT_ELIGIBLE);
            }
        }

        Voucher selected = byCode != null ? byCode : byId;
        if (byCode != null && byId != null && !Objects.equals(byCode.getId(), byId.getId())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_ELIGIBLE);
        }
        if (selected != null && !expectedType.equalsIgnoreCase(selected.getVoucherType())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_ELIGIBLE);
        }
        return selected;
    }

    private Map<String, Voucher> toEligibleByCode(List<Voucher> eligible) {
        return eligible.stream()
                .filter(voucher -> StringUtils.hasText(voucher.getCode()))
                .collect(Collectors.toMap(
                        voucher -> voucher.getCode().trim().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (existing, ignored) -> existing
                ));
    }

    private VoucherCandidate findBestCandidate(List<Voucher> candidates, String voucherType, BigDecimal subtotal) {
        return candidates.stream()
                .filter(voucher -> voucherType.equalsIgnoreCase(voucher.getVoucherType()))
                .map(voucher -> new VoucherCandidate(voucher, toPricingResponse(voucher, subtotal)))
                .filter(candidate -> candidate.pricing() != null)
                .max(Comparator.comparing(
                        candidate -> normalizeAmount(candidate.pricing().getPotentialDiscount()),
                        BigDecimal::compareTo
                ))
                .orElse(null);
    }

    private BigDecimal discountOf(VoucherCandidate candidate) {
        if (candidate == null || candidate.pricing() == null) {
            return BigDecimal.ZERO;
        }
        return normalizeAmount(candidate.pricing().getPotentialDiscount());
    }

    private void validateStackablePair(Voucher platform, Voucher store) {
        if (!isStackablePair(platform, store)) {
            throw new AppException(ErrorCode.VOUCHER_NOT_ELIGIBLE);
        }
    }

    private boolean isStackablePair(Voucher platform, Voucher store) {
        // Cross-scope (store + platform) is always allowed.
        // UI enforces 1 voucher per scope, so no same-scope conflict is possible.
        return true;
    }

    private Voucher resolvePricingVoucher(VoucherPricingResponse pricingResponse, Map<String, Voucher> eligibleById) {
        if (pricingResponse == null || !StringUtils.hasText(pricingResponse.getVoucherId())) {
            return null;
        }
        return eligibleById.get(pricingResponse.getVoucherId());
    }

    private RemovalType resolveRemovalType(VoucherRemoveRequest request) {
        if (StringUtils.hasText(request.getType())) {
            String normalized = request.getType().trim().toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "platform" -> new RemovalType(true, false);
                case "store" -> new RemovalType(false, true);
                case "all" -> new RemovalType(true, true);
                default -> throw new AppException(ErrorCode.VOUCHER_NOT_ELIGIBLE);
            };
        }

        if (request.isRemovePlatform() || request.isRemoveStore()) {
            return new RemovalType(request.isRemovePlatform(), request.isRemoveStore());
        }

        return new RemovalType(true, true);
    }

    private BigDecimal calculateCartSubtotal(Cart cart) {
        return cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId()).stream()
                .map(item -> defaultAmount(item.getUnitPrice()).multiply(BigDecimal.valueOf(defaultQuantity(item))))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private int defaultQuantity(CartItem item) {
        return item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 0;
    }

    private VoucherResponse mapVoucherResponse(Voucher voucher, boolean isCollected, BigDecimal subtotal, UserVoucher userVoucher) {
        BigDecimal potentialDiscount = subtotal != null ? calculateDiscount(voucher, subtotal) : null;
        return VoucherResponse.builder()
                .id(voucher.getId())
                .voucherType(voucher.getVoucherType())
                .campaignId(voucher.getCampaignId())
                .merchantId(voucher.getMerchantId())
                .storeId(voucher.getStoreId())
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(scale(voucher.getDiscountValue()))
                .minOrderValue(scale(voucher.getMinOrderValue()))
                .maxDiscountValue(scale(voucher.getMaxDiscountValue()))
                .totalQuantity(voucher.getTotalQuantity())
                .usedQuantity(voucher.getUsedQuantity())
                .userUsageLimit(voucher.getUserUsageLimit())
                .isStackable(voucher.getIsStackable())
                .applicableTo(voucher.getApplicableTo())
                .startsAt(voucher.getStartsAt())
                .expiresAt(voucher.getExpiresAt())
                .isActive(voucher.getIsActive())
                .isCollected(isCollected)
                .isUsed(userVoucher != null ? userVoucher.getIsUsed() : null)
                .collectedAt(userVoucher != null ? userVoucher.getCollectedAt() : null)
                .potentialDiscount(scale(potentialDiscount))
                .build();
    }

    private VoucherPricingResponse toPricingResponse(Voucher voucher, BigDecimal subtotal) {
        if (voucher == null) {
            return null;
        }
        BigDecimal discount = calculateDiscount(voucher, subtotal);
        return VoucherPricingResponse.builder()
                .voucherId(voucher.getId())
                .code(voucher.getCode())
                .voucherType(voucher.getVoucherType())
                .discountType(voucher.getDiscountType())
                .discountValue(scale(voucher.getDiscountValue()))
                .potentialDiscount(scale(discount))
                .build();
    }

    private Set<String> getCollectedVoucherIds(String userId, LocalDateTime now) {
        if (!StringUtils.hasText(userId)) {
            return Collections.emptySet();
        }
        ensureUserExists(userId);
        return userVoucherRepository.findActiveByUserId(userId, now).stream()
                .map(uv -> uv.getVoucher().getId())
                .collect(Collectors.toSet());
    }

    private boolean isVoucherInScope(Voucher voucher, String storeId, String merchantId) {
        if (!StringUtils.hasText(storeId)) {
            return true;
        }
        if (StringUtils.hasText(voucher.getStoreId())) {
            return storeId.equals(voucher.getStoreId());
        }
        if ("platform".equalsIgnoreCase(voucher.getVoucherType())) {
            return true;
        }
        return !StringUtils.hasText(voucher.getMerchantId()) || voucher.getMerchantId().equals(merchantId);
    }

    private boolean isVoucherEligible(Voucher voucher, String userId, BigDecimal subtotal, LocalDateTime now) {
        if (!isVoucherActive(voucher, now)) {
            return false;
        }

        BigDecimal minOrder = normalizeAmount(voucher.getMinOrderValue());
        if (subtotal.compareTo(minOrder) < 0) {
            return false;
        }

        Integer totalQty = voucher.getTotalQuantity();
        int usedQty = voucher.getUsedQuantity() != null ? voucher.getUsedQuantity() : 0;
        if (totalQty != null && usedQty >= totalQty) {
            return false;
        }

        long usedByUser = userVoucherRepository.countUsedByUserAndVoucher(userId, voucher.getId());
        int perUserLimit = voucher.getUserUsageLimit() != null ? voucher.getUserUsageLimit() : 1;
        return usedByUser < perUserLimit;
    }

    private boolean isVoucherActive(Voucher voucher, LocalDateTime now) {
        if (!Boolean.TRUE.equals(voucher.getIsActive())) {
            return false;
        }
        if (voucher.getStartsAt() != null && voucher.getStartsAt().isAfter(now)) {
            return false;
        }
        return voucher.getExpiresAt() == null || !voucher.getExpiresAt().isBefore(now);
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal subtotal) {
        BigDecimal safeSubtotal = normalizeAmount(subtotal);
        BigDecimal minOrder = normalizeAmount(voucher.getMinOrderValue());
        if (safeSubtotal.compareTo(minOrder) < 0) {
            return BigDecimal.ZERO;
        }

        String discountType = voucher.getDiscountType() != null ? voucher.getDiscountType().toLowerCase(Locale.ROOT) : "";
        BigDecimal discount;
        if ("percentage".equals(discountType)) {
            discount = safeSubtotal.multiply(normalizeAmount(voucher.getDiscountValue()))
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal max = normalizeAmount(voucher.getMaxDiscountValue());
            if (max.compareTo(BigDecimal.ZERO) > 0 && discount.compareTo(max) > 0) {
                discount = max;
            }
        } else if ("fixed".equals(discountType) || "free_ship".equals(discountType)) {
            discount = normalizeAmount(voucher.getDiscountValue());
        } else {
            discount = BigDecimal.ZERO;
        }

        if (discount.compareTo(safeSubtotal) > 0) {
            discount = safeSubtotal;
        }
        return scale(discount);
    }

    private void ensureUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private record VoucherCandidate(Voucher voucher, VoucherPricingResponse pricing) {
    }

    private record RemovalType(boolean removePlatform, boolean removeStore) {
    }
}
