package com.db.foodara.controller.user;

import com.db.foodara.dto.request.user.UserLoginRequest;
import com.db.foodara.dto.request.user.UserRegisterRequest;
import com.db.foodara.entity.user.UserEntity;
import com.db.foodara.service.User.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService authenticationService;

    @PostMapping
    UserEntity createUser(@RequestBody @Valid UserRegisterRequest request){
        // @RequestBody -> nhận dữ lieệu body json từ request
        // @Valid -> Validate các định dạng trong request (UserRequest)
        return authenticationService.createUser(request);
    }

    @PatchMapping
    UserEntity loginPatchUser(@RequestBody @Valid UserLoginRequest loginRequest){
        return authenticationService.userLogin(loginRequest);
    }

    @DeleteMapping
    boolean deleteUserById(@RequestBody String idUser){
        return authenticationService.deleteAccount(idUser);
    }

}
