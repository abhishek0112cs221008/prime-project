package com.abhishek.voya.service;

import com.abhishek.voya.dto.DTOs;
import com.abhishek.voya.entity.Product;
import com.abhishek.voya.exception.ResourceNotFoundException;
import com.abhishek.voya.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        p.setViewCount(p.getViewCount() + 1);
        return productRepository.save(p);
    }

    public Product createProduct(DTOs.ProductRequest request) {
        Product p = new Product();
        mapRequestToProduct(request, p);
        p.setViewCount(0);
        return productRepository.save(p);
    }

    public Product updateProduct(Integer id, DTOs.ProductRequest request) {
        Product p = getProductById(id);
        mapRequestToProduct(request, p);
        return productRepository.save(p);
    }

    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public List<Product> getRecommendations(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.Set<Product> recommendations = new java.util.HashSet<>();
        for (String interest : interests) {
            String term = interest.trim();
            if (!term.isEmpty()) {
                recommendations.addAll(productRepository.findByNameContainingIgnoreCase(term));
                recommendations.addAll(productRepository.findByCategory(term));
            }
        }
        return new java.util.ArrayList<>(recommendations);
    }

    private void mapRequestToProduct(DTOs.ProductRequest request, Product p) {
        p.setName(request.getName());
        p.setCategory(request.getCategory());
        p.setPrice(request.getPrice());
        p.setQuantity(request.getQuantity());
        p.setImageUrl(request.getImageUrl());
        p.setDescription(request.getDescription());
        p.setGitRepoUrl(request.getGitRepoUrl());
        p.setInstallationGuide(request.getInstallationGuide());
        p.setDiscountCode(request.getDiscountCode());
        p.setDiscountPercentage(request.getDiscountPercentage());
    }
}
