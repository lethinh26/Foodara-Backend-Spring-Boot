package com.db.foodara.repository.user;

import com.db.foodara.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    List<User> findFirstByEmailOrPhone(String email, String phone);
    boolean removeUserById(String id);
    boolean existsById(String id);

    // Admin: tìm kiếm theo email, fullName, phone
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR u.phone LIKE CONCAT('%', :q, '%')")
    Page<User> searchUsers(@Param("q") String query, Pageable pageable);

    // Admin: filter theo status
    Page<User> findByStatus(String status, Pageable pageable);

    // Admin: find users by role name (via user_roles + roles join)
    @Query("SELECT u FROM User u JOIN UserRole ur ON u.id = ur.userId JOIN Role r ON ur.roleId = r.id WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
}

