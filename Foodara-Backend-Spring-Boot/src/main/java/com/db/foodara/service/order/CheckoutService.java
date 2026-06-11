package com.db.foodara.service.order;

import com.db.foodara.dto.request.order.CheckoutPreviewRequest;
import com.db.foodara.dto.request.promotion.VoucherApplyRequest;
import com.db.foodara.dto.response.order.CartValidationResponse;
import com.db.foodara.dto.response.order.CheckoutDeliveryFeeResponse;
import com.db.foodara.dto.response.order.CheckoutPreviewResponse;
import com.db.foodara.dto.response.promotion.VoucherCartPricingResponse;
import com.db.foodara.entity.order.Cart;
import com.db.foodara.entity.order.CartItem;
import com.db.foodara.entity.store.Store;
import com.db.foodara.entity.user.UserAddress;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.order.CartItemRepository;
import com.db.foodara.repository.order.CartRepository;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.service.order.DeliveryQuoteService.DeliveryQuote;
import com.db.foodara.repository.user.UserAddressRepository;
import com.db.foodara.repository.user.UserRepository;
import com.db.foodara.service.promotion.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final BigDecimal PLATFORM_FEE_PERCENT = BigDecimal.valueOf(0.03);
    private static final BigDecimal PLATFORM_FEE_MIN = BigDecimal.valueOf(2000);
    private static final BigDecimal PLATFORM_FEE_MAX = BigDecimal.valueOf(10000);

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final UserAddressRepository userAddressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final VoucherService voucherService;
    private final DeliveryQuoteService deliveryQuoteService;

    public CheckoutPreviewResponse preview(String userId, CheckoutPreviewRequest request) {
        ensureUserExists(userId);

        Cart cart = cartRepository.findByUserIdAndStoreId(userId, request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        BigDecimal subtotal = calculateCartSubtotal(cart);

        CheckoutDeliveryFeeResponse deliveryFee = calculateDeliveryFee(userId, request.getStoreId(), request.getAddressId());
        VoucherCartPricingResponse voucherPricing = resolveVoucherPricing(userId, request);
        CartValidationResponse validation = cartService.validateCart(userId, null);

        BigDecimal platformDiscount = amount(voucherPricing.getAppliedPlatformVoucher() != null
                ? voucherPricing.getAppliedPlatformVoucher().getPotentialDiscount()
                : null);
        BigDecimal storeDiscount = amount(voucherPricing.getAppliedStoreVoucher() != null
                ? voucherPricing.getAppliedStoreVoucher().getPotentialDiscount()
                : null);
        BigDecimal totalDiscount = amount(voucherPricing.getTotalDiscount());
        if (totalDiscount.compareTo(subtotal) > 0) {
            totalDiscount = subtotal;
        }

        BigDecimal subtotalAfterVoucher = subtotal.subtract(totalDiscount).max(BigDecimal.ZERO);
        BigDecimal platformFee = calculatePlatformFee(subtotalAfterVoucher);
        BigDecimal totalAmount = subtotalAfterVoucher
                .add(amount(deliveryFee.getDeliveryFee()))
                .add(platformFee);

        return CheckoutPreviewResponse.builder()
                .storeId(request.getStoreId())
                .addressId(deliveryFee.getAddressId())
                .subtotal(scale(subtotal))
                .subtotalAfterVoucher(scale(subtotalAfterVoucher))
                .deliveryFee(scale(deliveryFee.getDeliveryFee()))
                .distanceKm(deliveryFee.getDistanceKm())
                .etaMinutes(deliveryFee.getEtaMinutes())
                .surgeMultiplier(deliveryFee.getSurgeMultiplier())
                .platformFee(scale(platformFee))
                .platformDiscount(scale(platformDiscount))
                .storeDiscount(scale(storeDiscount))
                .totalDiscount(scale(totalDiscount))
                .totalAmount(scale(totalAmount))
                .appliedPlatformVoucher(voucherPricing.getAppliedPlatformVoucher())
                .appliedStoreVoucher(voucherPricing.getAppliedStoreVoucher())
                .canCheckout(Boolean.TRUE.equals(validation.getValid()))
                .issues(validation.getIssues())
                .build();
    }

    public CheckoutDeliveryFeeResponse getDeliveryFee(String userId, String storeId, String addressId) {
        ensureUserExists(userId);
        return calculateDeliveryFee(userId, storeId, addressId);
    }

    public CheckoutDeliveryFeeResponse getDeliveryFeeByCoords(String storeId, BigDecimal lat, BigDecimal lng) {
        DeliveryQuote quote = deliveryQuoteService.quoteByCoords(storeId, lat, lng);
        return CheckoutDeliveryFeeResponse.builder()
                .storeId(quote.storeId())
                .addressId(null)
                .distanceKm(quote.distanceKm())
                .deliveryFee(quote.deliveryFee())
                .etaMinutes(quote.etaMinutes())
                .surgeMultiplier(quote.surgeMultiplier())
                .build();
    }

    public java.util.List<CheckoutDeliveryFeeResponse> getDeliveryFeeBatch(java.util.List<String> storeIds, BigDecimal lat, BigDecimal lng) {
        java.util.List<DeliveryQuote> quotes = deliveryQuoteService.quoteBatch(storeIds, lat, lng);
        java.util.List<CheckoutDeliveryFeeResponse> out = new java.util.ArrayList<>();
        for (int i = 0; i < storeIds.size(); i++) {
            DeliveryQuote q = i < quotes.size() ? quotes.get(i) : null;
            if (q == null) {
                out.add(CheckoutDeliveryFeeResponse.builder().storeId(storeIds.get(i)).build());
            } else {
                out.add(CheckoutDeliveryFeeResponse.builder()
                        .storeId(q.storeId()).addressId(null)
                        .distanceKm(q.distanceKm()).deliveryFee(q.deliveryFee())
                        .etaMinutes(q.etaMinutes()).surgeMultiplier(q.surgeMultiplier())
                        .build());
            }
        }
        return out;
    }

    private CheckoutDeliveryFeeResponse calculateDeliveryFee(String userId, String storeId, String addressId) {
        DeliveryQuote quote = deliveryQuoteService.quote(storeId, userId, addressId);
        return CheckoutDeliveryFeeResponse.builder()
                .storeId(quote.storeId())
                .addressId(quote.addressId())
                .distanceKm(quote.distanceKm())
                .deliveryFee(quote.deliveryFee())
                .etaMinutes(quote.etaMinutes())
                .surgeMultiplier(quote.surgeMultiplier())
                .build();
    }

    private UserAddress resolveAddress(String userId, String addressId) {
        if (StringUtils.hasText(addressId)) {
            return userAddressRepository.findByIdAndUserId(addressId, userId)
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        }
        return userAddressRepository.findByUserIdAndIsDefaultTrue(userId).orElse(null);
    }

    private VoucherCartPricingResponse resolveVoucherPricing(String userId, CheckoutPreviewRequest request) {
        boolean hasManualSelection = StringUtils.hasText(request.getPlatformCode())
                || StringUtils.hasText(request.getStoreCode())
                || StringUtils.hasText(request.getPlatformVoucherId())
                || StringUtils.hasText(request.getStoreVoucherId());

        if (!hasManualSelection) {
            return voucherService.getAvailableForCart(userId, request.getStoreId());
        }

        VoucherApplyRequest applyRequest = new VoucherApplyRequest();
        applyRequest.setStoreId(request.getStoreId());
        applyRequest.setPlatformCode(request.getPlatformCode());
        applyRequest.setStoreCode(request.getStoreCode());
        applyRequest.setPlatformVoucherId(request.getPlatformVoucherId());
        applyRequest.setStoreVoucherId(request.getStoreVoucherId());
        return voucherService.applyVouchersForCart(userId, applyRequest);
    }

    private BigDecimal calculateCartSubtotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        return items.stream()
                .map(item -> amount(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePlatformFee(BigDecimal subtotalAfterVoucher) {
        BigDecimal computed = amount(subtotalAfterVoucher).multiply(PLATFORM_FEE_PERCENT).setScale(2, RoundingMode.HALF_UP);
        if (computed.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (computed.compareTo(PLATFORM_FEE_MIN) < 0) {
            return PLATFORM_FEE_MIN;
        }
        if (computed.compareTo(PLATFORM_FEE_MAX) > 0) {
            return PLATFORM_FEE_MAX;
        }
        return computed;
    }

    private void ensureUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }


    private BigDecimal amount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}



