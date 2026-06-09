package com.db.foodara.repository.role;

import com.db.foodara.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
    Optional<Role> findByNameIgnoreCase(String name);
    boolean existsByName(String name);

    Role getRolesByName(String name);
}
