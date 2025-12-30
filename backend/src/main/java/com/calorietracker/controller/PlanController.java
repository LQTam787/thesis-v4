package com.calorietracker.controller;

import com.calorietracker.dto.response.PlanResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI-powered meal plan generation.
 * 
 * <p>Provides endpoints for generating and retrieving personalized 7-day meal plans
 * using Google Gemini AI based on user profile and eating history.</p>
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li><b>GET /api/plan:</b> Get current meal plan (if exists)</li>
 *   <li><b>POST /api/plan/generate:</b> Generate new AI meal plan</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see PlanService
 */
@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final UserRepository userRepository;

    /**
     * Get the current meal plan for the user
     */
    @GetMapping
    public ResponseEntity<PlanResponse> getPlan(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        PlanResponse response = planService.getPlan(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate a new meal plan using AI
     */
    @PostMapping("/generate")
    public ResponseEntity<PlanResponse> generatePlan(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        PlanResponse response = planService.generatePlan(user);
        return ResponseEntity.ok(response);
    }
}
