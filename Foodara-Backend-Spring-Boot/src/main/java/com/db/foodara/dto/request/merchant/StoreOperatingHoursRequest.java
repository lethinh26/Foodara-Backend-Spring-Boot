package com.db.foodara.dto.request.merchant;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreOperatingHoursRequest {
    @NotNull(message = "Day of week is required")
    private Integer dayOfWeek;

    /** Required when the day is open; ignored if {@code isClosed=true}. */
    private LocalTime openTime;

    /** Required when the day is open; ignored if {@code isClosed=true}. */
    private LocalTime closeTime;

    private Boolean isClosed;
}
