package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderAssignmentResponse {
    private String id;
    private String orderId;
    private String driverId;
    private String driverName;
    private String assignmentType;
    private String status;
    private Double distanceToStoreKm;
    private LocalDateTime responseDeadline;
    private LocalDateTime createdAt;
}
