package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardSummaryResponse {
    private DailyStatsResponse today;
    private DailyStatsResponse previous;
    private List<ChartDataPointResponse> revenueByDay;
    private List<ChartDataPointResponse> ordersByStatus;
    private List<TopRankingResponse> topRestaurants;
    private List<TopRankingResponse> topItems;
}
