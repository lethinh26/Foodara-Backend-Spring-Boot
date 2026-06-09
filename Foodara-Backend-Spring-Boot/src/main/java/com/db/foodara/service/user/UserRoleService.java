package com.db.foodara.service.user;

import com.db.foodara.dto.request.user.UserRoleRequest;
import com.db.foodara.entity.role.Role;
import com.db.foodara.entity.user.UserRole;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.role.RoleRepository;
import com.db.foodara.repository.user.UserRoleRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserRoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserRole addUserRole(UserRoleRequest request){
        if(request.getUserId() == null || request.getUserId().isEmpty()){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(request.getUserId());
        // id role name
        Role role = roleRepository.getRolesByName(request.getUserRole());
        userRole.setRoleId(role.getId());
        return userRoleRepository.save(userRole);
    }
}
