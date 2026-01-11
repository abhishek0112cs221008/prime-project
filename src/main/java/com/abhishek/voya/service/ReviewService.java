package com.abhishek.voya.service;

import com.abhishek.voya.entity.Review;
import com.abhishek.voya.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsByProduct(Integer productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Review addReview(Review review) {
        review.setCreatedAt(java.time.LocalDateTime.now());
        return reviewRepository.save(review);
    }
}
