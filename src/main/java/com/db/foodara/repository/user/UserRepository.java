package com.db.foodara.repository.user;

import com.db.foodara.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    List<User> findFirstByEmailOrPhone(String email, String phone);
    boolean removeUserById(String id);
}
