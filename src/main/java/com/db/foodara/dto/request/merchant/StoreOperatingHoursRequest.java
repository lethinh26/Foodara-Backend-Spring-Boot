package com.db.foodara.dto.request.merchant;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class StoreOperatingHoursRequest {
    @NotNull(message = "Day of week is required")
    private Integer dayOfWeek;

    @NotNull(message = "Open time is required")
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    private LocalTime closeTime;

    private Boolean isClosed;
}
