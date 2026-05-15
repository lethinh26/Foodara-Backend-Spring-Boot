package com.db.foodara.service.admin;

import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.*;
import com.db.foodara.entity.driver.Driver;
import com.db.foodara.entity.merchant.Merchant;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.settlement.DriverSettlement;
import com.db.foodara.entity.settlement.StoreSettlement;
import com.db.foodara.entity.settlement.StoreSettlementItem;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.driver.DriverRepository;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.settlement.*;
import com.db.foodara.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSettlementService {

    private final StoreSettlementRepository storeSettlementRepo;
    private final StoreSettlementItemRepository storeSettlementItemRepo;
    private final DriverSettlementRepository driverSettlementRepo;
    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;
    private final DriverRepository driverRepository;

    private static final Set<String> VALID_STATUSES = Set.of("pending", "confirmed", "paid", "disputed");


    public PageResponse<StoreSettlementResponse> getStoreSettlements(int page, int size, String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StoreSettlement> settlementPage;

        if (status != null && !status.isBlank()) {
            settlementPage = storeSettlementRepo.findByStatus(status, pageRequest);
        } else {
            settlementPage = storeSettlementRepo.findAll(pageRequest);
        }

        List<StoreSettlementResponse> content = settlementPage.getContent().stream()
                .map(this::mapStoreSettlement)
                .toList();

        return PageResponse.<StoreSettlementResponse>builder()
                .content(content)
                .page(settlementPage.getNumber())
                .number(settlementPage.getNumber())
                .size(settlementPage.getSize())
                .totalElements(settlementPage.getTotalElements())
                .totalPages(settlementPage.getTotalPages())
                .last(settlementPage.isLast())
                .build();
    }

    public List<StoreSettlementItemResponse> getStoreSettlementItems(String settlementId) {
        if (!storeSettlementRepo.existsById(settlementId)) {
            throw new AppException(ErrorCode.SETTLEMENT_NOT_FOUND);
        }
        return storeSettlementItemRepo.findBySettlementId(settlementId).stream()
                .map(this::mapStoreSettlementItem)
                .toList();
    }

    @Transactional
    public StoreSettlementResponse generateStoreSettlement(Map<String, Object> request, String adminUserId) {
        String merchantId = (String) request.get("merchantId");
        String periodStartStr = (String) request.get("periodStart");
        String periodEndStr = (String) request.get("periodEnd");

        if (merchantId == null || periodStartStr == null || periodEndStr == null) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        if (!merchantRepository.existsById(merchantId)) {
            throw new AppException(ErrorCode.MERCHANT_NOT_FOUND);
        }

        LocalDate periodStart = LocalDate.parse(periodStartStr);
        LocalDate periodEnd = LocalDate.parse(periodEndStr);

        StoreSettlement settlement = new StoreSettlement();
        settlement.setMerchantId(merchantId);
        settlement.setPeriodStart(periodStart);
        settlement.setPeriodEnd(periodEnd);
        settlement.setCreatedBy(adminUserId);
        storeSettlementRepo.save(settlement);

        log.info("Admin {} generated store settlement for merchant {} period {}-{}", 
                adminUserId, merchantId, periodStart, periodEnd);

        return mapStoreSettlement(settlement);
    }

    @Transactional
    public void confirmStoreSettlement(String id, String adminUserId) {
        StoreSettlement settlement = storeSettlementRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SETTLEMENT_NOT_FOUND));

        if (!"pending".equals(settlement.getStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        settlement.setStatus("confirmed");
        settlement.setConfirmedBy(adminUserId);
        storeSettlementRepo.save(settlement);

        log.info("Admin {} confirmed store settlement {}", adminUserId, id);
    }

    @Transactional
    public void payStoreSettlement(String id, Map<String, String> request) {
        StoreSettlement settlement = storeSettlementRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SETTLEMENT_NOT_FOUND));

        if (!"confirmed".equals(settlement.getStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        settlement.setStatus("paid");
        settlement.setPaidAt(LocalDateTime.now());
        if (request != null && request.containsKey("paymentReference")) {
            settlement.setPaymentReference(request.get("paymentReference"));
        }
        storeSettlementRepo.save(settlement);

        log.info("Store settlement {} marked as paid", id);
    }


    public PageResponse<DriverSettlementResponse> getDriverSettlements(int page, int size, String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DriverSettlement> settlementPage;

        if (status != null && !status.isBlank()) {
            settlementPage = driverSettlementRepo.findByStatus(status, pageRequest);
        } else {
            settlementPage = driverSettlementRepo.findAll(pageRequest);
        }

        List<DriverSettlementResponse> content = settlementPage.getContent().stream()
                .map(this::mapDriverSettlement)
                .toList();

        return PageResponse.<DriverSettlementResponse>builder()
                .content(content)
                .page(settlementPage.getNumber())
                .number(settlementPage.getNumber())
                .size(settlementPage.getSize())
                .totalElements(settlementPage.getTotalElements())
                .totalPages(settlementPage.getTotalPages())
                .last(settlementPage.isLast())
                .build();
    }

    @Transactional
    public DriverSettlementResponse generateDriverSettlement(Map<String, Object> request, String adminUserId) {
        String driverId = (String) request.get("driverId");
        String periodStartStr = (String) request.get("periodStart");
        String periodEndStr = (String) request.get("periodEnd");

        if (driverId == null || periodStartStr == null || periodEndStr == null) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        if (!driverRepository.existsById(driverId)) {
            throw new AppException(ErrorCode.DRIVER_NOT_FOUND);
        }

        LocalDate periodStart = LocalDate.parse(periodStartStr);
        LocalDate periodEnd = LocalDate.parse(periodEndStr);

        DriverSettlement settlement = new DriverSettlement();
        settlement.setDriverId(driverId);
        settlement.setPeriodStart(periodStart);
        settlement.setPeriodEnd(periodEnd);
        driverSettlementRepo.save(settlement);

        log.info("Admin {} generated driver settlement for driver {} period {}-{}",
                adminUserId, driverId, periodStart, periodEnd);

        return mapDriverSettlement(settlement);
    }

    @Transactional
    public void confirmDriverSettlement(String id, String adminUserId) {
        DriverSettlement settlement = driverSettlementRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SETTLEMENT_NOT_FOUND));

        if (!"pending".equals(settlement.getStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        settlement.setStatus("confirmed");
        driverSettlementRepo.save(settlement);

        log.info("Admin {} confirmed driver settlement {}", adminUserId, id);
    }


    private StoreSettlementResponse mapStoreSettlement(StoreSettlement s) {
        String merchantName = merchantRepository.findById(s.getMerchantId())
                .map(Merchant::getName)
                .orElse(null);

        return StoreSettlementResponse.builder()
                .id(s.getId())
                .merchantId(s.getMerchantId())
                .merchantName(merchantName)
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .totalOrders(s.getTotalOrders())
                .totalGmv(s.getTotalGmv())
                .totalCommission(s.getTotalCommission())
                .totalVoucherSubsidy(s.getTotalVoucherSubsidy())
                .totalDeductions(s.getTotalDeductions())
                .netAmount(s.getNetAmount())
                .status(s.getStatus())
                .paidAt(s.getPaidAt())
                .paymentReference(s.getPaymentReference())
                .createdBy(s.getCreatedBy())
                .confirmedBy(s.getConfirmedBy())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private StoreSettlementItemResponse mapStoreSettlementItem(StoreSettlementItem item) {
        String storeName = storeRepository.findById(item.getStoreId())
                .map(s -> s.getName())
                .orElse(null);
        String orderNumber = orderRepository.findById(item.getOrderId())
                .map(Order::getOrderNumber)
                .orElse(null);

        return StoreSettlementItemResponse.builder()
                .id(item.getId())
                .settlementId(item.getSettlementId())
                .storeId(item.getStoreId())
                .storeName(storeName)
                .orderId(item.getOrderId())
                .orderNumber(orderNumber)
                .orderSubtotal(item.getOrderSubtotal())
                .commissionAmount(item.getCommissionAmount())
                .voucherSubsidy(item.getVoucherSubsidy())
                .deduction(item.getDeduction())
                .netAmount(item.getNetAmount())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private DriverSettlementResponse mapDriverSettlement(DriverSettlement s) {
        String driverName = driverRepository.findById(s.getDriverId())
                .map(Driver::getFullName)
                .orElse(null);

        return DriverSettlementResponse.builder()
                .id(s.getId())
                .driverId(s.getDriverId())
                .driverName(driverName)
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .totalDeliveries(s.getTotalDeliveries())
                .totalDeliveryEarnings(s.getTotalDeliveryEarnings())
                .totalTips(s.getTotalTips())
                .totalBonuses(s.getTotalBonuses())
                .totalCodCollected(s.getTotalCodCollected())
                .totalCodTransferred(s.getTotalCodTransferred())
                .totalDeductions(s.getTotalDeductions())
                .netAmount(s.getNetAmount())
                .status(s.getStatus())
                .paidAt(s.getPaidAt())
                .paymentReference(s.getPaymentReference())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
