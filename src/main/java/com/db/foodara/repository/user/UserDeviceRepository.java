package com.db.foodara.repository.user;

import com.db.foodara.entity.user.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {
    List<UserDevice> findByUserId(String userId);
    Optional<UserDevice> findByUserIdAndDeviceToken(String userId, String deviceToken);
    void deleteByUserId(String userId);
}
