package org.shivang.financemanager.Service;

import org.shivang.financemanager.Model.CategoryType;
import org.shivang.financemanager.Model.Transaction;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.MonthlyReportResponse;
import org.shivang.financemanager.Model.dto.YearlyReportResponse;
import org.shivang.financemanager.Repository.TransactionRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepo transactionRepo;

    public ReportService(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    public MonthlyReportResponse getMonthlyReport(int year, int month, User user) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Transaction> transactions = transactionRepo.findByUserAndDateBetween(user, startDate, endDate);

        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            if (t.getCategory().getType() == CategoryType.INCOME) {
                incomeByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            } else {
                expenseByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            }
        }

        BigDecimal totalIncome = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenseByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyReportResponse(month, year, incomeByCategory, expenseByCategory, totalIncome.subtract(totalExpense));
    }

    public YearlyReportResponse getYearlyReport(int year, User user) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepo.findByUserAndDateBetween(user, startDate, endDate);

        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            if (t.getCategory().getType() == CategoryType.INCOME) {
                incomeByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            } else {
                expenseByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            }
        }

        BigDecimal totalIncome = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenseByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return new YearlyReportResponse(year, incomeByCategory, expenseByCategory, totalIncome.subtract(totalExpense));
    }
}
