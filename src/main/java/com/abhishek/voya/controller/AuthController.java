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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        sessionService.invalidateSession(token);
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null)
            return ResponseEntity.status(401).build();
        // For simplicity, we just return the user object if valid, in real app fetch
        // from DB
        return ResponseEntity.ok(new User());
    }
}
