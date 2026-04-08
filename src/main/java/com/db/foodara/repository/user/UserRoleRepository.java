package com.db.foodara.repository.user;

import com.db.foodara.entity.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    List<UserRole> findByUserId(String userId);
    void deleteByUserIdAndRoleId(String userId, String roleId);
    boolean existsByUserIdAndRoleId(String userId, String roleId);
}
