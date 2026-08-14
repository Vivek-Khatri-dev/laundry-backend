package com.dawsons.laundry.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RangeReportResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private int billCount;
    private int paidCount;
    private int pendingCount;
    private int voidedCount;
    private BigDecimal totalEarned;
    private List<DailyBreakdownItem> dailyBreakdown;
    private List<BillResponse> bills;

    public RangeReportResponse(LocalDate fromDate, LocalDate toDate, int billCount, int paidCount,
                                int pendingCount, int voidedCount, BigDecimal totalEarned,
                                List<DailyBreakdownItem> dailyBreakdown, List<BillResponse> bills) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.billCount = billCount;
        this.paidCount = paidCount;
        this.pendingCount = pendingCount;
        this.voidedCount = voidedCount;
        this.totalEarned = totalEarned;
        this.dailyBreakdown = dailyBreakdown;
        this.bills = bills;
    }

    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public int getBillCount() { return billCount; }
    public int getPaidCount() { return paidCount; }
    public int getPendingCount() { return pendingCount; }
    public int getVoidedCount() { return voidedCount; }
    public BigDecimal getTotalEarned() { return totalEarned; }
    public List<DailyBreakdownItem> getDailyBreakdown() { return dailyBreakdown; }
    public List<BillResponse> getBills() { return bills; }
}