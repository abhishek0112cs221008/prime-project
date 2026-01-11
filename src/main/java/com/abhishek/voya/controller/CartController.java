package com.abhishek.voya.controller;

import com.abhishek.voya.dto.DTOs;
import com.abhishek.voya.entity.CartItem;
import com.abhishek.voya.entity.User;
import com.abhishek.voya.exception.BadRequestException;
import com.abhishek.voya.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private com.abhishek.voya.service.SessionService sessionService;

    @Autowired
    private com.abhishek.voya.repository.UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        List<CartItem> items = cartService.getCart(user.getEmail());
        System.out.println("DEBUG: User " + user.getEmail() + " fetching cart. Found " + items.size() + " items.");
        if (!items.isEmpty()) {
            items.forEach(i -> System.out.println("DEBUG: CartItem " + i.getId() + " Product: "
                    + (i.getProduct() != null ? i.getProduct().getName() : "NULL")));
        }
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<CartItem> addToCart(@Valid @RequestBody DTOs.CartRequest request,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        return ResponseEntity.ok(cartService.addToCart(user.getEmail(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Integer id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User user = getUser(token);
        cartService.removeFromCart(id, user.getEmail());
        return ResponseEntity.ok().build();
    }

    private User getUser(String token) {
        Integer userId = sessionService.getUserId(token);
        if (userId == null)
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Please login first");
        return userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
    }
}
