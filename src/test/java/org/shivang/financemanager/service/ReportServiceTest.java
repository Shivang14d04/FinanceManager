package org.shivang.financemanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shivang.financemanager.Model.Category;
import org.shivang.financemanager.Model.CategoryType;
import org.shivang.financemanager.Model.Transaction;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.MonthlyReportResponse;
import org.shivang.financemanager.Model.dto.YearlyReportResponse;
import org.shivang.financemanager.Repository.TransactionRepo;
import org.shivang.financemanager.Service.ReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private Transaction incomeTx;
    private Transaction expenseTx;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("user@example.com");

        Category incomeCat = new Category();
        incomeCat.setName("Salary");
        incomeCat.setType(CategoryType.INCOME);

        Category expenseCat = new Category();
        expenseCat.setName("Food");
        expenseCat.setType(CategoryType.EXPENSE);

        incomeTx = new Transaction();
        incomeTx.setAmount(new BigDecimal("3000.00"));
        incomeTx.setCategory(incomeCat);

        expenseTx = new Transaction();
        expenseTx.setAmount(new BigDecimal("500.00"));
        expenseTx.setCategory(expenseCat);
    }

    @Test
    void getMonthlyReport_Success() {
        when(transactionRepo.findByUserAndDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(incomeTx, expenseTx));

        MonthlyReportResponse report = reportService.getMonthlyReport(2025, 1, user);

        assertNotNull(report);
        assertEquals(1, report.month());
        assertEquals(2025, report.year());
        assertEquals(new BigDecimal("3000.00"), report.totalIncome().get("Salary"));
        assertEquals(new BigDecimal("500.00"), report.totalExpenses().get("Food"));
        assertEquals(new BigDecimal("2500.00"), report.netSavings());
    }

    @Test
    void getYearlyReport_Success() {
        when(transactionRepo.findByUserAndDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(incomeTx, expenseTx));

        YearlyReportResponse report = reportService.getYearlyReport(2025, user);

        assertNotNull(report);
        assertEquals(2025, report.year());
        assertEquals(new BigDecimal("2500.00"), report.netSavings());
    }
}
