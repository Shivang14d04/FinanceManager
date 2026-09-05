package org.shivang.financemanager.Config;

import org.shivang.financemanager.Model.Category;
import org.shivang.financemanager.Model.CategoryType;
import org.shivang.financemanager.Repository.CategoryRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class DataInitConfig {

    @Bean
    public CommandLineRunner initCategories(CategoryRepo categoryRepo) {
        return args -> {
            if (categoryRepo.count() > 0) return;

            Map<String, CategoryType> defaults = Map.of(
                    "Salary", CategoryType.INCOME,
                    "Food", CategoryType.EXPENSE,
                    "Rent", CategoryType.EXPENSE,
                    "Transportation", CategoryType.EXPENSE,
                    "Entertainment", CategoryType.EXPENSE,
                    "Healthcare", CategoryType.EXPENSE,
                    "Utilities", CategoryType.EXPENSE
            );

            for (Map.Entry<String, CategoryType> entry : defaults.entrySet()) {
                Category category = new Category();
                category.setName(entry.getKey());
                category.setType(entry.getValue());
                category.setCustom(false);
                category.setUser(null);
                categoryRepo.save(category);
            }
        };
    }
}
