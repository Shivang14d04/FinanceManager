package org.shivang.financemanager.Repository;

import org.shivang.financemanager.Model.Category;
import org.shivang.financemanager.Model.Transaction;
import org.shivang.financemanager.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUser(Long id, User user);

    @Query("""
    SELECT t FROM Transaction t
    WHERE t.user = :user
    AND (:startDate IS NULL OR t.date >= :startDate)
    AND (:endDate IS NULL OR t.date <= :endDate)
    AND (:category IS NULL OR t.category = :category)
    ORDER BY t.date DESC
    """)
    List<Transaction> findFiltered(User user, LocalDate startDate, LocalDate endDate, Category category);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user ORDER BY t.date DESC")
    List<Transaction> findAllByUserOrderByDateDesc(User user);

    boolean existsByCategory(Category category);

    @Query("""
    SELECT t FROM Transaction t
    WHERE t.user = :user
    AND t.date >= :startDate AND t.date <= :endDate
    """)
    List<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :startDate")
    List<Transaction> findByUserAndDateAfter(User user, LocalDate startDate);
}
