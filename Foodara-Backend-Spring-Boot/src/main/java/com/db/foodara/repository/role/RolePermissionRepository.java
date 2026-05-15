package com.db.foodara.repository.role;

import com.db.foodara.entity.role.RolePermission;
import com.db.foodara.entity.role.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.permission WHERE rp.roleId = :roleId")
    List<RolePermission> findByRoleIdWithPermissions(@Param("roleId") String roleId);

    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.permission")
    List<RolePermission> findAllWithPermissions();

    void deleteByRoleId(String roleId);
}
