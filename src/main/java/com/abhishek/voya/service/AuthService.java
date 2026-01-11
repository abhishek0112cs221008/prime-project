package com.abhishek.voya.service;

import com.abhishek.voya.dto.DTOs;
import com.abhishek.voya.entity.User;
import com.abhishek.voya.exception.BadRequestException;
import com.abhishek.voya.exception.ResourceNotFoundException;
import com.abhishek.voya.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User register(DTOs.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(User.Role.customer);
        return userRepository.save(user);
    }

    public User login(DTOs.LoginRequest request) {
        if ("admin@mail.com".equals(request.getEmail()) && "admin#voya".equals(request.getPassword())) {
            return userRepository.findByEmail("admin@mail.com").orElseGet(() -> {
                // Create admin if not exists, or handle as special session
                // Assuming admin is in DB or we just return a stub if really needed
                // Let's rely on DB having it (user said DB is VOYA.sql)
                throw new ResourceNotFoundException("Admin user not found in DB");
            });
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }
        return user;
    }
}
