package org.shivang.financemanager.Service;

import org.shivang.financemanager.Exception.*;
import org.shivang.financemanager.Model.Category;
import org.shivang.financemanager.Model.CategoryType;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.CategoryRequest;
import org.shivang.financemanager.Model.dto.CategoryResponse;
import org.shivang.financemanager.Repository.CategoryRepo;
import org.shivang.financemanager.Repository.TransactionRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final TransactionRepo transactionRepo;

    public CategoryService(CategoryRepo categoryRepo, TransactionRepo transactionRepo) {
        this.categoryRepo = categoryRepo;
        this.transactionRepo = transactionRepo;
    }

    public List<CategoryResponse> getAllCategories(User user) {
        List<Category> categories = categoryRepo.findAllAccessibleByUser(user);
        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse createCategory(CategoryRequest request, User user) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Category name is required");
        }
        if (request.type() == null || request.type().isBlank()) {
            throw new BadRequestException("Category type is required");
        }

        CategoryType type;
        try {
            type = CategoryType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category type. Must be INCOME or EXPENSE");
        }

        // check if default category with same name exists
        if (categoryRepo.findByNameAndUser(request.name(), user).isPresent()) {
            throw new ConflictException("Category with name '" + request.name() + "' already exists");
        }

        Category category = new Category();
        category.setName(request.name());
        category.setType(type);
        category.setCustom(true);
        category.setUser(user);
        categoryRepo.save(category);
        return toResponse(category);
    }

    public void deleteCategory(String name, User user) {
        // check if it's a default category
        Category category = categoryRepo.findByNameAndUser(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.isCustom()) {
            throw new BadRequestException("Cannot delete default categories");
        }
        if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Cannot delete another user's category");
        }
        if (transactionRepo.existsByCategory(category)) {
            throw new BadRequestException("Cannot delete category that is referenced by transactions");
        }
        categoryRepo.delete(category);
    }

    public Category findCategoryByName(String name, User user) {
        return categoryRepo.findByNameAndUser(name, user)
                .orElseThrow(() -> new BadRequestException("Category '" + name + "' not found"));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType().name(), category.isCustom());
    }
}
