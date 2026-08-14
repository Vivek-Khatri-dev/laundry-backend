package com.dawsons.laundry.controller;

import com.dawsons.laundry.dto.BillResponse;
import com.dawsons.laundry.entity.Bill;
import com.dawsons.laundry.entity.BillStatus;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.repository.UserRepository;
import com.dawsons.laundry.security.UserPrincipal;
import com.dawsons.laundry.service.BillService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bills/status")
public class BillStatusController {

    private final BillService billService;
    private final UserRepository userRepository;

    public BillStatusController(BillService billService, UserRepository userRepository) {
        this.billService = billService;
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    public Map<String, List<BillResponse>> getAllStatuses() {
        Map<String, List<BillResponse>> result = new HashMap<>();
        
        for (BillStatus status : BillStatus.values()) {
            if (status != BillStatus.VOIDED && status != BillStatus.RETURNED) {
                List<BillResponse> bills = billService.getBillsByStatus(status).stream()
                        .map(BillResponse::new)
                        .collect(Collectors.toList());
                result.put(status.name(), bills);
            }
        }
        
        return result;
    }

    @GetMapping("/{status}")
    public List<BillResponse> getBillsByStatus(@PathVariable BillStatus status) {
        return billService.getBillsByStatus(status).stream()
                .map(BillResponse::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/{receiptNo}/{status}")
    public BillResponse updateStatus(@PathVariable Integer receiptNo,
                                      @PathVariable BillStatus status,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return new BillResponse(billService.updateBillStatus(receiptNo, status, actor));
    }
}