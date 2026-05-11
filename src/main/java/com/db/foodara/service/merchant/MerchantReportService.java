package com.db.foodara.service.merchant;

import com.db.foodara.dto.response.order.DailyRevenueResponse;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.order.OrderItem;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.order.OrderItemRepository;
import com.db.foodara.repository.order.OrderRepository;
import lombok.AllArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MerchantReportService {
    private final MerchantRepository merchantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    // doanh thu
    public double getAllRevenue(String merchantId, String storeId) {
        // Kiểm tra merchant và store (giữ nguyên logic của bạn)
        merchantRepository.findByOwnerId(merchantId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        List<Order> orders = orderRepository.findByStoreId(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return orders.stream()
                .flatMap(o -> orderItemRepository.findByOrderId(o.getId()).stream()) // Trải phẳng danh sách item
                .mapToDouble(oi -> oi.getTotalPrice().doubleValue()) // Chuyển sang double
                .sum(); // Tính tổng cực nhanh và gọn
    }


    public int getTotalOrder(String merchantId, String storeId) {
        merchantRepository.findByOwnerId(merchantId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        int length = 0;
        List<Order> orders = orderRepository.findByStoreId(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return orders.size();
    }

    public double getAVGTime(String merchantId, String storeId) {
        merchantRepository.findByOwnerId(merchantId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        List<Order> orders = orderRepository.findByStoreId(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getUpdatedAt() != null)
                .mapToLong(o -> {
                    Duration duration = Duration.between(o.getCreatedAt(), o.getUpdatedAt());
                    return duration.toMinutes();})
                .average() // Hàm này trả về OptionalDouble
                .orElse(0.0);
    }

    public double getSuccessOrderRate(String merchantId, String storeId) {
        merchantRepository.findByOwnerId(merchantId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        List<Order> orders = orderRepository.findByStoreId(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if (orders.isEmpty()) {
            return 0.0;
        }
        long successCount = orders.stream()
                .filter(o -> "SUCCESS".equalsIgnoreCase(o.getStatus())) // Hoặc o.getStatus() == OrderStatus.SUCCESS
                .count();
        return ((double) successCount / orders.size()) * 100;
    }

    public List<DailyRevenueResponse> getWeeklyRevenue(String merchantId, String storeId, LocalDateTime start, LocalDateTime end) {
        merchantRepository.findByOwnerId(merchantId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        boolean isDefault7Days = (start == null || end == null);
        LocalDateTime actualStart = isDefault7Days ? LocalDate.now().minusDays(6).atStartOfDay() : start;
        LocalDateTime actualEnd = isDefault7Days ? LocalDateTime.now() : end;

        List<Order> validOrders = orderRepository.getOrderByStoreId(storeId).stream()
                .filter(o -> o.getUpdatedAt() != null &&
                        !o.getUpdatedAt().isBefore(actualStart) &&
                        !o.getUpdatedAt().isAfter(actualEnd) &&
                        "completed".equalsIgnoreCase(o.getStatus()))
                .toList();

        Map<LocalDate, List<Order>> ordersByDate = validOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getUpdatedAt().toLocalDate()));

        List<DailyRevenueResponse> responses = new ArrayList<>();
        LocalDate startDateLoop = actualStart.toLocalDate();
        LocalDate endDateLoop = actualEnd.toLocalDate();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (LocalDate date = startDateLoop; !date.isAfter(endDateLoop); date = date.plusDays(1)) {
            List<Order> ordersOfDay = ordersByDate.getOrDefault(date, Collections.emptyList());

            double dailyRevenue = ordersOfDay.stream()
                    // Duyệt từng Order (o), gọi DB lấy List<OrderItem>, biến nó thành Stream
                    .flatMap(o -> orderItemRepository.findByOrderId(o.getId()).stream())
                    // Lấy ra giá tiền của từng Item và chuyển sang double
                    .mapToDouble(oi -> oi.getTotalPrice().doubleValue())
                    // Cộng tổng lại
                    .sum();

            String label = isDefault7Days ? getDayOfWeekLabel(date) : date.format(dateFormatter);

            responses.add(DailyRevenueResponse.builder()
                    .day(label)
                    .revenue(BigDecimal.valueOf(dailyRevenue))
                    .orders((long) ordersOfDay.size())
                    .build());
        }

        return responses;
    }
    private String getDayOfWeekLabel(LocalDate date) {
        int dayValue = date.getDayOfWeek().getValue();
        return (dayValue == 7) ? "CN" : "T" + (dayValue + 1);
    }
}
