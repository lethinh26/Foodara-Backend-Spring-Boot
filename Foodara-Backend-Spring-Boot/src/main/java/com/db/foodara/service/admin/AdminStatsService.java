package com.db.foodara.service.admin;

import com.db.foodara.dto.response.admin.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStatsService {

    private final JdbcTemplate jdbc;

    // Dashboard tổng hợp
    public DashboardSummaryResponse getDashboard(LocalDate from, LocalDate to) {
        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(6);

        DailyStatsResponse todayStats = buildSingleDayStats(endDate);
        DailyStatsResponse previousStats = buildSingleDayStats(endDate.minusDays(1));

        return DashboardSummaryResponse.builder()
                .today(todayStats)
                .previous(previousStats)
                .revenueByDay(getRevenueByDay(startDate, endDate))
                .ordersByStatus(getOrdersByStatus(startDate, endDate))
                .topRestaurants(getTopRestaurants(startDate, endDate, 5))
                .topItems(getTopItems(startDate, endDate, 5))
                .build();
    }

    // Thống kê theo ngày — batch query thay vì loop từng ngày
    public List<DailyStatsResponse> getDailyStats(LocalDate from, LocalDate to) {
        String sql = """
                WITH date_range AS (
                    SELECT generate_series(?::date, ?::date, '1 day'::interval)::date AS d
                ),
                order_stats AS (
                    SELECT placed_at::date AS d,
                           COUNT(*) AS total,
                           COUNT(*) FILTER (WHERE status = 'completed') AS completed,
                           COUNT(*) FILTER (WHERE status = 'cancelled') AS cancelled,
                           COALESCE(SUM(total_amount), 0) AS gmv,
                           COALESCE(SUM(commission_amount), 0) AS revenue
                    FROM orders WHERE placed_at::date BETWEEN ? AND ?
                    GROUP BY placed_at::date
                ),
                new_users AS (
                    SELECT created_at::date AS d, COUNT(*) AS cnt
                    FROM users WHERE created_at::date BETWEEN ? AND ?
                    GROUP BY created_at::date
                ),
                new_stores AS (
                    SELECT created_at::date AS d, COUNT(*) AS cnt
                    FROM stores WHERE created_at::date BETWEEN ? AND ?
                    GROUP BY created_at::date
                ),
                new_drivers AS (
                    SELECT created_at::date AS d, COUNT(*) AS cnt
                    FROM drivers WHERE created_at::date BETWEEN ? AND ?
                    GROUP BY created_at::date
                ),
                active_counts AS (
                    SELECT placed_at::date AS d,
                           COUNT(DISTINCT customer_id) AS active_users,
                           COUNT(DISTINCT store_id) AS active_stores,
                           COUNT(DISTINCT driver_id) FILTER (WHERE driver_id IS NOT NULL) AS active_drivers
                    FROM orders WHERE placed_at::date BETWEEN ? AND ?
                    GROUP BY placed_at::date
                )
                SELECT dr.d AS stat_date,
                       COALESCE(os.total, 0) AS total_orders,
                       COALESCE(os.completed, 0) AS total_completed,
                       COALESCE(os.cancelled, 0) AS total_cancelled,
                       COALESCE(os.gmv, 0) AS total_gmv,
                       COALESCE(os.revenue, 0) AS total_revenue,
                       COALESCE(nu.cnt, 0) AS new_users,
                       COALESCE(ns.cnt, 0) AS new_stores,
                       COALESCE(nd.cnt, 0) AS new_drivers,
                       COALESCE(ac.active_users, 0) AS active_users,
                       COALESCE(ac.active_stores, 0) AS active_stores,
                       COALESCE(ac.active_drivers, 0) AS active_drivers
                FROM date_range dr
                LEFT JOIN order_stats os ON dr.d = os.d
                LEFT JOIN new_users nu ON dr.d = nu.d
                LEFT JOIN new_stores ns ON dr.d = ns.d
                LEFT JOIN new_drivers nd ON dr.d = nd.d
                LEFT JOIN active_counts ac ON dr.d = ac.d
                ORDER BY dr.d
                """;

        Date f = Date.valueOf(from);
        Date t = Date.valueOf(to);

        return jdbc.query(sql, (rs, rowNum) -> {
            int total = rs.getInt("total_orders");
            int cancelled = rs.getInt("total_cancelled");
            BigDecimal gmv = rs.getBigDecimal("total_gmv");
            double cancelRate = total > 0 ? (double) cancelled / total * 100 : 0;

            return DailyStatsResponse.builder()
                    .statDate(rs.getDate("stat_date").toLocalDate())
                    .totalOrders(total)
                    .totalCompletedOrders(rs.getInt("total_completed"))
                    .totalCancelledOrders(cancelled)
                    .totalGmv(gmv)
                    .totalRevenue(rs.getBigDecimal("total_revenue"))
                    .avgOrderValue(total > 0 ? gmv.divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .avgDeliveryTimeMinutes(0)
                    .cancellationRate(Math.round(cancelRate * 100.0) / 100.0)
                    .newUsers(rs.getInt("new_users"))
                    .newStores(rs.getInt("new_stores"))
                    .newDrivers(rs.getInt("new_drivers"))
                    .activeUsers(rs.getInt("active_users"))
                    .activeStores(rs.getInt("active_stores"))
                    .activeDrivers(rs.getInt("active_drivers"))
                    .build();
        }, f, t, f, t, f, t, f, t, f, t, f, t);
    }

    // Stats cho 1 ngày (dùng cho dashboard today/previous)
    private DailyStatsResponse buildSingleDayStats(LocalDate day) {
        Date d = Date.valueOf(day);

        // 1 query lấy order stats
        var orderStats = jdbc.queryForMap(
                """
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE status = 'completed') AS completed,
                       COUNT(*) FILTER (WHERE status = 'cancelled') AS cancelled,
                       COALESCE(SUM(total_amount), 0) AS gmv,
                       COALESCE(SUM(commission_amount), 0) AS revenue
                FROM orders WHERE placed_at::date = ?
                """, d);

        int total = ((Number) orderStats.get("total")).intValue();
        int completed = ((Number) orderStats.get("completed")).intValue();
        int cancelled = ((Number) orderStats.get("cancelled")).intValue();
        BigDecimal gmv = (BigDecimal) orderStats.get("gmv");
        BigDecimal revenue = (BigDecimal) orderStats.get("revenue");
        double cancelRate = total > 0 ? (double) cancelled / total * 100 : 0;

        // 1 query lấy active counts
        var activeCounts = jdbc.queryForMap(
                """
                SELECT COUNT(DISTINCT customer_id) AS users,
                       COUNT(DISTINCT store_id) AS stores,
                       COUNT(DISTINCT driver_id) FILTER (WHERE driver_id IS NOT NULL) AS drivers
                FROM orders WHERE placed_at::date = ?
                """, d);

        return DailyStatsResponse.builder()
                .statDate(day)
                .totalOrders(total)
                .totalCompletedOrders(completed)
                .totalCancelledOrders(cancelled)
                .totalGmv(gmv)
                .totalRevenue(revenue)
                .avgOrderValue(total > 0 ? gmv.divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .avgDeliveryTimeMinutes(0)
                .cancellationRate(Math.round(cancelRate * 100.0) / 100.0)
                .newUsers(countCreated("users", d))
                .newStores(countCreated("stores", d))
                .newDrivers(countCreated("drivers", d))
                .activeUsers(((Number) activeCounts.get("users")).intValue())
                .activeStores(((Number) activeCounts.get("stores")).intValue())
                .activeDrivers(((Number) activeCounts.get("drivers")).intValue())
                .build();
    }

    private int countCreated(String table, Date day) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE created_at::date = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, day);
        return count != null ? count : 0;
    }

    // Revenue theo ngày
    private List<ChartDataPointResponse> getRevenueByDay(LocalDate from, LocalDate to) {
        String sql = """
                SELECT placed_at::date AS d,
                       COALESCE(SUM(total_amount), 0) AS gmv,
                       COALESCE(SUM(commission_amount), 0) AS rev
                FROM orders
                WHERE placed_at::date BETWEEN ? AND ?
                GROUP BY d ORDER BY d
                """;
        return jdbc.query(sql, (rs, rowNum) -> ChartDataPointResponse.builder()
                .label(rs.getDate("d").toLocalDate().toString())
                .value(rs.getBigDecimal("gmv"))
                .value2(rs.getBigDecimal("rev"))
                .build(), Date.valueOf(from), Date.valueOf(to));
    }

    // Phân bố đơn theo status
    private List<ChartDataPointResponse> getOrdersByStatus(LocalDate from, LocalDate to) {
        String sql = """
                SELECT status, COUNT(*) AS cnt
                FROM orders
                WHERE placed_at::date BETWEEN ? AND ?
                GROUP BY status ORDER BY cnt DESC
                """;
        return jdbc.query(sql, (rs, rowNum) -> ChartDataPointResponse.builder()
                .label(rs.getString("status"))
                .value(BigDecimal.valueOf(rs.getLong("cnt")))
                .build(), Date.valueOf(from), Date.valueOf(to));
    }

    // Top N quán theo doanh thu
    private List<TopRankingResponse> getTopRestaurants(LocalDate from, LocalDate to, int limit) {
        String sql = """
                SELECT s.name, COUNT(o.id) AS orders, COALESCE(SUM(o.total_amount), 0) AS revenue
                FROM orders o JOIN stores s ON o.store_id = s.id
                WHERE o.placed_at::date BETWEEN ? AND ? AND o.status = 'completed'
                GROUP BY s.id, s.name ORDER BY revenue DESC LIMIT ?
                """;
        return jdbc.query(sql, (rs, rowNum) -> TopRankingResponse.builder()
                .name(rs.getString("name"))
                .orders(rs.getInt("orders"))
                .revenue(rs.getBigDecimal("revenue"))
                .build(), Date.valueOf(from), Date.valueOf(to), limit);
    }

    // Top N món bán chạy theo doanh thu
    private List<TopRankingResponse> getTopItems(LocalDate from, LocalDate to, int limit) {
        String sql = """
                SELECT oi.item_name AS name, SUM(oi.quantity) AS sold,
                       COALESCE(SUM(oi.total_price), 0) AS revenue
                FROM order_items oi JOIN orders o ON oi.order_id = o.id
                WHERE o.placed_at::date BETWEEN ? AND ? AND o.status = 'completed'
                GROUP BY oi.item_name ORDER BY revenue DESC LIMIT ?
                """;
        return jdbc.query(sql, (rs, rowNum) -> TopRankingResponse.builder()
                .name(rs.getString("name"))
                .sold(rs.getInt("sold"))
                .revenue(rs.getBigDecimal("revenue"))
                .build(), Date.valueOf(from), Date.valueOf(to), limit);
    }
}
