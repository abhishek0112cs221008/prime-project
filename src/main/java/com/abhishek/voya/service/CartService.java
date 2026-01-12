package com.abhishek.voya.service;

import com.abhishek.voya.dto.DTOs;
import com.abhishek.voya.entity.CartItem;
import com.abhishek.voya.entity.Product;
import com.abhishek.voya.exception.ResourceNotFoundException;
import com.abhishek.voya.repository.CartItemRepository;
import com.abhishek.voya.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private com.abhishek.voya.repository.OrderRepository orderRepository;

    public List<CartItem> getCart(String userEmail) {
        return cartItemRepository.findByUserEmail(userEmail);
    }

    @Transactional
    public CartItem addToCart(String userEmail, DTOs.CartRequest request) {
        // Check if user has an active order (anything EXCEPT "Rejected")
        if (orderRepository.existsByUserEmailAndProductIdAndStatusNot(userEmail, request.getProductId(), "Rejected")) {
            throw new com.abhishek.voya.exception.BadRequestException("You have already purchased this project.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Optional<CartItem> existing = cartItemRepository.findByUserEmailAndProductId(userEmail, request.getProductId());

        if (existing.isPresent()) {
            throw new com.abhishek.voya.exception.BadRequestException("Item is already in your cart.");
        } else {
            CartItem item = new CartItem();
            item.setUserEmail(userEmail);
            item.setProduct(product);
            item.setQuantity(1); // Force quantity to 1 for digital products
            return cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeFromCart(Integer cartItemId, String userEmail) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getUserEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Item not found in your cart");
        }
        cartItemRepository.delete(item);
    }
}
