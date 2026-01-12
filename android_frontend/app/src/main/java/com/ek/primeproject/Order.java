package com.ek.primeproject;

import java.util.List;

public class Order {
    private Integer id;
    private String orderDate;
    private Double totalAmount;
    private String status;
    private List<CartItem> items; // Reusing CartItem structure for Order items if similar

    public Integer getId() {
        return id;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
