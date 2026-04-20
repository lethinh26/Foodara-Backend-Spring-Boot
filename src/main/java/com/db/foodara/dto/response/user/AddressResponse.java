package com.db.foodara.dto.response.user;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String id;
    private String label;
    private String recipientName;
    private String recipientPhone;
    private String addressLine;
    private String ward;
    private String districtId;
    private String cityId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String deliveryNote;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
