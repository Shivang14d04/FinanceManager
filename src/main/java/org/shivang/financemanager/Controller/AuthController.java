package org.shivang.financemanager.Controller;

import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.LoginRequest;
import org.shivang.financemanager.Model.dto.RegisterRequest;
import org.shivang.financemanager.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully", "userId", user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        authService.login(request, httpRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Login successful"));
    }
}
