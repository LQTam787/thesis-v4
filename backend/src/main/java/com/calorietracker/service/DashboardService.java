package com.calorietracker.service;

import com.calorietracker.dto.response.DashboardResponse;
import com.calorietracker.dto.response.MealEntryResponse;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.MealEntry;
import com.calorietracker.model.User;
import com.calorietracker.model.WeightEntry;
import com.calorietracker.repository.MealEntryRepository;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.repository.WeightEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for dashboard data aggregation and presentation.
 * 
 * <p>This service aggregates data from multiple sources (meals, weight entries, user profile)
 * to provide a comprehensive daily overview for the user's dashboard view.</p>
 * 
 * <h2>Dashboard Data Includes:</h2>
 * <ul>
 *   <li><b>Calorie Summary:</b> Allowed, consumed, and remaining calories for the day</li>
 *   <li><b>Meals by Type:</b> Grouped meal entries (breakfast, lunch, dinner, snacks)</li>
 *   <li><b>Weight Info:</b> Current weight, goal weight, and today's weight entry if logged</li>
 *   <li><b>User Context:</b> Name, goal type for personalized display</li>
 * </ul>
 * 
 * <h2>Design Pattern:</h2>
 * <p>Implements the Aggregator pattern, combining data from multiple repositories
 * into a single cohesive response object for the frontend.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see DashboardResponse
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WeightEntryRepository weightEntryRepository;

    public DashboardResponse getDashboardData(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get consumed calories for the date
        Integer consumedCalories = mealEntryRepository.sumCaloriesForUserAndDate(userId, date);
        Integer remainingCalories = user.getAllowedDailyIntake() - consumedCalories;

        // Get today's weight entry if exists
        Optional<WeightEntry> todayWeightEntry = weightEntryRepository.findByUserIdAndEntryDate(userId, date);
        BigDecimal todayWeight = todayWeightEntry.map(WeightEntry::getWeight).orElse(null);

        // Get meal entries for the date, grouped by meal type
        List<MealEntry> mealEntries = mealEntryRepository.findByUserIdAndEntryDateOrderByEntryTimeAsc(userId, date);
        
        Map<String, List<MealEntryResponse>> mealsByType = mealEntries.stream()
                .map(MealEntryResponse::fromEntity)
                .collect(Collectors.groupingBy(
                        entry -> entry.getMealType().name(),
                        Collectors.toList()
                ));

        return DashboardResponse.builder()
                .date(date)
                .allowedDailyIntake(user.getAllowedDailyIntake())
                .consumedCalories(consumedCalories)
                .remainingCalories(remainingCalories)
                .userName(user.getName())
                .goalType(user.getGoalType())
                .currentWeight(user.getWeight())
                .goalWeight(user.getGoal())
                .todayWeight(todayWeight)
                .mealsByType(mealsByType)
                .totalMealsCount(mealEntries.size())
                .build();
    }

    public DashboardResponse getTodayDashboard(Long userId) {
        return getDashboardData(userId, LocalDate.now());
    }
}
