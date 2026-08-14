package com.dawsons.laundry.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of the per-day breakdown shown under a range/monthly report —
 * lets the user see which days within the range were busy/quiet without
 * having to scroll the full bill list.
 */
public class DailyBreakdownItem {
    private LocalDate date;
    private int billCount;
    private BigDecimal totalEarned;

    public DailyBreakdownItem(LocalDate date, int billCount, BigDecimal totalEarned) {
        this.date = date;
        this.billCount = billCount;
        this.totalEarned = totalEarned;
    }

    public LocalDate getDate() { return date; }
    public int getBillCount() { return billCount; }
    public BigDecimal getTotalEarned() { return totalEarned; }
}