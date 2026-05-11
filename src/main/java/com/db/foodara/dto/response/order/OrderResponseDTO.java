package com.db.foodara.dto.response.order;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private String id;
    private String orderNumber;
    private String storeId;
    private String storeName;
    private String storeAddress;
    private String storeLogoUrl; // Lấy từ logic bổ trợ hoặc Snapshot

    private String customerId;
    private String status; // OrderStatus trong TS

    // Thông tin Driver
    private String driverId;
    private String driverName;
    private String driverPhone;

    // Items (Cần một DTO riêng cho Item vì trong DB bạn có thể lưu ở bảng OrderItem)
    private List<OrderItemResponseDTO> items;

    // Địa chỉ (Parse từ deliveryAddressSnapshot hoặc các trường Lat/Lng)
    private AddressDTO deliveryAddress;

    // Pricing (Khớp với CheckoutPricing interface)
    private PricingDTO pricing;

    private String paymentMethod;
    private String paymentStatus;

    private Integer estimatedDeliveryTime;
    private String note;
    private String pickupCode;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    // Hủy đơn
    private String cancelReason;
    private String cancelledBy;

    @Data
    @Builder
    public static class PricingDTO {
        private BigDecimal subtotal;
        private BigDecimal deliveryFee;
        private BigDecimal platformFee;
        private BigDecimal discount; // Tổng discount (store + voucher)
        private BigDecimal voucherDiscount;
        private BigDecimal total;
        private List<PriceBreakdownItemDTO> breakdown;
    }

    @Data
    @AllArgsConstructor
    public static class PriceBreakdownItemDTO {
        private String label;
        private BigDecimal amount;
        private String type; // "add", "subtract", "total"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponseDTO {
        private String id;
        private String menuItemId;
        private String comboId;
        private String name;
        private String image;
        private Integer quantity;
        private BigDecimal price;      // Đây là unitPrice (giá 1 món)
        private BigDecimal totalPrice; // Tổng giá (unitPrice * quantity)
        private String note;

        // Trường này để chứa dữ liệu JSON sau khi parse
        private Object selectedOptions;
    }

    @Data
    @Builder
    public static class AddressDTO {
        private String id;
        private String formattedAddress;
        private String street;
        private String city;
        private Double latitude;
        private Double longitude;
    }
}