package com.db.foodara.driver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDriverResponse {
    private String id;
    private String userId;
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private String idNumber;
    private String vehicleType;
    private String vehiclePlate;
    private String vehicleBrand;
    private String vehicleColor;
    private String approvalStatus;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private Boolean isOnline;
    private Boolean isBusy;
    private BigDecimal avgRating;
    private Integer totalRatings;
    private Integer totalDeliveries;
    private BigDecimal acceptanceRate;
    private BigDecimal completionRate;
    private BigDecimal walletBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
