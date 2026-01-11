package com.abhishek.voya.controller;

import com.abhishek.voya.entity.Review;
import com.abhishek.voya.entity.User;
import com.abhishek.voya.service.ReviewService;
import com.abhishek.voya.repository.UserRepository;
import com.abhishek.voya.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{productId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @PostMapping
    public ResponseEntity<Review> addReview(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Review review) {

        Integer userId = sessionService.getUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        String email = user.getEmail();

        review.setUserEmail(email);
        return ResponseEntity.ok(reviewService.addReview(review));
    }
}
