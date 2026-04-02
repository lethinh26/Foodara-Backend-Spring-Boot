package com.db.foodara.service.user;

import com.db.foodara.dto.reponse.user.AddressResponse;
import com.db.foodara.dto.reponse.user.DeviceResponse;
import com.db.foodara.dto.reponse.user.UserProfileResponse;
import com.db.foodara.dto.request.user.AddressRequest;
import com.db.foodara.dto.request.user.DeviceRequest;
import com.db.foodara.dto.request.user.UpdateProfileRequest;
import com.db.foodara.entity.user.User;
import com.db.foodara.entity.user.UserAddress;
import com.db.foodara.entity.user.UserDevice;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.user.UserAddressRepository;
import com.db.foodara.repository.user.UserRepository;
import com.db.foodara.repository.user.UserDeviceRepository;
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
    private final UserDeviceRepository userDeviceRepository;

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

    public List<AddressResponse> getAddresses(String userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDesc(userId).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse createAddress(String userId, AddressRequest request) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        return getAddressResponse(userId, request, address);
    }

    private AddressResponse getAddressResponse(String userId, AddressRequest request, UserAddress address) {
        address.setLabel(request.getLabel());
        address.setAddressLine(request.getFullAddress());
        address.setWard(request.getWardName());
        address.setDistrictId(request.getDistrictName());
        address.setCityId(request.getCityName());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setDeliveryNote(request.getNote());
        address.setIsDefault(request.isDefault());
        if (request.isDefault()) unsetOtherDefaults(userId);
        address = userAddressRepository.save(address);
        return mapToAddressResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(String userId, String addressId, AddressRequest request) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUserId().equals(userId)) throw new AppException(ErrorCode.UNAUTHORIZED);
        return getAddressResponse(userId, request, address);
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

    public List<DeviceResponse> getDevices(String userId) {
        return userDeviceRepository.findByUserId(userId).stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceResponse registerDevice(String userId, DeviceRequest request) {
        UserDevice device = userDeviceRepository.findByUserIdAndDeviceToken(userId, request.getDeviceToken())
                .orElse(new UserDevice());
        device.setUserId(userId);
        device.setDeviceToken(request.getDeviceToken());
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device = userDeviceRepository.save(device);
        return mapToDeviceResponse(device);
    }

    @Transactional
    public void deleteDevice(String userId, String deviceId) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));
        if (!device.getUserId().equals(userId)) throw new AppException(ErrorCode.UNAUTHORIZED);
        userDeviceRepository.delete(device);
    }

    private void unsetOtherDefaults(String userId) {
        userAddressRepository.findByUserIdOrderByIsDefaultDesc(userId).stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .forEach(a -> { a.setIsDefault(false); userAddressRepository.save(a); });
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId()).email(user.getEmail()).fullName(user.getFullName())
                .phone(user.getPhone()).avatarUrl(user.getAvatarUrl()).status(user.getStatus())
                .emailVerified(user.getEmailVerifiedAt() != null).createdAt(user.getCreatedAt())
                .build();
    }

    private AddressResponse mapToAddressResponse(UserAddress a) {
        return AddressResponse.builder()
                .id(a.getId()).label(a.getLabel())
                .fullAddress(a.getAddressLine())
                .wardName(a.getWard())
                .districtName(a.getDistrictId())
                .cityName(a.getCityId())
                .latitude(a.getLatitude()).longitude(a.getLongitude())
                .note(a.getDeliveryNote())
                .isDefault(Boolean.TRUE.equals(a.getIsDefault()))
                .build();
    }

    private DeviceResponse mapToDeviceResponse(UserDevice device) {
        return DeviceResponse.builder()
                .id(device.getId()).deviceToken(device.getDeviceToken())
                .deviceName(device.getDeviceName()).deviceType(device.getDeviceType())
                .createdAt(device.getCreatedAt())
                .build();
    }
}
