package org.shivang.financemanager.Controller;

import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.MonthlyReportResponse;
import org.shivang.financemanager.Model.dto.YearlyReportResponse;
import org.shivang.financemanager.Service.AuthService;
import org.shivang.financemanager.Service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    public ReportController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(@PathVariable int year, @PathVariable int month) {
        User user = authService.getCurrentUser();
        MonthlyReportResponse response = reportService.getMonthlyReport(year, month, user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        User user = authService.getCurrentUser();
        YearlyReportResponse response = reportService.getYearlyReport(year, user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
