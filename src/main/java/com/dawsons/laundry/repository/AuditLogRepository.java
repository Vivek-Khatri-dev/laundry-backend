package com.dawsons.laundry.repository;

import com.dawsons.laundry.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Integer entityId);
}
