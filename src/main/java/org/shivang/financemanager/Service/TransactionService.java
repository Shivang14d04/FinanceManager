package org.shivang.financemanager.Service;

import org.shivang.financemanager.Exception.BadRequestException;
import org.shivang.financemanager.Exception.ResourceNotFoundException;
import org.shivang.financemanager.Model.Category;
import org.shivang.financemanager.Model.Transaction;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.TransactionRequest;
import org.shivang.financemanager.Model.dto.TransactionResponse;
import org.shivang.financemanager.Model.dto.TransactionUpdateRequest;
import org.shivang.financemanager.Repository.TransactionRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepo transactionRepo;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepo transactionRepo, CategoryService categoryService) {
        this.transactionRepo = transactionRepo;
        this.categoryService = categoryService;
    }

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be a positive value");
        }
        if (request.date() == null || request.date().isBlank()) {
            throw new BadRequestException("Date is required");
        }
        if (request.category() == null || request.category().isBlank()) {
            throw new BadRequestException("Category is required");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(request.date());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Use YYYY-MM-DD");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Date cannot be in the future");
        }

        Category category = categoryService.findCategoryByName(request.category(), user);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.amount());
        transaction.setDate(date);
        transaction.setCategory(category);
        transaction.setDescription(request.description());
        transaction.setUser(user);
        transactionRepo.save(transaction);
        return toResponse(transaction);
    }

    public List<TransactionResponse> getTransactions(User user, String startDate, String endDate, Long categoryId) {
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);

        List<Transaction> transactions = transactionRepo.findAllByUserOrderByDateDesc(user);

        if (start != null) {
            transactions = transactions.stream().filter(t -> !t.getDate().isBefore(start)).toList();
        }
        if (end != null) {
            transactions = transactions.stream().filter(t -> !t.getDate().isAfter(end)).toList();
        }
        if (categoryId != null) {
            transactions = transactions.stream().filter(t -> t.getCategory().getId().equals(categoryId)).toList();
        }

        return transactions.stream().map(this::toResponse).toList();
    }

    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request, User user) {
        Transaction transaction = transactionRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (request.amount() != null) {
            if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Amount must be a positive value");
            }
            transaction.setAmount(request.amount());
        }
        if (request.category() != null) {
            Category category = categoryService.findCategoryByName(request.category(), user);
            transaction.setCategory(category);
        }
        if (request.description() != null) {
            transaction.setDescription(request.description());
        }

        transactionRepo.save(transaction);
        return toResponse(transaction);
    }

    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionRepo.delete(transaction);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Use YYYY-MM-DD");
        }
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDate().toString(),
                transaction.getCategory().getName(),
                transaction.getDescription(),
                transaction.getCategory().getType().name()
        );
    }
}
