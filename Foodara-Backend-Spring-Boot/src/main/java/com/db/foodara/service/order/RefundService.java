package com.db.foodara.service.order;

import com.db.foodara.config.RabbitMQConfig;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.promotion.Voucher;
import com.db.foodara.entity.promotion.UserVoucher;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.promotion.VoucherRepository;
import com.db.foodara.repository.promotion.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Transactional
    public Map<String, Object> processRefund(String orderId, String type) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"paid".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new RuntimeException("Order is not paid");
        }

        if ("voucher".equalsIgnoreCase(type)) {
            // Create a personal platform voucher with value = order total
            Voucher voucher = new Voucher();
            voucher.setVoucherType("platform");
            voucher.setCode("REFUND-" + order.getOrderNumber());
            voucher.setTitle("Hoàn tiền đơn " + order.getOrderNumber());
            voucher.setDescription("Voucher hoàn tiền từ đơn hàng đã huỷ");
            voucher.setDiscountType("fixed");
            voucher.setDiscountValue(order.getTotalAmount());
            voucher.setMinOrderValue(BigDecimal.ZERO);
            voucher.setTotalQuantity(1);
            voucher.setUsedQuantity(0);
            voucher.setUserUsageLimit(1);
            voucher.setIsStackable(false);
            voucher.setApplicableTo("all");
            voucher.setIsActive(true);
            voucher.setStartsAt(LocalDateTime.now());
            voucher.setExpiresAt(LocalDateTime.now().plusDays(30));
            voucherRepository.save(voucher);

            // Assign to user
            UserVoucher userVoucher = new UserVoucher();
            userVoucher.setUserId(order.getCustomerId());
            userVoucher.setVoucher(voucher);
            userVoucher.setIsUsed(false);
            userVoucher.setExpiresAt(voucher.getExpiresAt());
            userVoucherRepository.save(userVoucher);

            log.info("Created refund voucher {} for order {} (user={}, value={})",
                    voucher.getCode(), order.getOrderNumber(), order.getCustomerId(), order.getTotalAmount());

            // Publish notification event
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.REFUND_VOUCHER_KEY,
                    Map.of("orderNumber", order.getOrderNumber(),
                            "customerId", order.getCustomerId(),
                            "amount", order.getTotalAmount(),
                            "voucherCode", voucher.getCode(),
                            "type", "voucher"));

            return Map.of(
                "success", true,
                "voucherCode", voucher.getCode(),
                "discountValue", order.getTotalAmount()
            );
        }

        // Bank refund: just acknowledge
        log.info("Bank refund acknowledged for order {} ({})", order.getOrderNumber(), order.getTotalAmount());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.REFUND_BANK_KEY,
                Map.of("orderNumber", order.getOrderNumber(),
                        "customerId", order.getCustomerId(),
                        "amount", order.getTotalAmount(),
                        "type", "bank"));
        return Map.of("success", true, "message", "Bank refund acknowledged");
    }
}
