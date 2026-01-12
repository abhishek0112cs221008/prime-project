package com.abhishek.voya.controller;

import com.abhishek.voya.dto.DTOs;
import com.abhishek.voya.entity.Order;
import com.abhishek.voya.entity.User;
import com.abhishek.voya.exception.BadRequestException;
import com.abhishek.voya.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private com.abhishek.voya.service.SessionService sessionService;

    @Autowired
    private com.abhishek.voya.repository.UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        return ResponseEntity.ok(orderService.getMyOrders(user.getEmail()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        if (user.getRole() != User.Role.admin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Admin access required");
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> placeOrder(
            @jakarta.validation.Valid @RequestBody DTOs.OrderRequest request,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        String paymentId = orderService.placeOrder(user.getEmail(), request);
        return ResponseEntity
                .ok(Map.of("message", "Order placed successfully. Verifying payment...", "paymentId", paymentId));
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<Void> verifyOrder(@PathVariable Integer id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        if (user.getRole() != User.Role.admin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Admin access required");
        }
        orderService.verifyOrder(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> rejectOrder(@PathVariable Integer id,
            @jakarta.validation.Valid @RequestBody DTOs.RejectOrderRequest request,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        if (user.getRole() != User.Role.admin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Admin access required");
        }
        orderService.rejectOrder(id, request.getReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/access")
    public ResponseEntity<Map<String, String>> getProjectAccess(@PathVariable Integer id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        Map<String, String> accessDetails = orderService.getProjectAccess(user.getEmail(), id);
        return ResponseEntity.ok(accessDetails);
    }

    private User getUser(String token) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null)
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Please login first");
        return userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
    }
}
