package com.db.foodara.driver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverIncentiveProgramResponse {
    private String id;
    private String name;
    private String description;
    private String targetType;
    private Integer targetValue;
    private BigDecimal bonusAmount;
    private Integer maxParticipants;
    private Long currentParticipants;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
