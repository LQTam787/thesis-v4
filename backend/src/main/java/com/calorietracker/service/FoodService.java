package com.calorietracker.service;

import com.calorietracker.dto.request.FoodRequest;
import com.calorietracker.dto.response.FoodResponse;
import com.calorietracker.exception.BadRequestException;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.Food;
import com.calorietracker.model.MealType;
import com.calorietracker.model.User;
import com.calorietracker.repository.FoodRepository;
import com.calorietracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for food management operations.
 * 
 * <p>This service manages both system-wide foods (available to all users) and
 * custom foods created by individual users. It enforces access control to ensure
 * users can only modify their own custom foods.</p>
 * 
 * <h2>Food Types:</h2>
 * <ul>
 *   <li><b>System Foods:</b> Pre-defined foods with user=null, available to everyone</li>
 *   <li><b>Custom Foods:</b> User-created foods linked to their creator</li>
 * </ul>
 * 
 * <h2>Access Control:</h2>
 * <ul>
 *   <li>All users can view system foods and their own custom foods</li>
 *   <li>Only the creator can update or delete custom foods</li>
 *   <li>System foods cannot be modified or deleted by users</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see Food
 * @see FoodRepository
 */
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    /**
     * Get all foods available to a user (system foods + user's custom foods)
     */
    public List<FoodResponse> getAvailableFoods(Long userId) {
        return foodRepository.findAvailableFoodsForUser(userId)
                .stream()
                .map(FoodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get foods by meal type for a user
     */
    public List<FoodResponse> getFoodsByMealType(Long userId, MealType mealType) {
        return foodRepository.findByMealTypeForUser(userId, mealType)
                .stream()
                .map(FoodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get only the user's custom foods
     */
    public List<FoodResponse> getUserCustomFoods(Long userId) {
        return foodRepository.findByUserId(userId)
                .stream()
                .map(FoodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a single food by ID
     */
    public FoodResponse getFoodById(Long foodId, Long userId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + foodId));

        // Check if user has access to this food (system food or their own custom food)
        if (food.getUser() != null && !food.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Food not found with id: " + foodId);
        }

        return FoodResponse.fromEntity(food);
    }

    /**
     * Create a custom food for a user
     */
    @Transactional
    public FoodResponse createFood(FoodRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Food food = Food.builder()
                .name(request.getName())
                .image(request.getImage())
                .mealType(request.getMealType())
                .calories(request.getCalories())
                .user(user)
                .build();

        Food savedFood = foodRepository.save(food);
        return FoodResponse.fromEntity(savedFood);
    }

    /**
     * Update a custom food (only owner can update)
     */
    @Transactional
    public FoodResponse updateFood(Long foodId, FoodRequest request, Long userId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + foodId));

        // Only allow updating custom foods owned by the user
        if (food.getUser() == null) {
            throw new BadRequestException("Cannot update system foods");
        }
        if (!food.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only update your own custom foods");
        }

        food.setName(request.getName());
        food.setImage(request.getImage());
        food.setMealType(request.getMealType());
        food.setCalories(request.getCalories());

        Food updatedFood = foodRepository.save(food);
        return FoodResponse.fromEntity(updatedFood);
    }

    /**
     * Delete a custom food (only owner can delete)
     */
    @Transactional
    public void deleteFood(Long foodId, Long userId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + foodId));

        // Only allow deleting custom foods owned by the user
        if (food.getUser() == null) {
            throw new BadRequestException("Cannot delete system foods");
        }
        if (!food.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own custom foods");
        }

        foodRepository.delete(food);
    }
}
