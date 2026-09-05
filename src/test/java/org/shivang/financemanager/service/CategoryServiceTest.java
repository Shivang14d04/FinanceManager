package org.shivang.financemanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shivang.financemanager.Exception.BadRequestException;
import org.shivang.financemanager.Exception.ConflictException;
import org.shivang.financemanager.Exception.ResourceNotFoundException;
import org.shivang.financemanager.Model.Category;
import org.shivang.financemanager.Model.CategoryType;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.CategoryRequest;
import org.shivang.financemanager.Model.dto.CategoryResponse;
import org.shivang.financemanager.Repository.CategoryRepo;
import org.shivang.financemanager.Repository.TransactionRepo;
import org.shivang.financemanager.Service.CategoryService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("user@example.com");

        defaultCategory = new Category();
        defaultCategory.setId(1L);
        defaultCategory.setName("Salary");
        defaultCategory.setType(CategoryType.INCOME);
        defaultCategory.setCustom(false);

        customCategory = new Category();
        customCategory.setId(2L);
        customCategory.setName("SideBusiness");
        customCategory.setType(CategoryType.INCOME);
        customCategory.setCustom(true);
        customCategory.setUser(user);
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepo.findAllAccessibleByUser(user)).thenReturn(List.of(defaultCategory, customCategory));

        List<CategoryResponse> responses = categoryService.getAllCategories(user);

        assertEquals(2, responses.size());
        assertEquals("Salary", responses.get(0).name());
        assertEquals("SideBusiness", responses.get(1).name());
    }

    @Test
    void createCategory_Success() {
        CategoryRequest request = new CategoryRequest("Freelance", "INCOME");
        when(categoryRepo.findByNameAndUser("Freelance", user)).thenReturn(Optional.empty());

        CategoryResponse response = categoryService.createCategory(request, user);

        assertNotNull(response);
        assertEquals("Freelance", response.name());
        assertEquals("INCOME", response.type());
        assertTrue(response.isCustom());
        verify(categoryRepo).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsConflictException() {
        CategoryRequest request = new CategoryRequest("SideBusiness", "INCOME");
        when(categoryRepo.findByNameAndUser("SideBusiness", user)).thenReturn(Optional.of(customCategory));

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request, user));
    }

    @Test
    void createCategory_InvalidType_ThrowsBadRequestException() {
        CategoryRequest request = new CategoryRequest("InvalidCat", "INVALID_TYPE");
        assertThrows(BadRequestException.class, () -> categoryService.createCategory(request, user));
    }

    @Test
    void deleteCategory_DefaultCategory_ThrowsBadRequestException() {
        when(categoryRepo.findByNameAndUser("Salary", user)).thenReturn(Optional.of(defaultCategory));

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("Salary", user));
    }

    @Test
    void deleteCategory_ReferencedByTransactions_ThrowsBadRequestException() {
        when(categoryRepo.findByNameAndUser("SideBusiness", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepo.existsByCategory(customCategory)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("SideBusiness", user));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepo.findByNameAndUser("SideBusiness", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepo.existsByCategory(customCategory)).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory("SideBusiness", user));
        verify(categoryRepo).delete(customCategory);
    }
}
