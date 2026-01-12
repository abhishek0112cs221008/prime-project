package com.abhishek.voya.service;

import com.abhishek.voya.entity.CartItem;
import com.abhishek.voya.entity.Order;
import com.abhishek.voya.entity.Product;
import com.abhishek.voya.exception.BadRequestException;
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

    @Autowired
    private EmailService emailService;

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
        // Discount is now calculated per item inside the loop

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
            // Calculate discount
            java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
            if (request.getDiscountCode() != null
                    && request.getDiscountCode().equalsIgnoreCase(product.getDiscountCode())
                    && product.getDiscountPercentage() != null) {
                discount = product.getDiscountPercentage();
            } else if ("SAVE10".equalsIgnoreCase(request.getDiscountCode())) {
                discount = new java.math.BigDecimal("0.10");
            }
            order.setDiscount(discount);

            orderRepository.save(order);

            // Send Order Received Email (Pending)
            String subject = "Order Received: " + product.getName();
            String body = buildOrderReceivedEmail(order);
            emailService.sendEmail(userEmail, subject, body);
        }

        cartItemRepository.deleteAll(cartItems);

        return paymentId;
    }

    public void verifyOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Order not found"));
        order.setVerified(true);
        order.setStatus("Confirmed");
        orderRepository.save(order);

        // Send Email
        String subject = "Order Verified! Access your Project: " + order.getProduct().getName();
        String body = buildOrderEmail(order);
        emailService.sendEmail(order.getUserEmail(), subject, body);
    }

    public void rejectOrder(Integer orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Order not found"));
        order.setVerified(false);
        order.setStatus("Rejected");
        order.setRejectionReason(reason);
        orderRepository.save(order);

        // Send Email
        String subject = "Action Required: Issue with your Order for " + order.getProduct().getName();
        String body = "Dear User,\n\n" +
                "We encountered an issue verifying your payment for '" + order.getProduct().getName() + "'.\n\n" +
                "Reason: " + reason + "\n\n" +
                "Please contact support or try placing the order again with correct details.\n\n" +
                "Regards,\nPrime Project Team";
        emailService.sendEmail(order.getUserEmail(), subject, body);
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

    public java.util.Map<String, Object> validateDiscount(String userEmail, String code) {
        List<CartItem> cartItems = cartItemRepository.findByUserEmail(userEmail);
        boolean isValid = false;
        String message = "Invalid discount code";
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            java.math.BigDecimal price = product.getPrice();
            java.math.BigDecimal qty = new java.math.BigDecimal(item.getQuantity());
            java.math.BigDecimal lineTotal = price.multiply(qty);
            total = total.add(lineTotal);

            if (code != null && code.equalsIgnoreCase(product.getDiscountCode())
                    && product.getDiscountPercentage() != null) {
                isValid = true;
                java.math.BigDecimal itemDiscount = lineTotal.multiply(product.getDiscountPercentage())
                        .divide(new java.math.BigDecimal(100));
                discountAmount = discountAmount.add(itemDiscount);
                message = "Code applied: " + product.getDiscountPercentage() + "% off " + product.getName();
            }
        }

        if (!isValid && "SAVE10".equalsIgnoreCase(code)) {
            isValid = true;
            message = "Global discount applied (10%)";
            discountAmount = total.multiply(new java.math.BigDecimal("0.10"));
        }

        java.math.BigDecimal finalTotal = total.subtract(discountAmount);
        if (finalTotal.compareTo(java.math.BigDecimal.ZERO) < 0)
            finalTotal = java.math.BigDecimal.ZERO;

        return java.util.Map.of(
                "valid", isValid,
                "message", message,
                "discountAmount", discountAmount,
                "finalTotal", finalTotal);
    }

    private String buildOrderEmail(Order order) {
        Product p = order.getProduct();
        String productName = p != null ? p.getName() : "Project";
        String price = p != null && p.getPrice() != null ? "₹" + p.getPrice() : "N/A";

        return "<html><body style='font-family: sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                +
                "<h2 style='color: #4caf50; text-align: center;'>Order Verified!</h2>" +
                "<p>Dear User,</p>" +
                "<p>Thank you for your purchase. Your payment has been verified and you can now access your project.</p>"
                +
                "<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>" +
                "<tr style='background: #f5f5f5;'><th style='padding: 10px; text-align: left;'>Product</th><th style='padding: 10px; text-align: right;'>Price</th></tr>"
                +
                "<tr><td style='padding: 10px; border-bottom: 1px solid #eee;'>" + productName
                + "</td><td style='padding: 10px; text-align: right; border-bottom: 1px solid #eee;'>" + price
                + "</td></tr>" +
                "</table>" +
                "<p style='text-align: center;'><a href='http://localhost:8080/orders.html' style='background: #000; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Access Product</a></p>"
                +
                "<p style='font-size: 12px; color: #777; margin-top: 30px; text-align: center;'>Order ID: "
                + order.getId() + " | UTR: " + order.getUtr() + "</p>" +
                "</div></body></html>";
    }

    private String buildOrderReceivedEmail(Order order) {
        Product p = order.getProduct();
        String productName = p != null ? p.getName() : "Project";
        String price = p != null && p.getPrice() != null ? "₹" + p.getPrice() : "N/A";

        return "<html><body style='font-family: sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                +
                "<h2 style='color: #ffa000; text-align: center;'>Order Received</h2>" +
                "<p>Dear User,</p>" +
                "<p>We have received your order. We are currently verifying your payment Reference (UTR). This usually takes 1-2 hours.</p>"
                +
                "<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>" +
                "<tr style='background: #f5f5f5;'><th style='padding: 10px; text-align: left;'>Product</th><th style='padding: 10px; text-align: right;'>Price</th></tr>"
                +
                "<tr><td style='padding: 10px; border-bottom: 1px solid #eee;'>" + productName
                + "</td><td style='padding: 10px; text-align: right; border-bottom: 1px solid #eee;'>" + price
                + "</td></tr>" +
                "</table>" +
                "<p>Once verified, you will receive another email with access details.</p>" +
                "<p style='font-size: 12px; color: #777; margin-top: 30px; text-align: center;'>Order ID: "
                + order.getId() + " | UTR: " + order.getUtr() + "</p>" +
                "</div></body></html>";
    }
}
