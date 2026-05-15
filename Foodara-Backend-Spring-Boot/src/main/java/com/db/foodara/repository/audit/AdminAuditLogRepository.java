package com.db.foodara.repository.audit;

import com.db.foodara.entity.audit.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, String> {
    Page<AdminAuditLog> findByModule(String module, Pageable pageable);
    Page<AdminAuditLog> findByAdminId(String adminId, Pageable pageable);
    Page<AdminAuditLog> findByAction(String action, Pageable pageable);
}
