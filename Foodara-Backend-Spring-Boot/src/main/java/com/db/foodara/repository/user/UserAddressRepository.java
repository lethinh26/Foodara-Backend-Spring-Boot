package com.db.foodara.repository.user;

import com.db.foodara.entity.user.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, String> {
    List<UserAddress> findByUserId(String userId);
    List<UserAddress> findByUserIdOrderByIsDefaultDesc(String userId);
    Optional<UserAddress> findByUserIdAndIsDefaultTrue(String userId);
    Optional<UserAddress> findByIdAndUserId(String id, String userId);
}
