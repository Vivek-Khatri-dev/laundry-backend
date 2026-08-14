package com.dawsons.laundry.controller;

import com.dawsons.laundry.dto.DailyReportResponse;
import com.dawsons.laundry.dto.RangeReportResponse;
import com.dawsons.laundry.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    public DailyReportResponse dailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.dailyReport(date != null ? date : LocalDate.now());
    }

    // Powers both the custom date-range picker and the "This Month" quick
    // filter on the Reports page — e.g. /api/reports/range?from=2026-07-01&to=2026-07-31
    @GetMapping("/range")
    public RangeReportResponse rangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.rangeReport(from, to);
    }
}