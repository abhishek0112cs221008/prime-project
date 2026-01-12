package com.ek.primeproject;

public class Product {
    private Integer id;
    private String name;
    private Double price;
    private String description;
    private String category;
    private String imageUrl;

    public Product(Integer id, String name, Double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
