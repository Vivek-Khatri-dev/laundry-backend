package com.dawsons.laundry.service;

import com.dawsons.laundry.dto.BillResponse;
import com.dawsons.laundry.dto.DailyBreakdownItem;
import com.dawsons.laundry.dto.DailyReportResponse;
import com.dawsons.laundry.dto.RangeReportResponse;
import com.dawsons.laundry.entity.Bill;
import com.dawsons.laundry.entity.BillStatus;
import com.dawsons.laundry.exception.BadRequestException;
import com.dawsons.laundry.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final BillRepository billRepository;

    public ReportService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public DailyReportResponse dailyReport(LocalDate date) {
        List<Bill> bills = billRepository.findByCreateDateOrderByReceiptNoDesc(date);
        
        // Count total bills created today (excluding VOIDED and RETURNED)
        int billCount = (int) bills.stream()
                .filter(b -> b.getStatus() != BillStatus.VOIDED && b.getStatus() != BillStatus.RETURNED)
                .count();
        
        // Calculate total collected from PAID bills using discounted price (finalAmount)
        BigDecimal totalEarned = bills.stream()
                .filter(b -> b.getStatus() == BillStatus.PAID)
                .map(b -> b.getFinalAmount() != null ? b.getFinalAmount() : b.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<BillResponse> billResponses = bills.stream()
                .map(BillResponse::new)
                .collect(Collectors.toList());
        
        return new DailyReportResponse(date, billCount, totalEarned, billResponses);
    }

    /**
     * Report over an inclusive date range — powers both the "custom range"
     * picker and the one-click "This Month" report on the Reports page.
     */
    public RangeReportResponse rangeReport(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both 'from' and 'to' dates are required.");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must not be after 'to' date.");
        }

        List<Bill> bills = billRepository.findByCreateDateBetweenOrderByReceiptNoDesc(from, to);

        int billCount = (int) bills.stream()
                .filter(b -> b.getStatus() != BillStatus.VOIDED && b.getStatus() != BillStatus.RETURNED)
                .count();
        int paidCount = (int) bills.stream().filter(b -> b.getStatus() == BillStatus.PAID).count();
        int pendingCount = (int) bills.stream().filter(b -> b.getStatus() == BillStatus.PENDING).count();
        int voidedCount = (int) bills.stream().filter(b -> b.getStatus() == BillStatus.VOIDED).count();

        BigDecimal totalEarned = bills.stream()
                .filter(b -> b.getStatus() == BillStatus.PAID)
                .map(b -> b.getFinalAmount() != null ? b.getFinalAmount() : b.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Per-day breakdown, sorted chronologically, only for days that had
        // at least one bill (keeps the response small for wide date ranges).
        Map<LocalDate, List<Bill>> byDate = bills.stream()
                .collect(Collectors.groupingBy(Bill::getCreateDate, TreeMap::new, Collectors.toList()));
        List<DailyBreakdownItem> dailyBreakdown = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Bill>> entry : byDate.entrySet()) {
            List<Bill> dayBills = entry.getValue();
            int dayCount = (int) dayBills.stream()
                    .filter(b -> b.getStatus() != BillStatus.VOIDED && b.getStatus() != BillStatus.RETURNED)
                    .count();
            BigDecimal dayEarned = dayBills.stream()
                    .filter(b -> b.getStatus() == BillStatus.PAID)
                    .map(b -> b.getFinalAmount() != null ? b.getFinalAmount() : b.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyBreakdown.add(new DailyBreakdownItem(entry.getKey(), dayCount, dayEarned));
        }
        dailyBreakdown.sort(Comparator.comparing(DailyBreakdownItem::getDate).reversed());

        List<BillResponse> billResponses = bills.stream()
                .map(BillResponse::new)
                .collect(Collectors.toList());

        return new RangeReportResponse(from, to, billCount, paidCount, pendingCount, voidedCount,
                totalEarned, dailyBreakdown, billResponses);
    }
}