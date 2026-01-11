package com.abhishek.voya.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String userEmail;

    @Column(name = "shipping_address")
    private String shippingAddress;

    private String utr;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    // Discount amount applied
    private BigDecimal discount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "payment_id")
    private String paymentId;

    private String status = "Pending Verification";
}
