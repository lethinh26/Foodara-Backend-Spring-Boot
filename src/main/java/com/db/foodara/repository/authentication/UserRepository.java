package com.db.foodara.repository.authentication;

import com.db.foodara.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<UserEntity> findUserEntitiesByPhone(String phone);
    boolean removeUserById(String id);
}
