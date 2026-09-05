package org.shivang.financemanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shivang.financemanager.Exception.BadRequestException;
import org.shivang.financemanager.Exception.ResourceNotFoundException;
import org.shivang.financemanager.Model.Category;
import org.shivang.financemanager.Model.CategoryType;
import org.shivang.financemanager.Model.Transaction;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.TransactionRequest;
import org.shivang.financemanager.Model.dto.TransactionResponse;
import org.shivang.financemanager.Model.dto.TransactionUpdateRequest;
import org.shivang.financemanager.Repository.TransactionRepo;
import org.shivang.financemanager.Service.CategoryService;
import org.shivang.financemanager.Service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category category;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("user@example.com");

        category = new Category();
        category.setId(1L);
        category.setName("Salary");
        category.setType(CategoryType.INCOME);

        transaction = new Transaction();
        transaction.setId(10L);
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setDate(LocalDate.of(2025, 1, 15));
        transaction.setCategory(category);
        transaction.setDescription("Monthly Salary");
        transaction.setUser(user);
    }

    @Test
    void createTransaction_Success() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("5000.00"), "2025-01-15", "Salary", "Monthly Salary");
        when(categoryService.findCategoryByName("Salary", user)).thenReturn(category);
        when(transactionRepo.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(request, user);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.amount());
        assertEquals("Salary", response.category());
        assertEquals("INCOME", response.type());
    }

    @Test
    void createTransaction_NegativeAmount_ThrowsBadRequestException() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("-100.00"), "2025-01-15", "Salary", "Invalid");
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request, user));
    }

    @Test
    void createTransaction_FutureDate_ThrowsBadRequestException() {
        String futureDate = LocalDate.now().plusDays(5).toString();
        TransactionRequest request = new TransactionRequest(new BigDecimal("100.00"), futureDate, "Salary", "Future");
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request, user));
    }

    @Test
    void getTransactions_FilterByDate_Success() {
        when(transactionRepo.findAllByUserOrderByDateDesc(user)).thenReturn(List.of(transaction));

        List<TransactionResponse> responses = transactionService.getTransactions(user, "2025-01-01", "2025-01-31", null);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).id());
    }

    @Test
    void updateTransaction_Success() {
        TransactionUpdateRequest request = new TransactionUpdateRequest(new BigDecimal("6000.00"), null, "Updated Salary");
        when(transactionRepo.findByIdAndUser(10L, user)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.updateTransaction(10L, request, user);

        assertNotNull(response);
        assertEquals(new BigDecimal("6000.00"), response.amount());
        assertEquals("Updated Salary", response.description());
        verify(transactionRepo).save(transaction);
    }

    @Test
    void deleteTransaction_Success() {
        when(transactionRepo.findByIdAndUser(10L, user)).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(10L, user));
        verify(transactionRepo).delete(transaction);
    }

    @Test
    void deleteTransaction_NotFound_ThrowsResourceNotFoundException() {
        when(transactionRepo.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(99L, user));
    }
}
