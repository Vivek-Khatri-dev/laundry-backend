package com.dawsons.laundry.controller;

import com.dawsons.laundry.entity.AuditLog;
import com.dawsons.laundry.repository.AuditLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Locked to ADMIN role globally in SecurityConfig ("/api/audit/**").
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public record AuditEntryResponse(
            Integer id, String actor, String action, String entityType,
            Integer entityId, String details, LocalDateTime timestamp) {}

    @GetMapping
    public List<AuditEntryResponse> getAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditEntryResponse toResponse(AuditLog log) {
        return new AuditEntryResponse(
                log.getId(),
                log.getUser() != null ? log.getUser().getFullName() : "system",
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getTimestamp());
    }
}
