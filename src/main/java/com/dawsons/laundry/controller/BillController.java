package com.dawsons.laundry.controller;

import com.dawsons.laundry.dto.BillRequest;
import com.dawsons.laundry.dto.BillResponse;
import com.dawsons.laundry.dto.VoidBillRequest;
import com.dawsons.laundry.entity.Bill;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.repository.UserRepository;
import com.dawsons.laundry.security.UserPrincipal;
import com.dawsons.laundry.service.BillService;
import com.dawsons.laundry.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;
    private final ReceiptService receiptService;
    private final UserRepository userRepository;

    public BillController(BillService billService, ReceiptService receiptService, UserRepository userRepository) {
        this.billService = billService;
        this.receiptService = receiptService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        Bill bill = billService.createBill(request, currentUser(principal));
        return ResponseEntity.ok(new BillResponse(bill));
    }

    @GetMapping
    public List<BillResponse> getAllBills() {
        return billService.getAllActive().stream().map(BillResponse::new).collect(Collectors.toList());
    }

    @GetMapping("/{receiptNo}")
    public BillResponse getBill(@PathVariable Integer receiptNo) {
        return new BillResponse(billService.getByReceiptNoOrThrow(receiptNo));
    }

    @PutMapping("/{receiptNo}")
    public BillResponse editBill(@PathVariable Integer receiptNo,
                                  @Valid @RequestBody BillRequest request,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return new BillResponse(billService.editBill(receiptNo, request, currentUser(principal)));
    }

    @PostMapping("/{receiptNo}/mark-paid")
    public BillResponse markPaid(@PathVariable Integer receiptNo,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return new BillResponse(billService.markPaid(receiptNo, currentUser(principal)));
    }

    @PostMapping("/{receiptNo}/void")
    @PreAuthorize("hasRole('ADMIN')")
    public BillResponse voidBill(@PathVariable Integer receiptNo,
                                  @Valid @RequestBody VoidBillRequest request,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return new BillResponse(billService.voidBill(receiptNo, request.getReason(), false, currentUser(principal)));
    }

    @PostMapping("/{receiptNo}/return")
    public BillResponse returnBill(@PathVariable Integer receiptNo,
                                    @Valid @RequestBody VoidBillRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return new BillResponse(billService.voidBill(receiptNo, request.getReason(), true, currentUser(principal)));
    }

    @GetMapping("/{receiptNo}/receipt.html")
    public ResponseEntity<String> getReceiptHtml(@PathVariable Integer receiptNo) {
        Bill bill = billService.getByReceiptNoOrThrow(receiptNo);
        String html = receiptService.buildReceiptHtml(bill);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @RequestMapping(value = "/{receiptNo}/receipt.pdf", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> getReceiptPdf(@PathVariable Integer receiptNo) {
        try {
            Bill bill = billService.getByReceiptNoOrThrow(receiptNo);
            byte[] pdfBytes = receiptService.buildReceiptPdf(bill);

            String filename = "Receipt_" + String.format("%03d", receiptNo) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }
    @GetMapping("/search")
    public List<BillResponse> searchBills(@RequestParam String q) {
        return billService.searchBills(q).stream()
                .map(BillResponse::new)
                .collect(Collectors.toList());
    }

    private User currentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user vanished from DB"));
    }
}