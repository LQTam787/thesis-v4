package com.calorietracker.controller;

import com.calorietracker.dto.response.ReviewResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for AI-powered progress review generation.
 * 
 * <p>Provides endpoints for generating and managing progress reviews that analyze
 * user's meal plan adherence, calorie patterns, and weight progress.</p>
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li><b>GET /api/review:</b> Get current review (if exists)</li>
 *   <li><b>POST /api/review/generate:</b> Generate new AI progress review</li>
 *   <li><b>DELETE /api/review:</b> Delete current review</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see ReviewService
 */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ReviewResponse> getReview(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ReviewResponse response = reviewService.getReview(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<ReviewResponse> generateReview(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ReviewResponse response = reviewService.generateReview(user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteReview(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        reviewService.deleteReview(user.getId());
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }
}
