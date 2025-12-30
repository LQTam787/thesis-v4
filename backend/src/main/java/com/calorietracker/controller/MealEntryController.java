package com.calorietracker.controller;

import com.calorietracker.dto.request.MealEntryRequest;
import com.calorietracker.dto.response.MealEntryResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.MealEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for meal entry (food consumption log) operations.
 * 
 * <p>Handles the core calorie tracking functionality - logging meals,
 * viewing consumption history, and calculating calorie totals.</p>
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li><b>POST /api/meal-entries:</b> Log a new meal</li>
 *   <li><b>GET /api/meal-entries/today:</b> Get today's meals</li>
 *   <li><b>GET /api/meal-entries/date/{date}:</b> Get meals for specific date</li>
 *   <li><b>GET /api/meal-entries/range:</b> Get meals for date range</li>
 *   <li><b>GET /api/meal-entries/{id}:</b> Get single meal entry</li>
 *   <li><b>DELETE /api/meal-entries/{id}:</b> Delete meal entry</li>
 *   <li><b>GET /api/meal-entries/today/calories:</b> Get today's calorie total</li>
 *   <li><b>GET /api/meal-entries/date/{date}/calories:</b> Get calorie total for date</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see MealEntryService
 */
@RestController
@RequestMapping("/api/meal-entries")
@RequiredArgsConstructor
public class MealEntryController {

    private final MealEntryService mealEntryService;
    private final UserRepository userRepository;

    /**
     * Create a new meal entry
     */
    @PostMapping
    public ResponseEntity<MealEntryResponse> createMealEntry(
            @Valid @RequestBody MealEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        MealEntryResponse mealEntry = mealEntryService.createMealEntry(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mealEntry);
    }

    /**
     * Get today's meal entries
     */
    @GetMapping("/today")
    public ResponseEntity<List<MealEntryResponse>> getTodayMealEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<MealEntryResponse> entries = mealEntryService.getTodayMealEntries(userId);
        return ResponseEntity.ok(entries);
    }

    /**
     * Get meal entries for a specific date
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<MealEntryResponse>> getMealEntriesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<MealEntryResponse> entries = mealEntryService.getMealEntriesByDate(userId, date);
        return ResponseEntity.ok(entries);
    }

    /**
     * Get meal entries for a date range
     */
    @GetMapping("/range")
    public ResponseEntity<List<MealEntryResponse>> getMealEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<MealEntryResponse> entries = mealEntryService.getMealEntriesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    /**
     * Get a single meal entry by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MealEntryResponse> getMealEntryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        MealEntryResponse entry = mealEntryService.getMealEntryById(id, userId);
        return ResponseEntity.ok(entry);
    }

    /**
     * Delete a meal entry
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMealEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        mealEntryService.deleteMealEntry(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get today's total calories
     */
    @GetMapping("/today/calories")
    public ResponseEntity<Integer> getTodayTotalCalories(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        Integer totalCalories = mealEntryService.getTodayTotalCalories(userId);
        return ResponseEntity.ok(totalCalories);
    }

    /**
     * Get total calories for a specific date
     */
    @GetMapping("/date/{date}/calories")
    public ResponseEntity<Integer> getTotalCaloriesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        Integer totalCalories = mealEntryService.getTotalCaloriesForDate(userId, date);
        return ResponseEntity.ok(totalCalories);
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
