package com.db.foodara.service.User;

import com.db.foodara.dto.request.user.UserLoginRequest;
import com.db.foodara.dto.request.user.UserRegisterRequest;
import com.db.foodara.entity.user.UserEntity;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.authentication.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserEntity createUser(UserRegisterRequest userRequest) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Kiểm tra phone đã tồn tại
        if (userRepository.existsByPhone(userRequest.getPhone())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserEntity user = new UserEntity();
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setPasswordHash(userRequest.getPasswordHash());
        user.setFullName(userRequest.getFullName());
        user.setAvatarUrl(userRequest.getAvatarUrl());
        user.setStatus(userRequest.getStatus());
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return user;
    }

    public UserEntity userLogin(UserLoginRequest loginRequest) {
        String password = loginRequest.getPassword();
        String userName = loginRequest.getPhone();

        List<UserEntity> listUser = userRepository.findUserEntitiesByPhone(userName);

        UserEntity userLogin = listUser.stream()
                .filter(user -> (user.getEmail().equals(userName) || user.getPhone().equals(userName))
                        && user.getPasswordHash().equals(password))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));

        userLogin.setLastLoginAt(LocalDateTime.now());
        return userLogin;
    }

    public boolean deleteAccount(String id) {
        return userRepository.removeUserById(id);
    }

}
