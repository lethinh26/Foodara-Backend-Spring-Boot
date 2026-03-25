package com.db.foodara.repository.authentication;

import com.db.foodara.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    List<User> findFirstByEmailOrPhone(String email, String phone);
    boolean removeUserById(String id);
}
