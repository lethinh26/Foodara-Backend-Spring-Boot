package com.db.foodara.driver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverIncentiveProgressResponse {
    private String id;
    private String programId;
    private String driverId;
    private String driverName;
    private Integer currentValue;
    private Integer targetValue;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private Boolean bonusPaid;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
