package org.shivang.financemanager.Controller;

import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.TransactionRequest;
import org.shivang.financemanager.Model.dto.TransactionResponse;
import org.shivang.financemanager.Model.dto.TransactionUpdateRequest;
import org.shivang.financemanager.Service.AuthService;
import org.shivang.financemanager.Service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        User user = authService.getCurrentUser();
        TransactionResponse response = transactionService.createTransaction(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long categoryId) {
        User user = authService.getCurrentUser();
        List<TransactionResponse> transactions = transactionService.getTransactions(user, startDate, endDate, categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("transactions", transactions));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @RequestBody TransactionUpdateRequest request) {
        User user = authService.getCurrentUser();
        TransactionResponse response = transactionService.updateTransaction(id, request, user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Transaction deleted successfully"));
    }
}
