package com.db.foodara.dto.response.order;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record DailyRevenueResponse(
        String day,
        BigDecimal revenue,
        long orders
) {}