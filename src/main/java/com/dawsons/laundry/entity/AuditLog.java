package com.dawsons.laundry.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String action;         // CREATE_BILL, MARK_PAID, EDIT_BILL, VOID_BILL, ADD_PRODUCT, ...

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;     // BILL, PRODUCT, USER

    @Column(name = "entity_id")
    private Integer entityId;

    @Lob
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    void onCreate() { this.timestamp = LocalDateTime.now(); }

    public AuditLog() {}

    public AuditLog(User user, String action, String entityType, Integer entityId, String details) {
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Integer getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
