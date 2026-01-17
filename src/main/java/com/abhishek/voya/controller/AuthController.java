package com.abhishek.voya.controller;

import com.abhishek.voya.dto.DTOs;
import com.abhishek.voya.entity.User;
import com.abhishek.voya.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private com.abhishek.voya.service.SessionService sessionService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody DTOs.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<java.util.Map<String, Object>> login(@Valid @RequestBody DTOs.LoginRequest request) {
        User user = authService.login(request);
        String token = sessionService.createSession(user);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("user", user);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<java.util.Map<String, Object>> googleLogin(
            @RequestBody java.util.Map<String, String> request) {
        String idToken = request.get("idToken");
        User user = authService.loginWithGoogle(idToken);
        String token = sessionService.createSession(user);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("user", user);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<java.util.Map<String, String>> logout(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        sessionService.invalidateSession(token);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestHeader(value = "X-Auth-Token", required = false) String token,
            @Valid @RequestBody DTOs.UpdateProfileRequest request) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<java.util.Map<String, String>> forgotPassword(
            @Valid @RequestBody DTOs.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Password reset email sent");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(
            @Valid @RequestBody DTOs.ResetPasswordRequest request) {
        authService.resetPassword(request);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<java.util.Map<String, String>> changePassword(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @Valid @RequestBody DTOs.ChangePasswordRequest request) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null)
            return ResponseEntity.status(401).build();
        authService.changePassword(userId, request);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}
