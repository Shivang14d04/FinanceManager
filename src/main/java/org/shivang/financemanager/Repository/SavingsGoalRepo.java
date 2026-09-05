package org.shivang.financemanager.Repository;

import org.shivang.financemanager.Model.SavingsGoal;
import org.shivang.financemanager.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepo extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findAllByUser(User user);
    Optional<SavingsGoal> findByIdAndUser(Long id, User user);
}
