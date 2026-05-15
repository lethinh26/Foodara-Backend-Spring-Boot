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

    private static final BigDecimal BASE_DELIVERY_FEE = BigDecimal.valueOf(15000);
    private static final BigDecimal EXTRA_FEE_PER_KM = BigDecimal.valueOf(3000);
    private static final BigDecimal FREE_BASE_DISTANCE_KM = BigDecimal.valueOf(2);
    private static final BigDecimal MAX_DELIVERY_FEE = BigDecimal.valueOf(60000);
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

    public CheckoutPreviewResponse preview(String userId, CheckoutPreviewRequest request) {
        ensureUserExists(userId);

        Cart cart = cartRepository.findByUserIdAndStoreId(userId, request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        BigDecimal subtotal = calculateCartSubtotal(cart);

        CheckoutDeliveryFeeResponse deliveryFee = calculateDeliveryFee(userId, request.getStoreId(), request.getAddressId());
        VoucherCartPricingResponse voucherPricing = resolveVoucherPricing(userId, request);
        CartValidationResponse validation = cartService.validateCart(userId);

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

    private CheckoutDeliveryFeeResponse calculateDeliveryFee(String userId, String storeId, String addressId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        UserAddress address = resolveAddress(userId, addressId);

        BigDecimal distanceKm = BigDecimal.ZERO;
        BigDecimal fee = BASE_DELIVERY_FEE;

        if (address != null
                && address.getLatitude() != null
                && address.getLongitude() != null
                && store.getLatitude() != null
                && store.getLongitude() != null) {
            distanceKm = haversineKm(
                    store.getLatitude().doubleValue(),
                    store.getLongitude().doubleValue(),
                    address.getLatitude().doubleValue(),
                    address.getLongitude().doubleValue()
            );

            BigDecimal extraDistance = distanceKm.subtract(FREE_BASE_DISTANCE_KM);
            if (extraDistance.compareTo(BigDecimal.ZERO) > 0) {
                fee = fee.add(extraDistance.multiply(EXTRA_FEE_PER_KM));
            }
            if (fee.compareTo(MAX_DELIVERY_FEE) > 0) {
                fee = MAX_DELIVERY_FEE;
            }
        }

        return CheckoutDeliveryFeeResponse.builder()
                .storeId(storeId)
                .addressId(address != null ? address.getId() : null)
                .distanceKm(scale(distanceKm.max(BigDecimal.ZERO)))
                .deliveryFee(scale(fee))
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

    private BigDecimal haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadius * c);
    }

    private BigDecimal amount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
