package com.db.foodara.driver.service;

import com.db.foodara.driver.dto.request.UpdateDriverApprovalRequest;
import com.db.foodara.driver.dto.request.VerifyDriverDocumentRequest;
import com.db.foodara.driver.dto.response.*;
import com.db.foodara.driver.entity.*;
import com.db.foodara.driver.exception.AppException;
import com.db.foodara.driver.exception.ErrorCode;
import com.db.foodara.driver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDriverService {

    private final DriverRepository driverRepository;
    private final DriverDocumentRepository documentRepository;
    private final DriverBankAccountRepository bankAccountRepository;
    private final DriverWalletTransactionRepository walletTransactionRepository;
    private final DriverShiftRepository shiftRepository;

    private static final Set<String> VALID_APPROVAL_STATUSES = Set.of("pending", "approved", "rejected", "suspended");
    private static final Set<String> VALID_VERIFICATION_STATUSES = Set.of("pending", "verified", "rejected");

    public PageResponse<AdminDriverResponse> getDrivers(int page, int size, String search, String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Driver> driverPage;

        if (search != null && !search.isBlank()) {
            driverPage = driverRepository.searchDrivers(search.trim(), pageRequest);
        } else if (status != null && !status.isBlank()) {
            driverPage = driverRepository.findByApprovalStatus(status, pageRequest);
        } else {
            driverPage = driverRepository.findAll(pageRequest);
        }

        List<AdminDriverResponse> content = driverPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AdminDriverResponse>builder()
                .content(content)
                .page(driverPage.getNumber())
                .number(driverPage.getNumber())
                .size(driverPage.getSize())
                .totalElements(driverPage.getTotalElements())
                .totalPages(driverPage.getTotalPages())
                .last(driverPage.isLast())
                .build();
    }

    public AdminDriverResponse getDriverDetail(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));
        return mapToResponse(driver);
    }

    @Transactional
    public void updateDriverApproval(String id, UpdateDriverApprovalRequest request) {
        if (!VALID_APPROVAL_STATUSES.contains(request.getApprovalStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));

        driver.setApprovalStatus(request.getApprovalStatus());
        if ("approved".equals(request.getApprovalStatus())) {
            driver.setApprovedAt(LocalDateTime.now());
            driver.setRejectionReason(null);
        } else if ("rejected".equals(request.getApprovalStatus())) {
            driver.setRejectionReason(request.getReason());
        }

        driverRepository.save(driver);
    }

    public List<DriverDocumentResponse> getDriverDocuments(String driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new AppException(ErrorCode.DRIVER_NOT_FOUND);
        }
        return documentRepository.findByDriverId(driverId).stream()
                .map(this::mapDocumentToResponse)
                .toList();
    }

    @Transactional
    public void verifyDocument(String id, VerifyDriverDocumentRequest request) {
        if (!VALID_VERIFICATION_STATUSES.contains(request.getVerificationStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        DriverDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_DOCUMENT_NOT_FOUND));

        document.setVerificationStatus(request.getVerificationStatus());
        document.setVerifiedAt(LocalDateTime.now());
        if ("rejected".equals(request.getVerificationStatus())) {
            document.setRejectionReason(request.getRejectionReason());
        } else {
            document.setRejectionReason(null);
        }

        documentRepository.save(document);
    }

    public List<DriverBankAccountResponse> getDriverBankAccounts(String driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new AppException(ErrorCode.DRIVER_NOT_FOUND);
        }
        return bankAccountRepository.findByDriverId(driverId).stream()
                .map(this::mapBankAccountToResponse)
                .toList();
    }

    public PageResponse<DriverWalletTransactionResponse> getDriverWalletTransactions(String driverId, int page, int size) {
        if (!driverRepository.existsById(driverId)) {
            throw new AppException(ErrorCode.DRIVER_NOT_FOUND);
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DriverWalletTransaction> txPage = walletTransactionRepository.findByDriverId(driverId, pageRequest);

        List<DriverWalletTransactionResponse> content = txPage.getContent().stream()
                .map(this::mapWalletTransactionToResponse)
                .toList();

        return PageResponse.<DriverWalletTransactionResponse>builder()
                .content(content)
                .page(txPage.getNumber())
                .number(txPage.getNumber())
                .size(txPage.getSize())
                .totalElements(txPage.getTotalElements())
                .totalPages(txPage.getTotalPages())
                .last(txPage.isLast())
                .build();
    }

    public PageResponse<DriverShiftResponse> getDriverShifts(String driverId, int page, int size) {
        if (!driverRepository.existsById(driverId)) {
            throw new AppException(ErrorCode.DRIVER_NOT_FOUND);
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DriverShift> shiftPage = shiftRepository.findByDriverId(driverId, pageRequest);

        List<DriverShiftResponse> content = shiftPage.getContent().stream()
                .map(this::mapShiftToResponse)
                .toList();

        return PageResponse.<DriverShiftResponse>builder()
                .content(content)
                .page(shiftPage.getNumber())
                .number(shiftPage.getNumber())
                .size(shiftPage.getSize())
                .totalElements(shiftPage.getTotalElements())
                .totalPages(shiftPage.getTotalPages())
                .last(shiftPage.isLast())
                .build();
    }

    // --- Mappers ---

    private AdminDriverResponse mapToResponse(Driver d) {
        return AdminDriverResponse.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .fullName(d.getFullName())
                .phone(d.getPhone())
                .dateOfBirth(d.getDateOfBirth())
                .idNumber(d.getIdNumber())
                .vehicleType(d.getVehicleType())
                .vehiclePlate(d.getVehiclePlate())
                .vehicleBrand(d.getVehicleBrand())
                .vehicleColor(d.getVehicleColor())
                .approvalStatus(d.getApprovalStatus())
                .approvedAt(d.getApprovedAt())
                .rejectionReason(d.getRejectionReason())
                .isOnline(d.getIsOnline())
                .isBusy(d.getIsBusy())
                .avgRating(d.getAvgRating())
                .totalRatings(d.getTotalRatings())
                .totalDeliveries(d.getTotalDeliveries())
                .acceptanceRate(d.getAcceptanceRate())
                .completionRate(d.getCompletionRate())
                .walletBalance(d.getWalletBalance())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private DriverDocumentResponse mapDocumentToResponse(DriverDocument doc) {
        return DriverDocumentResponse.builder()
                .id(doc.getId())
                .driverId(doc.getDriverId())
                .documentType(doc.getDocumentType())
                .documentUrl(doc.getDocumentUrl())
                .documentNumber(doc.getDocumentNumber())
                .expiryDate(doc.getExpiryDate())
                .verificationStatus(doc.getVerificationStatus())
                .verifiedAt(doc.getVerifiedAt())
                .rejectionReason(doc.getRejectionReason())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private DriverBankAccountResponse mapBankAccountToResponse(DriverBankAccount acc) {
        return DriverBankAccountResponse.builder()
                .id(acc.getId())
                .driverId(acc.getDriverId())
                .bankName(acc.getBankName())
                .accountNumber(acc.getAccountNumber())
                .accountHolder(acc.getAccountHolder())
                .branch(acc.getBranch())
                .isDefault(acc.getIsDefault())
                .isVerified(acc.getIsVerified())
                .createdAt(acc.getCreatedAt())
                .build();
    }

    private DriverWalletTransactionResponse mapWalletTransactionToResponse(DriverWalletTransaction tx) {
        return DriverWalletTransactionResponse.builder()
                .id(tx.getId())
                .driverId(tx.getDriverId())
                .transactionType(tx.getTransactionType())
                .amount(tx.getAmount())
                .balanceAfter(tx.getBalanceAfter())
                .referenceType(tx.getReferenceType())
                .referenceId(tx.getReferenceId())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private DriverShiftResponse mapShiftToResponse(DriverShift shift) {
        return DriverShiftResponse.builder()
                .id(shift.getId())
                .driverId(shift.getDriverId())
                .wentOnlineAt(shift.getWentOnlineAt())
                .wentOfflineAt(shift.getWentOfflineAt())
                .durationMinutes(shift.getDurationMinutes())
                .totalOrders(shift.getTotalOrders())
                .totalEarnings(shift.getTotalEarnings())
                .createdAt(shift.getCreatedAt())
                .build();
    }
}
