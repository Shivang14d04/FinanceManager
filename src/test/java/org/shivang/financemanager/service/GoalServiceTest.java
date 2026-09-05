package org.shivang.financemanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shivang.financemanager.Exception.BadRequestException;
import org.shivang.financemanager.Exception.ResourceNotFoundException;
import org.shivang.financemanager.Model.SavingsGoal;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.GoalRequest;
import org.shivang.financemanager.Model.dto.GoalResponse;
import org.shivang.financemanager.Model.dto.GoalUpdateRequest;
import org.shivang.financemanager.Repository.SavingsGoalRepo;
import org.shivang.financemanager.Repository.TransactionRepo;
import org.shivang.financemanager.Service.GoalService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private SavingsGoalRepo goalRepo;

    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private GoalService goalService;

    private User user;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("user@example.com");

        goal = new SavingsGoal();
        goal.setId(1L);
        goal.setGoalName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("10000.00"));
        goal.setTargetDate(LocalDate.now().plusYears(1));
        goal.setStartDate(LocalDate.now().minusMonths(1));
        goal.setUser(user);
    }

    @Test
    void createGoal_Success() {
        GoalRequest request = new GoalRequest("Emergency Fund", new BigDecimal("10000.00"), LocalDate.now().plusYears(1).toString(), LocalDate.now().minusMonths(1).toString());
        when(goalRepo.save(any(SavingsGoal.class))).thenReturn(goal);
        when(transactionRepo.findByUserAndDateAfter(eq(user), any(LocalDate.class))).thenReturn(Collections.emptyList());

        GoalResponse response = goalService.createGoal(request, user);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.goalName());
        assertEquals(new BigDecimal("10000.00"), response.targetAmount());
    }

    @Test
    void createGoal_PastTargetDate_ThrowsBadRequestException() {
        GoalRequest request = new GoalRequest("Past Goal", new BigDecimal("1000.00"), "2020-01-01", "2019-01-01");
        assertThrows(BadRequestException.class, () -> goalService.createGoal(request, user));
    }

    @Test
    void getGoal_Success() {
        when(goalRepo.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepo.findByUserAndDateAfter(eq(user), any(LocalDate.class))).thenReturn(Collections.emptyList());

        GoalResponse response = goalService.getGoal(1L, user);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void updateGoal_Success() {
        GoalUpdateRequest request = new GoalUpdateRequest(new BigDecimal("15000.00"), null);
        when(goalRepo.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepo.findByUserAndDateAfter(eq(user), any(LocalDate.class))).thenReturn(Collections.emptyList());

        GoalResponse response = goalService.updateGoal(1L, request, user);

        assertNotNull(response);
        assertEquals(new BigDecimal("15000.00"), response.targetAmount());
        verify(goalRepo).save(goal);
    }

    @Test
    void deleteGoal_Success() {
        when(goalRepo.findById(1L)).thenReturn(Optional.of(goal));

        assertDoesNotThrow(() -> goalService.deleteGoal(1L, user));
        verify(goalRepo).delete(goal);
    }
}
