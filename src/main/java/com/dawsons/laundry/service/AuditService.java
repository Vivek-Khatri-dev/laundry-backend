package com.dawsons.laundry.service;

import com.dawsons.laundry.entity.AuditLog;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(User actor, String action, String entityType, Integer entityId, String details) {
        auditLogRepository.save(new AuditLog(actor, action, entityType, entityId, details));
    }
}
