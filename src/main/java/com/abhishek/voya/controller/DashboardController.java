package com.abhishek.voya.controller;

import com.abhishek.voya.entity.User;
import com.abhishek.voya.exception.BadRequestException;
import com.abhishek.voya.repository.OrderRepository;
import com.abhishek.voya.repository.ProductRepository;
import com.abhishek.voya.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private com.abhishek.voya.service.SessionService sessionService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Auth-Token", required = false) String token) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Please login first");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getRole() != User.Role.admin) {
            throw new BadRequestException("Unauthorized");
        }

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalProducts", productRepository.count());
        stats.put("totalCustomers", userRepository.count());
        stats.put("totalOrders", orderRepository.count());
        return ResponseEntity.ok(stats);
    }
}
