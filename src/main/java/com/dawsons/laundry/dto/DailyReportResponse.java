package com.dawsons.laundry.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DailyReportResponse {
    private LocalDate date;
    private int billCount;
    private BigDecimal totalEarned;
    private List<BillResponse> bills;

    public DailyReportResponse(LocalDate date, int billCount, BigDecimal totalEarned, List<BillResponse> bills) {
        this.date = date;
        this.billCount = billCount;
        this.totalEarned = totalEarned;
        this.bills = bills;
    }

    public LocalDate getDate() { return date; }
    public int getBillCount() { return billCount; }
    public BigDecimal getTotalEarned() { return totalEarned; }
    public List<BillResponse> getBills() { return bills; }
}
