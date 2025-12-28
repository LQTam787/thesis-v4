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
