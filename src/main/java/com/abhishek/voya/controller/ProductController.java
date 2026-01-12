package com.abhishek.voya.controller;

import com.abhishek.voya.dto.DTOs;
import com.abhishek.voya.entity.Product;
import com.abhishek.voya.entity.User;
import com.abhishek.voya.exception.BadRequestException;
import com.abhishek.voya.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private com.abhishek.voya.service.SessionService sessionService;

    @Autowired
    private com.abhishek.voya.repository.UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Product>> getRecommendations(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getInterests() == null || user.getInterests().isEmpty()) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }

        List<String> interests = java.util.Arrays.asList(user.getInterests().split(","));
        return ResponseEntity.ok(productService.getRecommendations(interests));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody DTOs.ProductRequest request,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        checkAdmin(token);
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id,
            @Valid @RequestBody DTOs.ProductRequest request,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        checkAdmin(token);
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        checkAdmin(token);
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/admin")
    public ResponseEntity<java.util.Map<String, Object>> getProductForAdmin(@PathVariable Integer id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        checkAdmin(token);
        Product p = productService.getProductById(id);

        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("category", p.getCategory());
        map.put("price", p.getPrice());
        map.put("quantity", p.getQuantity());
        map.put("imageUrl", p.getImageUrl());
        map.put("description", p.getDescription());
        map.put("gitRepoUrl", p.getGitRepoUrl());
        map.put("installationGuide", p.getInstallationGuide());

        return ResponseEntity.ok(map);
    }

    private void checkAdmin(String token) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Please login first");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
        if (user.getRole() != User.Role.admin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
