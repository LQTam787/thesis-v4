package com.calorietracker.service;

import com.calorietracker.dto.request.MealEntryRequest;
import com.calorietracker.dto.response.MealEntryResponse;
import com.calorietracker.exception.BadRequestException;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.Food;
import com.calorietracker.model.MealEntry;
import com.calorietracker.model.User;
import com.calorietracker.repository.FoodRepository;
import com.calorietracker.repository.MealEntryRepository;
import com.calorietracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for meal entry (food consumption log) operations.
 * 
 * <p>This service handles the core calorie tracking functionality, allowing users
 * to log meals, view their consumption history, and calculate daily calorie totals.</p>
 * 
 * <h2>Key Operations:</h2>
 * <ul>
 *   <li><b>Log Meal:</b> Record food consumption with date and time</li>
 *   <li><b>View by Date:</b> Retrieve meals for a specific day or date range</li>
 *   <li><b>Calculate Totals:</b> Sum calories consumed for a given period</li>
 *   <li><b>Delete Entry:</b> Remove incorrectly logged meals</li>
 * </ul>
 * 
 * <h2>Access Control:</h2>
 * <p>Users can only access and modify their own meal entries. The service validates
 * that users have access to the food items they're logging (system or own custom foods).</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see MealEntry
 * @see MealEntryRepository
 */
@Service
@RequiredArgsConstructor
public class MealEntryService {

    private final MealEntryRepository mealEntryRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    /**
     * Create a new meal entry
     */
    @Transactional
    public MealEntryResponse createMealEntry(MealEntryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + request.getFoodId()));

        // Verify user has access to this food (system food or their own custom food)
        if (food.getUser() != null && !food.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have access to this food");
        }

        MealEntry mealEntry = MealEntry.builder()
                .user(user)
                .food(food)
                .entryDate(request.getEntryDate())
                .entryTime(request.getEntryTime())
                .build();

        MealEntry savedEntry = mealEntryRepository.save(mealEntry);
        return MealEntryResponse.fromEntity(savedEntry);
    }

    /**
     * Get all meal entries for a user on a specific date
     */
    public List<MealEntryResponse> getMealEntriesByDate(Long userId, LocalDate date) {
        return mealEntryRepository.findByUserIdAndEntryDateOrderByEntryTimeAsc(userId, date)
                .stream()
                .map(MealEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get today's meal entries for a user
     */
    public List<MealEntryResponse> getTodayMealEntries(Long userId) {
        return getMealEntriesByDate(userId, LocalDate.now());
    }

    /**
     * Get meal entries for a date range
     */
    public List<MealEntryResponse> getMealEntriesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return mealEntryRepository.findByUserIdAndEntryDateBetween(userId, startDate, endDate)
                .stream()
                .map(MealEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a single meal entry by ID
     */
    public MealEntryResponse getMealEntryById(Long entryId, Long userId) {
        MealEntry mealEntry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal entry not found with id: " + entryId));

        // Verify ownership
        if (!mealEntry.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Meal entry not found with id: " + entryId);
        }

        return MealEntryResponse.fromEntity(mealEntry);
    }

    /**
     * Delete a meal entry (only owner can delete)
     */
    @Transactional
    public void deleteMealEntry(Long entryId, Long userId) {
        MealEntry mealEntry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal entry not found with id: " + entryId));

        // Verify ownership
        if (!mealEntry.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own meal entries");
        }

        mealEntryRepository.delete(mealEntry);
    }

    /**
     * Get total calories consumed for a user on a specific date
     */
    public Integer getTotalCaloriesForDate(Long userId, LocalDate date) {
        return mealEntryRepository.sumCaloriesForUserAndDate(userId, date);
    }

    /**
     * Get today's total calories consumed
     */
    public Integer getTodayTotalCalories(Long userId) {
        return getTotalCaloriesForDate(userId, LocalDate.now());
    }
}
