// SapDiagnosticController.java
package com.dawsons.laundry.controller;

import com.dawsons.laundry.sap.SapB1Client;
import com.dawsons.laundry.sap.SapB1SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sap")
@PreAuthorize("hasRole('ADMIN')")
public class SapDiagnosticController {
    
    private final SapB1Client sapB1Client;
    private final SapB1SyncService sapB1SyncService;
    
    public SapDiagnosticController(SapB1Client sapB1Client, SapB1SyncService sapB1SyncService) {
        this.sapB1Client = sapB1Client;
        this.sapB1SyncService = sapB1SyncService;
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", sapB1Client.isEnabled());
        
        if (sapB1Client.isEnabled()) {
            try {
                boolean connected = sapB1Client.testConnection();
                status.put("connected", connected);
            } catch (Exception e) {
                status.put("connected", false);
                status.put("error", e.getMessage());
            }
        }
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/sync/customer/{customerId}")
    public ResponseEntity<?> syncCustomer(@PathVariable Integer customerId) {
        // You'll need to inject CustomerService here
        return ResponseEntity.ok("Sync initiated");
    }
    
    @PostMapping("/sync/product/{productId}")
    public ResponseEntity<?> syncProduct(@PathVariable Integer productId) {
        // You'll need to inject ProductService here
        return ResponseEntity.ok("Sync initiated");
    }
    
    @PostMapping("/sync/bill/{receiptNo}")
    public ResponseEntity<?> syncBill(@PathVariable Integer receiptNo) {
        // You'll need to inject BillService here
        return ResponseEntity.ok("Sync initiated");
    }
}