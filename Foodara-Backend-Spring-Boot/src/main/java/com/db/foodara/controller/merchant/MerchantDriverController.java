package com.db.foodara.controller.merchant;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.merchant.MerchantDriverInfoResponse;
import com.db.foodara.entity.driver.Driver;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.driver.DriverRepository;

import lombok.RequiredArgsConstructor;

/**
 * Lookup driver info (name + phone) for handover screens.
 * Frontend uses this when displaying which driver picks up an order.
 */
@RestController
@RequestMapping("/v1/merchant/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantDriverController {

    private final DriverRepository driverRepository;

    @GetMapping("/{driverId}")
    public ApiResponse<MerchantDriverInfoResponse> getDriver(@PathVariable String driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));
        return ApiResponse.success(MerchantDriverInfoResponse.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .fullName(driver.getFullName())
                .phone(driver.getPhone())
                .build());
    }
}
