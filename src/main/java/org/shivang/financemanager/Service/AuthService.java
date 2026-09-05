package org.shivang.financemanager.Service;

import org.shivang.financemanager.Exception.ConflictException;
import org.shivang.financemanager.Exception.BadRequestException;
import org.shivang.financemanager.Exception.UnauthorizedException;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.LoginRequest;
import org.shivang.financemanager.Model.dto.RegisterRequest;
import org.shivang.financemanager.Repository.UserRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User register(RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (!request.username().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Username must be a valid email address");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        if (request.password().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new BadRequestException("Full name is required");
        }
        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            throw new BadRequestException("Phone number is required");
        }
        if (userRepo.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        return userRepo.save(user);
    }

    public void login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("Not authenticated");
        }
        String username = auth.getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
