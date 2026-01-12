package com.abhishek.voya.service;

import com.abhishek.voya.entity.CartItem;
import com.abhishek.voya.entity.Order;
import com.abhishek.voya.entity.Product;
import com.abhishek.voya.exception.BadRequestException;
import com.abhishek.voya.repository.CartItemRepository;
import com.abhishek.voya.repository.OrderRepository;
import com.abhishek.voya.repository.CartItemRepository;
import com.abhishek.voya.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    public List<Order> getMyOrders(String userEmail) {
        return orderRepository.findByUserEmail(userEmail);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll(org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "orderDate"));
    }

    @Transactional
    public String placeOrder(String userEmail, com.abhishek.voya.dto.DTOs.OrderRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserEmail(userEmail);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Check if UTR is already used
        if (orderRepository.existsByUtr(request.getUtr())) {
            throw new BadRequestException(
                    "This Payment Reference (UTR) has already been used. Please enter a valid unique UTR.");
        }

        String paymentId = UUID.randomUUID().toString();
        java.math.BigDecimal discount = java.math.BigDecimal.ZERO;

        // Simple Discount Logic based on code
        if ("SAVE10".equalsIgnoreCase(request.getDiscountCode())) {
            discount = new java.math.BigDecimal("0.10"); // 10%
        }

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            // Stock check removed for digital products (unlimited availability)
            // if (product.getQuantity() < item.getQuantity()) { ... }
            // product.setQuantity(product.getQuantity() - item.getQuantity());
            // productRepository.save(product);

            Order order = new Order();
            order.setUserEmail(userEmail);
            order.setProduct(product);
            order.setQuantity(item.getQuantity());
            order.setPaymentId(paymentId);
            order.setStatus("Pending Verification");
            order.setShippingAddress(request.getShippingAddress());
            order.setUtr(request.getUtr());
            order.setOrderDate(java.time.LocalDateTime.now());

            // Calculate discount amount per item if needed, or just flag it.
            // For simplicity here, we assume the discount is just tracked.
            // In a real app, we'd adjust the price.
            // Let's store the discount rate or amount.
            order.setDiscount(discount);

            orderRepository.save(order);
        }

        cartItemRepository.deleteAll(cartItems);

        return paymentId;
    }

    public void verifyOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Order not found"));
        order.setVerified(true);
        order.setStatus("Confirmed");
        orderRepository.save(order);
    }

    public void rejectOrder(Integer orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Order not found"));
        order.setVerified(false);
        order.setStatus("Rejected");
        order.setRejectionReason(reason);
        orderRepository.save(order);
    }

    public java.util.Map<String, String> getProjectAccess(String userEmail, Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Order not found"));
        if (!order.getUserEmail().equals(userEmail)) {
            throw new BadRequestException("This order does not belong to you");
        }
        if (!order.isVerified()) {
            throw new BadRequestException("Order is not yet verified");
        }
        return java.util.Map.of(
                "gitRepoUrl", order.getProduct().getGitRepoUrl() != null ? order.getProduct().getGitRepoUrl() : "",
                "installationGuide",
                order.getProduct().getInstallationGuide() != null ? order.getProduct().getInstallationGuide()
                        : "No guide available.");
    }
}
