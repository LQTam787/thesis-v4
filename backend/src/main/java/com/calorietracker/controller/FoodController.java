package com.calorietracker.controller;

import com.calorietracker.dto.request.FoodRequest;
import com.calorietracker.dto.response.FoodResponse;
import com.calorietracker.model.MealType;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for food management operations.
 * 
 * <p>Handles CRUD operations for food items, including both system-wide foods
 * and user-created custom foods. Users can only modify their own custom foods.</p>
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li><b>GET /api/foods:</b> Get all available foods (system + custom)</li>
 *   <li><b>GET /api/foods/meal-type/{type}:</b> Get foods filtered by meal type</li>
 *   <li><b>GET /api/foods/custom:</b> Get user's custom foods only</li>
 *   <li><b>GET /api/foods/{id}:</b> Get single food by ID</li>
 *   <li><b>POST /api/foods:</b> Create new custom food</li>
 *   <li><b>PUT /api/foods/{id}:</b> Update custom food</li>
 *   <li><b>DELETE /api/foods/{id}:</b> Delete custom food</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see FoodService
 */
@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;
    private final UserRepository userRepository;

    /**
     * Get all available foods (system + user's custom foods)
     */
    @GetMapping
    public ResponseEntity<List<FoodResponse>> getAllFoods(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<FoodResponse> foods = foodService.getAvailableFoods(userId);
        return ResponseEntity.ok(foods);
    }

    /**
     * Get foods filtered by meal type
     */
    @GetMapping("/meal-type/{mealType}")
    public ResponseEntity<List<FoodResponse>> getFoodsByMealType(
            @PathVariable MealType mealType,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<FoodResponse> foods = foodService.getFoodsByMealType(userId, mealType);
        return ResponseEntity.ok(foods);
    }

    /**
     * Get only the user's custom foods
     */
    @GetMapping("/custom")
    public ResponseEntity<List<FoodResponse>> getCustomFoods(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<FoodResponse> foods = foodService.getUserCustomFoods(userId);
        return ResponseEntity.ok(foods);
    }

    /**
     * Get a single food by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        FoodResponse food = foodService.getFoodById(id, userId);
        return ResponseEntity.ok(food);
    }

    /**
     * Create a new custom food
     */
    @PostMapping
    public ResponseEntity<FoodResponse> createFood(
            @Valid @RequestBody FoodRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        FoodResponse food = foodService.createFood(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(food);
    }

    /**
     * Update a custom food
     */
    @PutMapping("/{id}")
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable Long id,
            @Valid @RequestBody FoodRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        FoodResponse food = foodService.updateFood(id, request, userId);
        return ResponseEntity.ok(food);
    }

    /**
     * Delete a custom food
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        foodService.deleteFood(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to extract user ID from authenticated user
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
