package com.db.foodara.service.authentication;

import com.db.foodara.dto.request.user.LoginRequest;
import com.db.foodara.dto.request.user.UserRequest;
import com.db.foodara.entity.user.User;
import com.db.foodara.repository.authentication.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(UserRequest userRequest){
        /*
        * create user when register an account
        * create user in admin page (maybe)
        * gọi post <=> save()
        */
        String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$";
        String PHONE_REGEX = "0[0-9]{9}";

        User user = new User();

        // kiem tra không được trùng
        // kiem tra định dạng
        if(userRepository.existsByEmail(userRequest.getEmail())){
            throw new RuntimeException("Email is unique so can't repeat");
        }else if(userRequest.getEmail().matches(EMAIL_REGEX)){
            throw new RuntimeException("Email isn't valid");
        }else if(userRepository.existsByPhone(userRequest.getPhone())){
            throw new RuntimeException("Phone is unique so can't repeat");
        } else if (userRequest.getPhone().matches(PHONE_REGEX)) {
            throw new RuntimeException("Phone isn't valid");
        }

        user.setEmail(userRequest.getEmail());
        user.setPhone(user.getPhone());
        user.setPasswordHash(userRequest.getPasswordHash());
        user.setFullName(user.getFullName());
        user.setAvatarUrl(userRequest.getAvatarUrl());
        user.setStatus(userRequest.getStatus());
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user); // -> user

        // trước mắt thì gọi return user sau này nhờ cái jwt return token
        return user;
    }

    public User userLogin(LoginRequest loginRequest){
        String password = loginRequest.getPassword();
        String userName = loginRequest.getUserName();

        List<User> listUser = userRepository.findFirstByEmailOrPhone(userName, userName);

        // validate
        if(userName.isEmpty()){
            throw new RuntimeException("Username isn't valid!!!");
        }else if(password.length() < 8){
            throw new RuntimeException("Password have to has more than 8 character!!!");
        } else if (listUser.isEmpty()) {
            throw new RuntimeException("Username isn't existing");
        }

        // từ từ nha password hash đợi tìm hiểu
        User userLogin = listUser.stream()
                                .filter(user -> (user.getEmail().equals(userName) || user.getPhone().equals(userName))
                                                        && user.getPasswordHash().equals(password))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("User name isn't existing"));
        // lần cuối cùng đăng nhập lại
        userLogin.setLastLoginAt(LocalDateTime.now());
        return  userLogin;
    }

    public boolean deleteAccount(String id){
        return userRepository.removeUserById(id);
    }

}
