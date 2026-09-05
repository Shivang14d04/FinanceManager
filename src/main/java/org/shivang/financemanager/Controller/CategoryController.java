package org.shivang.financemanager.Controller;

import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.CategoryRequest;
import org.shivang.financemanager.Model.dto.CategoryResponse;
import org.shivang.financemanager.Service.AuthService;
import org.shivang.financemanager.Service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService;

    public CategoryController(CategoryService categoryService, AuthService authService) {
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getAllCategories() {
        User user = authService.getCurrentUser();
        List<CategoryResponse> categories = categoryService.getAllCategories(user);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("categories", categories));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        User user = authService.getCurrentUser();
        CategoryResponse response = categoryService.createCategory(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable String name) {
        User user = authService.getCurrentUser();
        categoryService.deleteCategory(name, user);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Category deleted successfully"));
    }
}
