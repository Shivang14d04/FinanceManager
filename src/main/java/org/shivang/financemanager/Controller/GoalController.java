package org.shivang.financemanager.Controller;

import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.GoalRequest;
import org.shivang.financemanager.Model.dto.GoalResponse;
import org.shivang.financemanager.Model.dto.GoalUpdateRequest;
import org.shivang.financemanager.Service.AuthService;
import org.shivang.financemanager.Service.GoalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final AuthService authService;

    public GoalController(GoalService goalService, AuthService authService) {
        this.goalService = goalService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@RequestBody GoalRequest request) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.createGoal(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getAllGoals() {
        User user = authService.getCurrentUser();
        List<GoalResponse> goals = goalService.getAllGoals(user);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("goals", goals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.getGoal(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, @RequestBody GoalUpdateRequest request) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.updateGoal(id, request, user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        goalService.deleteGoal(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Goal deleted successfully"));
    }
}
