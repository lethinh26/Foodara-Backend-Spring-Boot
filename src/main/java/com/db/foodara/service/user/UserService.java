package com.db.foodara.service.user;

import com.db.foodara.dto.response.user.AddressResponse;
import com.db.foodara.dto.response.user.UserProfileResponse;
import com.db.foodara.dto.request.user.AddressRequest;
import com.db.foodara.dto.request.user.UpdateProfileRequest;

import com.db.foodara.entity.user.User;
import com.db.foodara.entity.user.UserAddress;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.location.CityRepository;
import com.db.foodara.repository.location.DistrictRepository;
import com.db.foodara.repository.user.UserAddressRepository;
import com.db.foodara.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    // ============================================================
    // Profile
    // ============================================================
    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) {
            userRepository.findByPhone(request.getPhone())
                    .filter(u -> !u.getId().equals(userId))
                    .ifPresent(u -> { throw new AppException(ErrorCode.PHONE_EXISTS); });
            user.setPhone(request.getPhone());
        }
        user = userRepository.save(user);
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateAvatar(String userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setAvatarUrl(avatarUrl);
        user = userRepository.save(user);
        return mapToProfileResponse(user);
    }

    // ============================================================
    // Addresses
    // ============================================================
    public List<AddressResponse> getAddresses(String userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDesc(userId).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse createAddress(String userId, AddressRequest request) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        return saveAddress(userId, request, address);
    }

    @Transactional
    public AddressResponse updateAddress(String userId, String addressId, AddressRequest request) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUserId().equals(userId)) throw new AppException(ErrorCode.UNAUTHORIZED);
        return saveAddress(userId, request, address);
    }

    @Transactional
    public void deleteAddress(String userId, String addressId) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUserId().equals(userId)) throw new AppException(ErrorCode.UNAUTHORIZED);
        userAddressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setDefaultAddress(String userId, String addressId) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUserId().equals(userId)) throw new AppException(ErrorCode.UNAUTHORIZED);
        unsetOtherDefaults(userId);
        address.setIsDefault(true);
        address = userAddressRepository.save(address);
        return mapToAddressResponse(address);
    }

    // ============================================================
    // Private helpers
    // ============================================================
    private AddressResponse saveAddress(String userId, AddressRequest request, UserAddress address) {
        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        address.setAddressLine(request.getAddressLine());
        address.setWard(request.getWard());
        address.setDistrictId(request.getDistrictId());
        address.setCityId(request.getCityId());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setDeliveryNote(request.getDeliveryNote());
        address.setIsDefault(request.isDefault());

        if (request.isDefault()) {
            unsetOtherDefaults(userId);
        }

        address = userAddressRepository.save(address);
        return mapToAddressResponse(address);
    }

    private void unsetOtherDefaults(String userId) {
        userAddressRepository.findByUserIdOrderByIsDefaultDesc(userId).stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .forEach(a -> { a.setIsDefault(false); userAddressRepository.save(a); });
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerifiedAt() != null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressResponse mapToAddressResponse(UserAddress a) {

        return AddressResponse.builder()
                .id(a.getId())
                .label(a.getLabel())
                .recipientName(a.getRecipientName())
                .recipientPhone(a.getRecipientPhone())
                .addressLine(a.getAddressLine())
                .ward(a.getWard())
                .districtId(a.getDistrictId())
                .cityId(a.getCityId())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .deliveryNote(a.getDeliveryNote())
                .isDefault(Boolean.TRUE.equals(a.getIsDefault()))
                .createdAt(a.getCreatedAt())
                .build();
    }
}
