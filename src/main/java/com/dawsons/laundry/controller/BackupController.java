package com.dawsons.laundry.controller;

import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.repository.UserRepository;
import com.dawsons.laundry.security.UserPrincipal;
import com.dawsons.laundry.service.AuditService;
import com.dawsons.laundry.service.BackupService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final BackupService backupService;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public BackupController(BackupService backupService, AuditService auditService, UserRepository userRepository) {
        this.backupService = backupService;
        this.auditService = auditService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<byte[]> backup(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            byte[] excelData = backupService.createExcelBackup();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "Dawsons_Laundry_Backup_" + timestamp + ".xlsx";

            User actor = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            auditService.log(actor, "BACKUP", "DATABASE", null, "Excel backup created");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
                    
        } catch (Exception e) {
            throw new RuntimeException("Backup failed: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        try {
            return ResponseEntity.ok(backupService.testConnection());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}