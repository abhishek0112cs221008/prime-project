package com.abhishek.voya.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String category;
    private BigDecimal price;
    private Integer quantity;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "git_repo_url")
    private String gitRepoUrl;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "installation_guide", columnDefinition = "TEXT")
    private String installationGuide;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "discount_code")
    private String discountCode;

    @Column(name = "discount_percent")
    private BigDecimal discountPercentage;
}
