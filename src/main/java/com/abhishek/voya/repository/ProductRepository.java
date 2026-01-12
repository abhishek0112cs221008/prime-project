package com.abhishek.voya.repository;

import com.abhishek.voya.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory(String category);

    List<Product> findByNameContainingIgnoreCase(String name); // For recommendations matching interests
}
