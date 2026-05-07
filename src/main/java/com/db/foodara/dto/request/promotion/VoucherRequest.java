package com.db.foodara.dto.request.promotion;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VoucherRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Mã voucher không được để trống")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã voucher chỉ gồm chữ hoa và số")
    private String code;

    private String description;

    @NotBlank(message = "Loại giảm giá là bắt buộc")
    @Pattern(regexp = "percentage|fixed", message = "Loại giảm giá phải là 'percentage' hoặc 'fixed'")
    private String discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Giá trị đơn tối thiểu không được âm")
    private BigDecimal minOrderValue;

    private BigDecimal maxDiscountValue;

    @NotNull(message = "Số lượng là bắt buộc")
    @Min(value = 1, message = "Số lượng ít nhất phải là 1")
    private Integer totalQuantity;

    @NotBlank(message = "Store ID là bắt buộc")
    private String storeId;

    @NotNull(message = "Ngày bắt đầu là bắt buộc")
    @FutureOrPresent(message = "Ngày bắt đầu không được ở quá khứ")
    private LocalDateTime startsAt;

    @NotNull(message = "Ngày hết hạn là bắt buộc")
    private LocalDateTime expiresAt;

    private Boolean isActive;
}