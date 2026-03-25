package com.db.foodara.controller.user_authentication;

import com.db.foodara.dto.request.user.LoginRequest;
import com.db.foodara.dto.request.user.UserRequest;
import com.db.foodara.entity.user.User;
import com.db.foodara.service.authentication.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping
    User createUser(@RequestBody @Valid UserRequest request){
        // @RequestBody -> nhận dữ lieệu body json từ request
        // @Valid -> Validate các định dạng trong request (UserRequest)
        return authenticationService.createUser(request);
    }

    @PatchMapping
    User loginPatchUser(@RequestBody @Valid LoginRequest loginRequest){
        return authenticationService.userLogin(loginRequest);
    }

    @DeleteMapping
    boolean deleteUserById(@RequestBody String idUser){
        return authenticationService.deleteAccount(idUser);
    }

}
