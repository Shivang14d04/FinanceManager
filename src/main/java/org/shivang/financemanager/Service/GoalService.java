package org.shivang.financemanager.Service;

import org.shivang.financemanager.Exception.BadRequestException;
import org.shivang.financemanager.Exception.ForbiddenException;
import org.shivang.financemanager.Exception.ResourceNotFoundException;
import org.shivang.financemanager.Model.CategoryType;
import org.shivang.financemanager.Model.SavingsGoal;
import org.shivang.financemanager.Model.Transaction;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.GoalRequest;
import org.shivang.financemanager.Model.dto.GoalResponse;
import org.shivang.financemanager.Model.dto.GoalUpdateRequest;
import org.shivang.financemanager.Repository.SavingsGoalRepo;
import org.shivang.financemanager.Repository.TransactionRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class GoalService {

    private final SavingsGoalRepo goalRepo;
    private final TransactionRepo transactionRepo;

    public GoalService(SavingsGoalRepo goalRepo, TransactionRepo transactionRepo) {
        this.goalRepo = goalRepo;
        this.transactionRepo = transactionRepo;
    }

    public GoalResponse createGoal(GoalRequest request, User user) {
        if (request.goalName() == null || request.goalName().isBlank()) {
            throw new BadRequestException("Goal name is required");
        }
        if (request.targetAmount() == null || request.targetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target amount must be a positive value");
        }
        if (request.targetDate() == null || request.targetDate().isBlank()) {
            throw new BadRequestException("Target date is required");
        }

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(request.targetDate());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid target date format");
        }
        if (!targetDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        LocalDate startDate = LocalDate.now();
        if (request.startDate() != null && !request.startDate().isBlank()) {
            try {
                startDate = LocalDate.parse(request.startDate());
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid start date format");
            }
        }

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(request.goalName());
        goal.setTargetAmount(request.targetAmount());
        goal.setTargetDate(targetDate);
        goal.setStartDate(startDate);
        goal.setUser(user);
        goalRepo.save(goal);

        return toResponse(goal, user);
    }

    public List<GoalResponse> getAllGoals(User user) {
        return goalRepo.findAllByUser(user).stream()
                .map(goal -> toResponse(goal, user))
                .toList();
    }

    public GoalResponse getGoal(Long id, User user) {
        SavingsGoal goal = goalRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }
        return toResponse(goal, user);
    }

    public GoalResponse updateGoal(Long id, GoalUpdateRequest request, User user) {
        SavingsGoal goal = goalRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        if (request.targetAmount() != null) {
            if (request.targetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target amount must be a positive value");
            }
            goal.setTargetAmount(request.targetAmount());
        }
        if (request.targetDate() != null && !request.targetDate().isBlank()) {
            LocalDate targetDate;
            try {
                targetDate = LocalDate.parse(request.targetDate());
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid target date format");
            }
            if (!targetDate.isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            goal.setTargetDate(targetDate);
        }

        goalRepo.save(goal);
        return toResponse(goal, user);
    }

    public void deleteGoal(Long id, User user) {
        SavingsGoal goal = goalRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }
        goalRepo.delete(goal);
    }

    private GoalResponse toResponse(SavingsGoal goal, User user) {
        BigDecimal progress = calculateProgress(goal, user);
        BigDecimal percentage = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? progress.multiply(BigDecimal.valueOf(100)).divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal remaining = goal.getTargetAmount().subtract(progress).max(BigDecimal.ZERO);

        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getTargetDate().toString(),
                goal.getStartDate().toString(),
                progress,
                percentage,
                remaining
        );
    }

    private BigDecimal calculateProgress(SavingsGoal goal, User user) {
        List<Transaction> transactions = transactionRepo.findByUserAndDateAfter(user, goal.getStartDate());

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIncome.subtract(totalExpense);
    }
}
