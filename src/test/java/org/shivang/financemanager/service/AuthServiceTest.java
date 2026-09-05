package org.shivang.financemanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shivang.financemanager.Exception.BadRequestException;
import org.shivang.financemanager.Exception.ConflictException;
import org.shivang.financemanager.Exception.UnauthorizedException;
import org.shivang.financemanager.Model.User;
import org.shivang.financemanager.Model.dto.LoginRequest;
import org.shivang.financemanager.Model.dto.RegisterRequest;
import org.shivang.financemanager.Repository.UserRepo;
import org.shivang.financemanager.Service.AuthService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("user@example.com", "password123", "John Doe", "+1234567890");
        validLoginRequest = new LoginRequest("user@example.com", "password123");
    }

    @Test
    void register_Success() {
        when(userRepo.existsByUsername(validRegisterRequest.username())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.password())).thenReturn("encodedPassword");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(validRegisterRequest.username());
        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(validRegisterRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user@example.com", result.getUsername());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void register_InvalidEmail_ThrowsBadRequestException() {
        RegisterRequest invalidRequest = new RegisterRequest("notanemail", "password123", "John", "123");
        assertThrows(BadRequestException.class, () -> authService.register(invalidRequest));
    }

    @Test
    void register_ShortPassword_ThrowsBadRequestException() {
        RegisterRequest invalidRequest = new RegisterRequest("user@example.com", "123", "John", "123");
        assertThrows(BadRequestException.class, () -> authService.register(invalidRequest));
    }

    @Test
    void register_DuplicateUsername_ThrowsConflictException() {
        when(userRepo.existsByUsername(validRegisterRequest.username())).thenReturn(true);
        assertThrows(ConflictException.class, () -> authService.register(validRegisterRequest));
    }

    @Test
    void login_Success() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertDoesNotThrow(() -> authService.login(validLoginRequest, request));
    }

    @Test
    void login_InvalidCredentials_ThrowsUnauthorizedException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThrows(UnauthorizedException.class, () -> authService.login(validLoginRequest, request));
    }
}
