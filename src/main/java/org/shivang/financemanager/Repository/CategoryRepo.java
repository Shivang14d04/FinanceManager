package org.shivang.financemanager.Repository;

import org.shivang.financemanager.Model.Category;
import org.shivang.financemanager.Model.CategoryType;
import org.shivang.financemanager.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.isCustom = false OR c.user = :user")
    List<Category> findAllAccessibleByUser(User user);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.isCustom = false OR c.user = :user)")
    Optional<Category> findByNameAndUser(String name, User user);

    boolean existsByNameAndUserAndIsCustomTrue(String name, User user);

    Optional<Category> findByNameAndUserAndIsCustomTrue(String name, User user);
}
